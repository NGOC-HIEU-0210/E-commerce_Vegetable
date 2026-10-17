package com.hieu.service.impl;

import com.hieu.mapper.OrderMapper;
import com.hieu.model.*;
import com.hieu.payload.dto.OrderDto;
import com.hieu.payload.request.CheckoutRequest;
import com.hieu.payload.response.PageResponse;
import com.hieu.repository.*;
import com.hieu.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final SaleOrderRepository saleOrderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final CouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return saleOrderRepository.findAll().stream().map(OrderMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderDto> findPaged(Pageable pageable) {
        return PageResponse.of(saleOrderRepository.findAll(pageable), OrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        return OrderMapper.toDto(getOrder(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto findByCode(String orderCode) {
        return OrderMapper.toDto(saleOrderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderCode)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findByUser(Long userId) {
        return saleOrderRepository.findByUserId(userId).stream().map(OrderMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderDto> search(Long statusId, String keyword, Pageable pageable) {
        Page<SaleOrder> page;
        if (keyword == null || keyword.isBlank()) {
            page = (statusId == null)
                    ? saleOrderRepository.findAll(pageable)
                    : saleOrderRepository.findByStatusId(statusId, pageable);
        } else {
            page = saleOrderRepository.search(statusId, keyword, pageable);
        }
        return PageResponse.of(page, OrderMapper::toDto);
    }

    @Override
    @Transactional
    public OrderDto checkout(CheckoutRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        OrderStatus status = resolveStatus(request.getStatusId());
        User user = request.getUserId() == null ? null
                : userRepository.findById(request.getUserId()).orElse(null);
        Coupon coupon = request.getCouponCode() == null ? null
                : couponRepository.findByCode(request.getCouponCode()).orElse(null);

        SaleOrder order = SaleOrder.builder()
                .orderCode(generateOrderCode())
                .customerName(request.getCustomerName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .note(request.getNote())
                .orderDate(LocalDateTime.now())
                .status(status)
                .user(user)
                .coupon(coupon)
                .couponCode(coupon == null ? null : coupon.getCode())
                .build();

        List<SaleOrderProduct> items = new ArrayList<>();
        double subtotal = 0;
        for (CheckoutRequest.CheckoutItem item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));
            int qty = item.getQuantity() == null ? 1 : item.getQuantity();
            double lineTotal = product.getPrice() * qty;
            subtotal += lineTotal;

            items.add(SaleOrderProduct.builder()
                    .order(order)
                    .product(product)
                    .quantity(qty)
                    .price(product.getPrice())
                    .build());
        }
        order.setItems(items);
        order.setTotal(subtotal - discountFor(coupon, subtotal));

        SaleOrder saved = saleOrderRepository.save(order);

        if (coupon != null) {
            coupon.setUsedCount((coupon.getUsedCount() == null ? 0 : coupon.getUsedCount()) + 1);
            couponRepository.save(coupon);
        }
        return OrderMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OrderDto updateStatus(Long id, Long statusId) {
        SaleOrder order = getOrder(id);
        order.setStatus(resolveStatus(statusId));
        return OrderMapper.toDto(saleOrderRepository.save(order));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!saleOrderRepository.existsById(id)) {
            throw new RuntimeException("Order not found: " + id);
        }
        saleOrderRepository.deleteById(id);
    }

    private double discountFor(Coupon coupon, double subtotal) {
        if (coupon == null || Boolean.FALSE.equals(coupon.getActive())) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        if (coupon.getEndDate() != null && today.isAfter(coupon.getEndDate())) return 0;
        if (coupon.getStartDate() != null && today.isBefore(coupon.getStartDate())) return 0;
        if (coupon.getMinOrderValue() != null && subtotal < coupon.getMinOrderValue()) return 0;

        double discount;
        if ("PERCENT".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = subtotal * coupon.getDiscountValue() / 100.0;
            if (coupon.getMaxDiscount() != null && coupon.getMaxDiscount() > 0) {
                discount = Math.min(discount, coupon.getMaxDiscount());
            }
        } else {
            discount = coupon.getDiscountValue() == null ? 0 : coupon.getDiscountValue();
        }
        return Math.min(discount, subtotal);
    }

    private OrderStatus resolveStatus(Long statusId) {
        if (statusId != null) {
            return orderStatusRepository.findById(statusId)
                    .orElseThrow(() -> new RuntimeException("Order status not found: " + statusId));
        }
        return orderStatusRepository.findByName("PENDING").orElseGet(() ->
                orderStatusRepository.save(OrderStatus.builder()
                        .name("PENDING").description("Chờ xử lý").build()));
    }

    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis()
                + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private SaleOrder getOrder(Long id) {
        return saleOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }
}

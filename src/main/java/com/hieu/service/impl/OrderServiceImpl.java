package com.hieu.service.impl;

import com.hieu.enums.PaymentMethod;
import com.hieu.enums.PaymentStatus;
import com.hieu.mapper.OrderMapper;
import com.hieu.model.*;
import com.hieu.payload.dto.OrderDto;
import com.hieu.payload.dto.OrderTrackingDto;
import com.hieu.payload.request.CheckoutRequest;
import com.hieu.payload.response.PageResponse;
import com.hieu.repository.*;
import com.hieu.service.InventoryService;
import com.hieu.service.OrderService;
import com.hieu.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final CartItemRepository cartItemRepository;
    private final PricingService pricingService;
    private final InventoryService inventoryService;

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
                .paymentMethod(resolvePaymentMethod(request.getPaymentMethod()))
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        List<SaleOrderProduct> items = new ArrayList<>();
        double subtotal = 0;
        for (CheckoutRequest.CheckoutItem item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));
            int qty = item.getQuantity() == null ? 1 : item.getQuantity();
            double unitPrice = pricingService.currentPrice(product);   // giá sau giảm theo độ tươi, tính trước khi trừ lô
            inventoryService.consume(product.getId(), qty);            // trừ theo lô (FEFO), lỗi nếu hàng tươi không đủ
            if (productRepository.decreaseStock(product.getId(), qty) == 0) {
                throw new IllegalArgumentException("Not enough stock for: " + product.getName());
            }
            double lineTotal = unitPrice * qty;
            subtotal += lineTotal;

            items.add(SaleOrderProduct.builder()
                    .order(order)
                    .product(product)
                    .quantity(qty)
                    .price(unitPrice)
                    .build());
        }
        order.setItems(items);
        order.getTrackings().add(newTracking(order, status, "Đặt hàng thành công"));
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
    public OrderDto checkoutFromCart(Long userId, CheckoutRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByIdAsc(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        List<CheckoutRequest.CheckoutItem> items = new ArrayList<>();
        for (CartItem ci : cartItems) {
            if (!Boolean.TRUE.equals(ci.getProduct().getActive())) {
                throw new IllegalArgumentException("Product unavailable: " + ci.getProduct().getName());
            }
            CheckoutRequest.CheckoutItem item = new CheckoutRequest.CheckoutItem();
            item.setProductId(ci.getProduct().getId());
            item.setQuantity(ci.getQuantity());
            items.add(item);
        }
        request.setItems(items);
        request.setUserId(userId);
        request.setStatusId(null);

        OrderDto dto = checkout(request);
        cartItemRepository.deleteAll(cartItems);   // đặt xong thì xóa giỏ
        return dto;
    }

    @Override
    @Transactional
    public OrderDto updateStatus(Long id, Long statusId) {
        SaleOrder order = getOrder(id);
        String oldName = order.getStatus() == null ? null : order.getStatus().getName();
        OrderStatus status = resolveStatus(statusId);
        order.setStatus(status);

        if ("CANCELLED".equals(status.getName()) && !"CANCELLED".equals(oldName)) {
            releaseCoupon(order);
            restoreStock(order);
        }
        if ("COMPLETED".equals(status.getName()) && order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);   // COD: thu tiền khi giao xong
        }
        order.getTrackings().add(newTracking(order, status, status.getDescription()));
        return OrderMapper.toDto(saleOrderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto cancelByUser(Long id, Long userId) {
        SaleOrder order = getOrder(id);
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }
        String current = order.getStatus() == null ? null : order.getStatus().getName();
        if (!"PENDING".equals(current)) {
            throw new IllegalArgumentException("Only pending orders can be cancelled");
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Paid orders cannot be cancelled, please contact support");
        }

        OrderStatus cancelled = orderStatusRepository.findByName("CANCELLED")
                .orElseThrow(() -> new IllegalStateException("CANCELLED status is not configured"));
        order.setStatus(cancelled);
        releaseCoupon(order);
        restoreStock(order);
        order.getTrackings().add(newTracking(order, cancelled, "Khách hàng huỷ đơn"));
        return OrderMapper.toDto(saleOrderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto updatePaymentStatus(Long id, PaymentStatus paymentStatus) {
        SaleOrder order = getOrder(id);
        order.setPaymentStatus(paymentStatus);
        if (order.getStatus() != null) {
            order.getTrackings().add(newTracking(order, order.getStatus(),
                    paymentStatus == PaymentStatus.PAID ? "Đã thanh toán" : "Chưa thanh toán"));
        }
        return OrderMapper.toDto(saleOrderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderTrackingDto> findTracking(Long id) {
        return getOrder(id).getTrackings().stream()
                .sorted(Comparator.comparing(OrderTracking::getId))
                .map(OrderMapper::toTrackingDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!saleOrderRepository.existsById(id)) {
            throw new RuntimeException("Order not found: " + id);
        }
        saleOrderRepository.deleteById(id);
    }

    private OrderTracking newTracking(SaleOrder order, OrderStatus status, String note) {
        return OrderTracking.builder().order(order).status(status.getName()).note(note).build();
    }

    private void releaseCoupon(SaleOrder order) {
        Coupon coupon = order.getCoupon();
        if (coupon != null && coupon.getUsedCount() != null && coupon.getUsedCount() > 0) {
            coupon.setUsedCount(coupon.getUsedCount() - 1);
            couponRepository.save(coupon);
        }
    }
    private void restoreStock(SaleOrder order) {
        for (SaleOrderProduct it : order.getItems()) {
            productRepository.increaseStock(it.getProduct().getId(), it.getQuantity());
            inventoryService.restore(it.getProduct().getId(), it.getQuantity());
        }
    }

    private PaymentMethod resolvePaymentMethod(String value) {
        if (value == null || value.isBlank()) {
            return PaymentMethod.COD;
        }
        try {
            return PaymentMethod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment method: " + value);
        }
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
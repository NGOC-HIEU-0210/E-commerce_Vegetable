package com.hieu.mapper;

import com.hieu.model.SaleOrder;
import com.hieu.model.SaleOrderProduct;
import com.hieu.payload.dto.OrderDto;
import com.hieu.payload.dto.OrderItemDto;

import java.time.format.DateTimeFormatter;
import java.util.List;

public final class OrderMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private OrderMapper() {
    }

    public static OrderDto toDto(SaleOrder order) {
        if (order == null) {
            return null;
        }
        return OrderDto.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .customerName(order.getCustomerName())
                .email(order.getEmail())
                .phone(order.getPhone())
                .address(order.getAddress())
                .note(order.getNote())
                .orderDate(order.getOrderDate() == null ? null : order.getOrderDate().format(FORMATTER))
                .total(order.getTotal())
                .status(order.getStatus() == null ? null : order.getStatus().getName())
                .statusId(order.getStatus() == null ? null : order.getStatus().getId())
                .couponCode(order.getCouponCode())
                .userId(order.getUser() == null ? null : order.getUser().getId())
                .items(order.getItems() == null ? List.of()
                        : order.getItems().stream().map(OrderMapper::toItemDto).toList())
                .build();
    }

    public static OrderItemDto toItemDto(SaleOrderProduct item) {
        if (item == null) {
            return null;
        }
        Double price = item.getPrice();
        Integer qty = item.getQuantity();
        return OrderItemDto.builder()
                .productId(item.getProduct() == null ? null : item.getProduct().getId())
                .productName(item.getProduct() == null ? null : item.getProduct().getName())
                .productImage(item.getProduct() == null ? null : item.getProduct().getImage())
                .quantity(qty)
                .price(price)
                .subtotal(price == null || qty == null ? null : price * qty)
                .build();
    }
}

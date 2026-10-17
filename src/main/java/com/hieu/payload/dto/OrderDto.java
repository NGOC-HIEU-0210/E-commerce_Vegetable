package com.hieu.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {

    private Long id;
    private String orderCode;
    private String customerName;
    private String email;
    private String phone;
    private String address;
    private String note;
    private String orderDate;
    private Double total;
    private String status;
    private Long statusId;
    private String couponCode;
    private Long userId;
    private List<OrderItemDto> items;
}

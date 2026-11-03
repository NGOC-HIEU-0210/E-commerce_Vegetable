package com.hieu.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Long productId;
    private String productName;
    private String productImage;

    private Double price;
    private Double originalPrice;
    private Integer discountPercent;
    private Integer quantity;
    private Double subtotal;
    private Boolean available;
}
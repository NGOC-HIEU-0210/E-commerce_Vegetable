package com.hieu.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavouriteDto {

    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productImage;
    private Double productPrice;
    private LocalDateTime createdDate;
}

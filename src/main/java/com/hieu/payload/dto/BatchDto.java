package com.hieu.payload.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDto {
    private Long id;
    private Long productId;
    private String productName;
    private Integer initialQuantity;
    private Integer remainingQuantity;
    private Integer wastedQuantity;
    private Double costPrice;
    private LocalDate receivedDate;
    private LocalDate expiryDate;

    private Integer daysLeft;
    private String status;
}

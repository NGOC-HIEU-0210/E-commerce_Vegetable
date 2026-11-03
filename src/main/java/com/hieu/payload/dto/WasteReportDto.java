package com.hieu.payload.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WasteReportDto {

    private LocalDate from;
    private LocalDate to;
    private long totalWastedQuantity;
    private double totalWastedCost;
    private List<Item> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private Long productId;
        private String productName;
        private long wastedQuantity;
        private double wastedCost;
    }
}

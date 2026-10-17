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
public class DashboardDto {

    private long totalUsers;
    private long totalProducts;
    private long totalCategories;
    private long totalOrders;
    private long totalCoupons;
    private double totalRevenue;
    private long pendingOrders;
    private long completedOrders;
    private long cancelledOrders;
}

package com.hieu.controller;

import com.hieu.payload.dto.DashboardDto;
import com.hieu.payload.response.ApiResponse;
import com.hieu.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Thống kê dành cho quản trị viên (yêu cầu quyền ADMIN)")
public class DashboardController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final CouponRepository couponRepository;
    private final OrderStatusRepository orderStatusRepository;

    @GetMapping
    @Operation(summary = "Lấy số liệu thống kê tổng quan")
    public ApiResponse<DashboardDto> stats() {
        DashboardDto dto = DashboardDto.builder()
                .totalUsers(userRepository.count())
                .totalProducts(productRepository.count())
                .totalCategories(categoryRepository.count())
                .totalOrders(saleOrderRepository.count())
                .totalCoupons(couponRepository.count())
                .totalRevenue(revenue("COMPLETED"))
                .pendingOrders(countByStatus("PENDING"))
                .completedOrders(countByStatus("COMPLETED"))
                .cancelledOrders(countByStatus("CANCELLED"))
                .build();
        return ApiResponse.success(dto);
    }

    private Long statusId(String name) {
        return orderStatusRepository.findByName(name).map(s -> s.getId()).orElse(null);
    }

    private long countByStatus(String name) {
        Long id = statusId(name);
        return id == null ? 0 : saleOrderRepository.countByStatusId(id);
    }

    private double revenue(String name) {
        Long id = statusId(name);
        if (id == null) {
            return 0;
        }
        Double v = saleOrderRepository.sumTotalByStatusId(id);
        return v == null ? 0 : v;
    }
}

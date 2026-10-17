package com.hieu.controller;

import com.hieu.model.OrderStatus;
import com.hieu.payload.response.ApiResponse;
import com.hieu.service.OrderStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-statuses")
@RequiredArgsConstructor
@Tag(name = "Order Statuses", description = "Trạng thái đơn hàng")
public class OrderStatusController {

    private final OrderStatusService orderStatusService;

    @GetMapping
    @Operation(summary = "Lấy danh sách trạng thái đơn hàng")
    public ApiResponse<List<OrderStatus>> findAll() {
        return ApiResponse.success(orderStatusService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy trạng thái đơn hàng theo id")
    public ApiResponse<OrderStatus> findById(@PathVariable Long id) {
        return ApiResponse.success(orderStatusService.findById(id));
    }
}

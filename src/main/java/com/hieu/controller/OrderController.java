package com.hieu.controller;

import com.hieu.config.CurrentUser;
import com.hieu.payload.dto.OrderDto;
import com.hieu.payload.request.CheckoutRequest;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.PageResponse;
import com.hieu.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Quản lý đơn hàng")
public class OrderController {

    private final OrderService orderService;
    private final CurrentUser currentUser;

    @GetMapping
    @Operation(summary = "Lấy tất cả đơn hàng")
    public ApiResponse<List<OrderDto>> findAll() {
        return ApiResponse.success(orderService.findAll());
    }

    @GetMapping("/paged")
    @Operation(summary = "Lấy đơn hàng phân trang")
    public ApiResponse<PageResponse<OrderDto>> findPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.findPaged(
                PageRequest.of(page, size, Sort.by("id").descending())));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm đơn hàng theo trạng thái và từ khóa")
    public ApiResponse<PageResponse<OrderDto>> search(
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.search(statusId, keyword,
                PageRequest.of(page, size, Sort.by("id").descending())));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy đơn hàng của người dùng đang đăng nhập")
    public ApiResponse<List<OrderDto>> findMine(Authentication auth) {
        return ApiResponse.success(orderService.findByUser(currentUser.id(auth)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Lấy đơn hàng theo người dùng (chỉ admin)")
    public ApiResponse<List<OrderDto>> findByUser(@PathVariable Long userId) {
        return ApiResponse.success(orderService.findByUser(userId));
    }

    @GetMapping("/code/{orderCode}")
    @Operation(summary = "Lấy đơn hàng theo mã đơn (chủ đơn hoặc admin)")
    public ApiResponse<OrderDto> findByCode(Authentication auth, @PathVariable String orderCode) {
        OrderDto order = orderService.findByCode(orderCode);
        currentUser.requireOwnerOrAdmin(auth, order.getUserId());
        return ApiResponse.success(order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy đơn hàng theo id (chủ đơn hoặc admin)")
    public ApiResponse<OrderDto> findById(Authentication auth, @PathVariable Long id) {
        OrderDto order = orderService.findById(id);
        currentUser.requireOwnerOrAdmin(auth, order.getUserId());
        return ApiResponse.success(order);
    }

    @PostMapping("/checkout")
    @Operation(summary = "Đặt hàng (checkout) từ danh sách sản phẩm")
    public ApiResponse<OrderDto> checkout(Authentication auth, @RequestBody CheckoutRequest request) {

        request.setUserId(currentUser.id(auth));
        if (!currentUser.isAdmin(auth)) {
            request.setStatusId(null);
        }
        return ApiResponse.success("Order placed", orderService.checkout(request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Cập nhật trạng thái đơn hàng (chỉ admin)")
    public ApiResponse<OrderDto> updateStatus(@PathVariable Long id, @RequestParam Long statusId) {
        return ApiResponse.success("Status updated", orderService.updateStatus(id, statusId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Xóa đơn hàng (chỉ admin)")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ApiResponse.success("Order deleted", null);
    }
}
package com.hieu.controller;

import com.hieu.payload.dto.CouponDto;
import com.hieu.payload.request.CouponApplyRequest;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.PageResponse;
import com.hieu.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "Quản lý mã giảm giá")
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    @Operation(summary = "Lấy tất cả mã giảm giá")
    public ApiResponse<List<CouponDto>> findAll() {
        return ApiResponse.success(couponService.findAll());
    }

    @GetMapping("/paged")
    @Operation(summary = "Lấy mã giảm giá phân trang")
    public ApiResponse<PageResponse<CouponDto>> findPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(couponService.findPaged(
                PageRequest.of(page, size, Sort.by("id").descending())));
    }

    @GetMapping("/active")
    @Operation(summary = "Lấy các mã giảm giá đang hiệu lực")
    public ApiResponse<List<CouponDto>> findActive() {
        return ApiResponse.success(couponService.findActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy mã giảm giá theo id")
    public ApiResponse<CouponDto> findById(@PathVariable Long id) {
        return ApiResponse.success(couponService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Tạo mã giảm giá mới")
    public ApiResponse<CouponDto> create(@RequestBody CouponDto dto) {
        return ApiResponse.success("Coupon created", couponService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật mã giảm giá")
    public ApiResponse<CouponDto> update(@PathVariable Long id, @RequestBody CouponDto dto) {
        return ApiResponse.success("Coupon updated", couponService.update(id, dto));
    }

    @PostMapping("/apply")
    @Operation(summary = "Áp dụng mã giảm giá và tính số tiền được giảm")
    public ApiResponse<Double> apply(@RequestBody CouponApplyRequest request) {
        return ApiResponse.success("Discount calculated", couponService.apply(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mã giảm giá")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return ApiResponse.success("Coupon deleted", null);
    }
}

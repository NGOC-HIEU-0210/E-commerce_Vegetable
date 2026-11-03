package com.hieu.controller;

import com.hieu.payload.dto.BatchDto;
import com.hieu.payload.dto.WasteReportDto;
import com.hieu.payload.request.BatchRequest;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.PageResponse;
import com.hieu.service.InventoryService;
import com.hieu.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/batches")
@RequiredArgsConstructor
@Tag(name = "Admin Batches", description = "Quản lý lô hàng, hạn dùng, hao hụt (yêu cầu quyền ADMIN)")
public class BatchController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Nhập 1 lô hàng mới (tự cộng vào tồn kho)")
    public ApiResponse<BatchDto> receive(@Valid @RequestBody BatchRequest request) {
        return ApiResponse.success("Batch received", inventoryService.receive(request));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Danh sách lô của 1 sản phẩm, sắp theo hạn, phân trang")
    public ApiResponse<PageResponse<BatchDto>> byProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(inventoryService.findByProduct(productId, pageOf(page, size, Sort.by("expiryDate").ascending())));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Lô sắp hết hạn trong N ngày tới (gồm cả lô đã quá hạn chưa xuất hủy), phân trang")
    public ApiResponse<PageResponse<BatchDto>> expiring(
            @RequestParam(defaultValue = "2") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(inventoryService.findExpiring(days, pageOf(page, size, Sort.by("expiryDate").ascending())));
    }

    @GetMapping("/waste-report")
    @Operation(summary = "Báo cáo hao hụt theo sản phẩm (mặc định 30 ngày gần nhất)")
    public ApiResponse<WasteReportDto> wasteReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate end = to == null ? PricingService.today() : to;
        LocalDate start = from == null ? end.minusDays(30) : from;
        return ApiResponse.success(inventoryService.wasteReport(start, end));
    }

    @PostMapping("/write-off-expired")
    @Operation(summary = "Xuất hủy ngay các lô quá hạn (bình thường job tự chạy lúc 00:05)")
    public ApiResponse<Integer> writeOffNow() {
        return ApiResponse.success("Done", inventoryService.writeOffExpired());
    }

    private PageRequest pageOf(int page, int size, Sort sort) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), sort);
    }
}

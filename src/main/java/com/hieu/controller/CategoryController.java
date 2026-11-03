package com.hieu.controller;

import com.hieu.config.CurrentUser;
import com.hieu.payload.dto.CategoryDto;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.PageResponse;
import com.hieu.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Quản lý danh mục sản phẩm")
public class CategoryController {

    private final CategoryService categoryService;
    private final CurrentUser currentUser;

    @GetMapping
    @Operation(summary = "Admin: lấy tất cả danh mục (kể cả đang ẩn), phân trang, tìm theo tên")
    public ApiResponse<PageResponse<CategoryDto>> findAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(categoryService.findPaged(keyword, pageOf(page, size)));
    }

    @GetMapping("/active")
    @Operation(summary = "Khách: danh mục đang hoạt động, tìm theo tên, phân trang")
    public ApiResponse<PageResponse<CategoryDto>> findActive(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(categoryService.findActivePaged(keyword, pageOf(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy danh mục theo id (khách chỉ thấy danh mục đang hoạt động)")
    public ApiResponse<CategoryDto> findById(Authentication auth, @PathVariable Long id) {
        return ApiResponse.success(currentUser.isAdmin(auth)
                ? categoryService.findById(id)
                : categoryService.findPublicById(id));
    }

    @PostMapping
    @Operation(summary = "Tạo danh mục mới (JSON)")
    public ApiResponse<CategoryDto> create(@RequestBody CategoryDto dto) {
        return ApiResponse.success("Category created", categoryService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật danh mục")
    public ApiResponse<CategoryDto> update(@PathVariable Long id, @RequestBody CategoryDto dto) {
        return ApiResponse.success("Category updated", categoryService.update(id, dto));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tải lên ảnh danh mục")
    public ApiResponse<CategoryDto> setImage(@PathVariable Long id,
                                             @RequestPart("file") MultipartFile file) {
        return ApiResponse.success("Image updated", categoryService.setImage(id, file));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa danh mục")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success("Category deleted", null);
    }

    private PageRequest pageOf(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by("id").descending());
    }
}
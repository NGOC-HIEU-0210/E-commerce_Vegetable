package com.hieu.controller;

import com.hieu.payload.dto.CategoryDto;
import com.hieu.payload.response.ApiResponse;
import com.hieu.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Quản lý danh mục sản phẩm")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Lấy tất cả danh mục")
    public ApiResponse<List<CategoryDto>> findAll() {
        return ApiResponse.success(categoryService.findAll());
    }

    @GetMapping("/active")
    @Operation(summary = "Lấy danh mục đang hoạt động")
    public ApiResponse<List<CategoryDto>> findActive() {
        return ApiResponse.success(categoryService.findActive());
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm danh mục theo tên")
    public ApiResponse<List<CategoryDto>> search(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(categoryService.search(keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy danh mục theo id")
    public ApiResponse<CategoryDto> findById(@PathVariable Long id) {
        return ApiResponse.success(categoryService.findById(id));
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
}
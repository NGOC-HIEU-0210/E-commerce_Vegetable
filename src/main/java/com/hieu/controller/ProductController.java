package com.hieu.controller;

import com.hieu.payload.dto.ProductDto;
import com.hieu.payload.dto.ProductImageDto;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.PageResponse;
import com.hieu.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Quản lý sản phẩm")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Lấy tất cả sản phẩm")
    public ApiResponse<List<ProductDto>> findAll() {
        return ApiResponse.success(productService.findAll());
    }

    @GetMapping("/paged")
    @Operation(summary = "Lấy sản phẩm phân trang")
    public ApiResponse<PageResponse<ProductDto>> findPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(productService.findPaged(
                PageRequest.of(page, size, Sort.by("id").descending())));
    }

    @GetMapping("/latest")
    @Operation(summary = "Lấy sản phẩm mới nhất")
    public ApiResponse<List<ProductDto>> latest(@RequestParam(defaultValue = "8") int limit) {
        return ApiResponse.success(productService.findLatest(limit));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm sản phẩm theo tên và danh mục")
    public ApiResponse<List<ProductDto>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return ApiResponse.success(productService.search(keyword, categoryId));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Lấy sản phẩm theo danh mục")
    public ApiResponse<List<ProductDto>> byCategory(@PathVariable Long categoryId) {
        return ApiResponse.success(productService.findByCategory(categoryId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy sản phẩm theo id")
    public ApiResponse<ProductDto> findById(@PathVariable Long id) {
        return ApiResponse.success(productService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Tạo sản phẩm mới (JSON)")
    public ApiResponse<ProductDto> create(@RequestBody ProductDto dto) {
        return ApiResponse.success("Product created", productService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật sản phẩm")
    public ApiResponse<ProductDto> update(@PathVariable Long id, @RequestBody ProductDto dto) {
        return ApiResponse.success("Product updated", productService.update(id, dto));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tải lên ảnh chính của sản phẩm")
    public ApiResponse<ProductDto> setImage(@PathVariable Long id,
                                            @RequestPart("file") MultipartFile file) {
        return ApiResponse.success("Image updated", productService.setImage(id, file));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Thêm ảnh phụ cho sản phẩm")
    public ApiResponse<ProductImageDto> addImage(@PathVariable Long id,
                                                 @RequestPart("file") MultipartFile file) {
        return ApiResponse.success("Image added", productService.addImage(id, file));
    }

    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "Xóa ảnh phụ của sản phẩm")
    public ApiResponse<Void> deleteImage(@PathVariable Long imageId) {
        productService.deleteImage(imageId);
        return ApiResponse.success("Image deleted", null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa sản phẩm")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.success("Product deleted", null);
    }
}
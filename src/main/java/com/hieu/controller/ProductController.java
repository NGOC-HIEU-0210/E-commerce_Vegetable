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

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Quản lý sản phẩm")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Admin: Lấy tất cả sản phẩm, phân trang")
    public ApiResponse<PageResponse<ProductDto>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(productService.findPaged(pageOf(page, size)));
    }

    @GetMapping("/latest")
    @Operation(summary = "Lấy sản phẩm mới nhất, phân trang")
    public ApiResponse<PageResponse<ProductDto>> latest(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return ApiResponse.success(productService.findLatest(pageOf(page, size)));
    }

    @GetMapping("/rescue")
    @Operation(summary = "Giải cứu: hàng sắp hết hạn đang giảm giá, giảm nhiều nhất lên đầu")
    public ApiResponse<java.util.List<ProductDto>> rescue() {
        return ApiResponse.success(productService.findRescue());
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm sản phẩm theo tên và danh mục, phân trang")
    public ApiResponse<PageResponse<ProductDto>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(productService.search(keyword, categoryId, pageOf(page, size)));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Lấy sản phẩm theo danh mục, phân trang")
    public ApiResponse<PageResponse<ProductDto>> byCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(productService.findByCategory(categoryId, pageOf(page, size)));
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

    private PageRequest pageOf(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by("id").descending());
    }
}
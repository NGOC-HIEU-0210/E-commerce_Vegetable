package com.hieu.service;

import com.hieu.payload.dto.ProductDto;
import com.hieu.payload.dto.ProductImageDto;
import com.hieu.payload.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    PageResponse<ProductDto> findPaged(Pageable pageable);

    PageResponse<ProductDto> findByCategory(Long categoryId, Pageable pageable);

    PageResponse<ProductDto> search(String keyword, Long categoryId, Pageable pageable);

    PageResponse<ProductDto> findLatest(Pageable pageable);

    ProductDto findById(Long id);

    java.util.List<ProductDto> findRescue();

    ProductDto create(ProductDto dto);

    ProductDto update(Long id, ProductDto dto);

    ProductDto setImage(Long id, MultipartFile image);

    void delete(Long id);

    ProductImageDto addImage(Long productId, MultipartFile file);

    void deleteImage(Long imageId);
}

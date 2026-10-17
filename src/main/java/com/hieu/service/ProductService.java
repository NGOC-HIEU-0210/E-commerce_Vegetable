package com.hieu.service;

import com.hieu.payload.dto.ProductDto;
import com.hieu.payload.dto.ProductImageDto;
import com.hieu.payload.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    List<ProductDto> findAll();

    PageResponse<ProductDto> findPaged(Pageable pageable);

    ProductDto findById(Long id);

    List<ProductDto> findByCategory(Long categoryId);

    List<ProductDto> search(String keyword, Long categoryId);

    List<ProductDto> findLatest(int limit);

    ProductDto create(ProductDto dto);

    ProductDto update(Long id, ProductDto dto);

    ProductDto setImage(Long id, MultipartFile image);

    void delete(Long id);

    ProductImageDto addImage(Long productId, MultipartFile file);

    void deleteImage(Long imageId);
}

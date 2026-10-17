package com.hieu.service;

import com.hieu.payload.dto.CategoryDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {

    List<CategoryDto> findAll();

    List<CategoryDto> findActive();

    CategoryDto findById(Long id);

    CategoryDto create(CategoryDto dto);

    CategoryDto update(Long id, CategoryDto dto);

    CategoryDto setImage(Long id, MultipartFile image);

    void delete(Long id);

    List<CategoryDto> search(String keyword);
}

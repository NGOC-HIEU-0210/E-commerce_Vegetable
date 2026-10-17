package com.hieu.service.impl;

import com.hieu.mapper.CategoryMapper;
import com.hieu.model.Category;
import com.hieu.payload.dto.CategoryDto;
import com.hieu.repository.CategoryRepository;
import com.hieu.service.CategoryService;
import com.hieu.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UploadService uploadService;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream().map(CategoryMapper::toSummary).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findActive() {
        return categoryRepository.findByActiveTrue().stream().map(CategoryMapper::toSummary).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto findById(Long id) {
        return CategoryMapper.toDto(getCategory(id));
    }

    @Override
    @Transactional
    public CategoryDto create(CategoryDto dto) {
        if (categoryRepository.findByName(dto.getName()).isPresent()) {
            throw new IllegalArgumentException("Category name already exists");
        }
        Category category = Category.builder()
                .name(dto.getName())
                .image(dto.getImage())
                .active(dto.getActive() == null ? true : dto.getActive())
                .build();
        if (dto.getParentCategoryId() != null) {
            category.setParentCategory(getCategory(dto.getParentCategoryId()));
        }
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryDto dto) {
        Category category = getCategory(id);
        if (dto.getName() != null) category.setName(dto.getName());
        if (dto.getImage() != null) category.setImage(dto.getImage());
        if (dto.getActive() != null) category.setActive(dto.getActive());
        if (dto.getParentCategoryId() != null) {
            category.setParentCategory(getCategory(dto.getParentCategoryId()));
        }
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto setImage(Long id, MultipartFile image) {
        Category category = getCategory(id);
        String path = uploadService.save(image, "Category");
        if (path != null) {
            category.setImage(path);
        }
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> search(String keyword) {
        return categoryRepository.findAll().stream()
                .filter(c -> keyword == null || keyword.isBlank()
                        || (c.getName() != null && c.getName().toLowerCase().contains(keyword.toLowerCase())))
                .map(CategoryMapper::toSummary)
                .toList();
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }
}

package com.hieu.mapper;

import com.hieu.model.Category;
import com.hieu.payload.dto.CategoryDto;

import java.util.List;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        Category parent = category.getParentCategory();
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .image(category.getImage())
                .active(category.getActive())
                .parentCategoryId(parent == null ? null : parent.getId())
                .parentCategoryName(parent == null ? null : parent.getName())
                .children(category.getChildren() == null ? List.of()
                        : category.getChildren().stream().map(CategoryMapper::toDto).toList())
                .build();
    }

    public static CategoryDto toSummary(Category category) {
        if (category == null) {
            return null;
        }
        Category parent = category.getParentCategory();
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .image(category.getImage())
                .active(category.getActive())
                .parentCategoryId(parent == null ? null : parent.getId())
                .parentCategoryName(parent == null ? null : parent.getName())
                .children(List.of())
                .build();
    }
}

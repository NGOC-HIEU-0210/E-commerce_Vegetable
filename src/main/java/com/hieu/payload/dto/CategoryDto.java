package com.hieu.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {

    private Long id;
    private String name;
    private String image;
    private Boolean active;
    private Long parentCategoryId;
    private String parentCategoryName;
    private List<CategoryDto> children;
}

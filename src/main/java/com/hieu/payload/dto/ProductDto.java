package com.hieu.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private Long id;
    private String name;
    private String image;
    private Double price;
    private Boolean active;
    private String description;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<ProductImageDto> images;
    private Long favouriteCount;
}

package com.hieu.mapper;

import com.hieu.model.Product;
import com.hieu.model.ProductImage;
import com.hieu.payload.dto.ProductImageDto;
import com.hieu.payload.dto.ProductDto;

import java.util.List;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .image(product.getImage())
                .price(product.getPrice())
                .active(product.getActive())
                .description(product.getDescription())
                .categoryId(product.getCategory() == null ? null : product.getCategory().getId())
                .categoryName(product.getCategory() == null ? null : product.getCategory().getName())
                .createdDate(product.getCreatedDate())
                .updatedDate(product.getUpdatedDate())
                .images(product.getProductImages() == null ? List.of()
                        : product.getProductImages().stream().map(ProductMapper::toImageDto).toList())
                .build();
    }

    public static ProductImageDto toImageDto(ProductImage image) {
        if (image == null) {
            return null;
        }
        return ProductImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .productId(image.getProduct() == null ? null : image.getProduct().getId())
                .build();
    }
}

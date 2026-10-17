package com.hieu.mapper;

import com.hieu.model.Favourite;
import com.hieu.payload.dto.FavouriteDto;

public final class FavouriteMapper {

    private FavouriteMapper() {
    }

    public static FavouriteDto toDto(Favourite favourite) {
        if (favourite == null) {
            return null;
        }
        return FavouriteDto.builder()
                .id(favourite.getId())
                .userId(favourite.getUser() == null ? null : favourite.getUser().getId())
                .productId(favourite.getProduct() == null ? null : favourite.getProduct().getId())
                .productName(favourite.getProduct() == null ? null : favourite.getProduct().getName())
                .productImage(favourite.getProduct() == null ? null : favourite.getProduct().getImage())
                .productPrice(favourite.getProduct() == null ? null : favourite.getProduct().getPrice())
                .createdDate(favourite.getCreatedDate())
                .build();
    }
}

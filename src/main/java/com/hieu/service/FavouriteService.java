package com.hieu.service;

import com.hieu.payload.dto.FavouriteDto;

import java.util.List;

public interface FavouriteService {

    List<FavouriteDto> findByUser(Long userId);

    boolean toggle(Long userId, Long productId);

    boolean isFavourite(Long userId, Long productId);

    long countByProduct(Long productId);

    void remove(Long userId, Long productId);
}

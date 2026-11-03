package com.hieu.service;

import com.hieu.payload.dto.CartDto;

public interface CartService {

    CartDto getCart(Long userId);

    CartDto addItem(Long userId, Long productId, int quantity);

    CartDto setQuantity(Long userId, Long productId, int quantity);

    CartDto removeItem(Long userId, Long productId);

    void clear(Long userId);
}
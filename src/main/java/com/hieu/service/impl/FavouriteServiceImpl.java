package com.hieu.service.impl;

import com.hieu.mapper.FavouriteMapper;
import com.hieu.model.Favourite;
import com.hieu.model.Product;
import com.hieu.model.User;
import com.hieu.payload.dto.FavouriteDto;
import com.hieu.repository.FavouriteRepository;
import com.hieu.repository.ProductRepository;
import com.hieu.repository.UserRepository;
import com.hieu.service.FavouriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavouriteServiceImpl implements FavouriteService {

    private final FavouriteRepository favouriteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FavouriteDto> findByUser(Long userId) {
        return favouriteRepository.findByUserId(userId).stream()
                .map(FavouriteMapper::toDto).toList();
    }

    @Override
    @Transactional
    public boolean toggle(Long userId, Long productId) {
        var existing = favouriteRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            favouriteRepository.delete(existing.get());
            return false;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        favouriteRepository.save(Favourite.builder().user(user).product(product).build());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavourite(Long userId, Long productId) {
        return favouriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByProduct(Long productId) {
        return favouriteRepository.countByProductId(productId);
    }

    @Override
    @Transactional
    public void remove(Long userId, Long productId) {
        favouriteRepository.deleteByUserIdAndProductId(userId, productId);
    }
}

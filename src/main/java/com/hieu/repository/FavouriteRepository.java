package com.hieu.repository;

import com.hieu.model.Favourite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavouriteRepository extends JpaRepository<Favourite, Long> {

    Optional<Favourite> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    List<Favourite> findByUserId(Long userId);

    long countByProductId(Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}

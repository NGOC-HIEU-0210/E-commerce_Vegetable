package com.hieu.repository;

import com.hieu.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    List<Product> findByActiveTrue();

    @Query("SELECT p FROM Product p WHERE p.active = true "
            + "AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    List<Product> search(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category.id = :categoryId "
            + "AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    List<Product> searchByCategory(@Param("categoryId") Long categoryId,
                                   @Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.createdDate DESC")
    Page<Product> findLatest(Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.price), 0) FROM Product p WHERE p.active = true")
    Double sumPrices();
}

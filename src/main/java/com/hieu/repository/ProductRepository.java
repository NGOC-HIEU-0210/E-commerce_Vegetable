package com.hieu.repository;

import com.hieu.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    List<Product> findByActiveTrue();

    @Query("SELECT p FROM Product p WHERE p.active = true "
            + "AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category.id = :categoryId "
            + "AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> searchByCategory(@Param("categoryId") Long categoryId,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.createdDate DESC")
    Page<Product> findLatest(Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.price), 0) FROM Product p WHERE p.active = true")
    Double sumPrices();

    @Modifying
    @Query("UPDATE Product p SET p.stock = COALESCE(p.stock, 0) - :qty WHERE p.id = :id AND COALESCE(p.stock, 0) >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE Product p SET p.stock = COALESCE(p.stock, 0) + :qty WHERE p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("qty") int qty);


    @Modifying
    @Query("UPDATE Product p SET p.stock = CASE WHEN COALESCE(p.stock, 0) >= :qty THEN COALESCE(p.stock, 0) - :qty ELSE 0 END WHERE p.id = :id")
    int writeOffStock(@Param("id") Long id, @Param("qty") int qty);


    @Query("SELECT p FROM Product p WHERE p.active = true AND EXISTS (SELECT 1 FROM ProductBatch b "
            + "WHERE b.product = p AND b.status = :status AND b.remainingQuantity > 0 AND b.expiryDate >= :today)")
    List<Product> findWithSellableBatches(@Param("status") com.hieu.enums.BatchStatus status,
                                          @Param("today") java.time.LocalDate today);
}

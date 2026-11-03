package com.hieu.repository;

import com.hieu.enums.BatchStatus;
import com.hieu.model.ProductBatch;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {

    boolean existsByProductId(Long productId);

    // Lô của 1 sản phẩm, sắp theo hạn (FEFO), có phân trang.
    @Query("SELECT b FROM ProductBatch b WHERE b.product.id = :productId ORDER BY b.expiryDate ASC")
    Page<ProductBatch> findByProductIdOrderByExpiryDateAsc(@Param("productId") Long productId, Pageable pageable);

    // Lô còn bán được của 1 sản phẩm, lô gần hết hạn nhất đứng đầu (FEFO). Khóa dòng để tránh bán trùng.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM ProductBatch b WHERE b.product.id = :productId AND b.status = :status "
            + "AND b.remainingQuantity > 0 AND b.expiryDate >= :today ORDER BY b.expiryDate ASC")
    List<ProductBatch> findSellableForUpdate(@Param("productId") Long productId,
                                             @Param("status") BatchStatus status,
                                             @Param("today") LocalDate today);

    // Các lô còn chỗ để hoàn hàng khi hủy đơn (chưa hết hạn, chưa xuất hủy).
    @Query("SELECT b FROM ProductBatch b WHERE b.product.id = :productId AND b.status <> :expired "
            + "AND b.expiryDate >= :today ORDER BY b.expiryDate ASC")
    List<ProductBatch> findRestorable(@Param("productId") Long productId,
                                      @Param("expired") BatchStatus expired,
                                      @Param("today") LocalDate today);

    //Hạn gần nhất của lô còn bán được, theo từng sản phẩm: [productId, LocalDate].
    @Query("SELECT b.product.id, MIN(b.expiryDate) FROM ProductBatch b WHERE b.product.id IN :ids "
            + "AND b.status = :status AND b.remainingQuantity > 0 AND b.expiryDate >= :today "
            + "GROUP BY b.product.id")
    List<Object[]> earliestExpiry(@Param("ids") Collection<Long> ids,
                                  @Param("status") BatchStatus status,
                                  @Param("today") LocalDate today);

    // Lô sắp hết hạn (kể cả lô đã quá hạn nhưng chưa xuất hủy), có phân trang.
    @Query("SELECT b FROM ProductBatch b WHERE b.status = :status "
            + "AND b.remainingQuantity > 0 AND b.expiryDate <= :until ORDER BY b.expiryDate ASC")
    Page<ProductBatch> findExpiring(@Param("status") BatchStatus status, @Param("until") LocalDate until, Pageable pageable);

    // Lô đã quá hạn mà còn hàng -> cần xuất hủy.
    @Query("SELECT b FROM ProductBatch b JOIN FETCH b.product WHERE b.status = :status "
            + "AND b.remainingQuantity > 0 AND b.expiryDate < :today")
    List<ProductBatch> findOverdue(@Param("status") BatchStatus status, @Param("today") LocalDate today);

    @Query("SELECT b FROM ProductBatch b JOIN FETCH b.product WHERE b.wastedQuantity > 0 "
            + "AND b.expiryDate BETWEEN :from AND :to ORDER BY b.expiryDate DESC")
    List<ProductBatch> findWasted(@Param("from") LocalDate from, @Param("to") LocalDate to);
}

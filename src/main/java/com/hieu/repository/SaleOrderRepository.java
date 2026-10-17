package com.hieu.repository;

import com.hieu.model.SaleOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SaleOrderRepository extends JpaRepository<SaleOrder, Long> {

    Optional<SaleOrder> findByOrderCode(String orderCode);

    List<SaleOrder> findByUserId(Long userId);

    Page<SaleOrder> findByStatusId(Long statusId, Pageable pageable);

    @Query("SELECT o FROM SaleOrder o WHERE o.status.id = :statusId "
            + "AND (o.orderCode LIKE %:keyword% OR o.customerName LIKE %:keyword% "
            + "OR o.phone LIKE %:keyword% OR o.email LIKE %:keyword%)")
    Page<SaleOrder> search(@Param("statusId") Long statusId,
                           @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM SaleOrder o WHERE o.status.id = :statusId")
    Double sumTotalByStatusId(@Param("statusId") Long statusId);

    long countByStatusId(Long statusId);
}

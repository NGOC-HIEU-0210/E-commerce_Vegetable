package com.hieu.repository;

import com.hieu.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    List<Coupon> findByActiveTrue();

    List<Coupon> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate today, LocalDate today2);
}

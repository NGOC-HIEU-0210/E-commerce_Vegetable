package com.hieu.service;

import com.hieu.payload.dto.CouponDto;
import com.hieu.payload.request.CouponApplyRequest;
import com.hieu.payload.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CouponService {

    List<CouponDto> findAll();

    PageResponse<CouponDto> findPaged(Pageable pageable);

    CouponDto findById(Long id);

    List<CouponDto> findActive();

    CouponDto create(CouponDto dto);

    CouponDto update(Long id, CouponDto dto);

    void delete(Long id);

    PageResponse<CouponDto> search(String keyword, Pageable pageable);

    double apply(CouponApplyRequest request);

    void disableExpired();
}

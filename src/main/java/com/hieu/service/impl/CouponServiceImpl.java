package com.hieu.service.impl;

import com.hieu.mapper.CouponMapper;
import com.hieu.model.Coupon;
import com.hieu.payload.dto.CouponDto;
import com.hieu.payload.request.CouponApplyRequest;
import com.hieu.payload.response.PageResponse;
import com.hieu.repository.CouponRepository;
import com.hieu.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CouponDto> findAll() {
        return couponRepository.findAll().stream().map(CouponMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CouponDto> findPaged(Pageable pageable) {
        return PageResponse.of(couponRepository.findAll(pageable), CouponMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponDto findById(Long id) {
        return CouponMapper.toDto(getCoupon(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponDto> findActive() {
        LocalDate today = LocalDate.now();
        return couponRepository
                .findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today)
                .stream().map(CouponMapper::toDto).toList();
    }

    @Override
    @Transactional
    public CouponDto create(CouponDto dto) {
        if (couponRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists");
        }
        Coupon coupon = Coupon.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .minOrderValue(dto.getMinOrderValue())
                .maxDiscount(dto.getMaxDiscount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .usageLimit(dto.getUsageLimit())
                .usedCount(0)
                .active(dto.getActive() == null ? true : dto.getActive())
                .build();
        return CouponMapper.toDto(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public CouponDto update(Long id, CouponDto dto) {
        Coupon coupon = getCoupon(id);
        if (dto.getName() != null) coupon.setName(dto.getName());
        if (dto.getDescription() != null) coupon.setDescription(dto.getDescription());
        if (dto.getDiscountType() != null) coupon.setDiscountType(dto.getDiscountType());
        if (dto.getDiscountValue() != null) coupon.setDiscountValue(dto.getDiscountValue());
        if (dto.getMinOrderValue() != null) coupon.setMinOrderValue(dto.getMinOrderValue());
        if (dto.getMaxDiscount() != null) coupon.setMaxDiscount(dto.getMaxDiscount());
        if (dto.getStartDate() != null) coupon.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) coupon.setEndDate(dto.getEndDate());
        if (dto.getUsageLimit() != null) coupon.setUsageLimit(dto.getUsageLimit());
        if (dto.getActive() != null) coupon.setActive(dto.getActive());
        return CouponMapper.toDto(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new RuntimeException("Coupon not found: " + id);
        }
        couponRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CouponDto> search(String keyword, Pageable pageable) {
        Page<Coupon> page = (keyword == null || keyword.isBlank())
                ? couponRepository.findAll(pageable)
                : couponRepository.findAll(pageable);
        return PageResponse.of(page, CouponMapper::toDto);
    }

    @Override
    @Transactional
    public double apply(CouponApplyRequest request) {
        Coupon coupon = couponRepository.findByCode(request.getCode())
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        if (Boolean.FALSE.equals(coupon.getActive())) {
            throw new IllegalArgumentException("Coupon is not active");
        }
        LocalDate today = LocalDate.now();
        if (coupon.getStartDate() != null && today.isBefore(coupon.getStartDate())) {
            throw new IllegalArgumentException("Coupon is not started yet");
        }
        if (coupon.getEndDate() != null && today.isAfter(coupon.getEndDate())) {
            throw new IllegalArgumentException("Coupon is expired");
        }
        double total = request.getOrderTotal() == null ? 0 : request.getOrderTotal();
        if (coupon.getMinOrderValue() != null && total < coupon.getMinOrderValue()) {
            throw new IllegalArgumentException("Order total is below the minimum value");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsageLimit() > 0
                && coupon.getUsedCount() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new IllegalArgumentException("Coupon usage limit reached");
        }

        double discount;
        if ("PERCENT".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = total * coupon.getDiscountValue() / 100.0;
            if (coupon.getMaxDiscount() != null && coupon.getMaxDiscount() > 0) {
                discount = Math.min(discount, coupon.getMaxDiscount());
            }
        } else {
            discount = coupon.getDiscountValue();
        }
        return Math.min(discount, total);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void disableExpired() {
        LocalDate today = LocalDate.now();
        List<Coupon> expired = couponRepository.findByActiveTrue().stream()
                .filter(c -> c.getEndDate() != null && c.getEndDate().isBefore(today))
                .peek(c -> c.setActive(false))
                .toList();
        couponRepository.saveAll(expired);
    }

    private Coupon getCoupon(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found: " + id));
    }
}

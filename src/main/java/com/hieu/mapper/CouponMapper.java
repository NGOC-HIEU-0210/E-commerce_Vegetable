package com.hieu.mapper;

import com.hieu.model.Coupon;
import com.hieu.payload.dto.CouponDto;

public final class CouponMapper {

    private CouponMapper() {
    }

    public static CouponDto toDto(Coupon coupon) {
        if (coupon == null) {
            return null;
        }
        return CouponDto.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderValue(coupon.getMinOrderValue())
                .maxDiscount(coupon.getMaxDiscount())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .active(coupon.getActive())
                .build();
    }
}

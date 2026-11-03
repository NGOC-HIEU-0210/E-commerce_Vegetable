
package com.hieu.service;

import com.hieu.model.Coupon;
import com.hieu.payload.dto.CouponDto;
import com.hieu.payload.request.CouponApplyRequest;
import com.hieu.payload.response.PageResponse;
import com.hieu.repository.CouponRepository;
import com.hieu.service.impl.CouponServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    @Test
    void findPaged_mapsToPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);

        when(couponRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(
                        List.of(
                                Coupon.builder()
                                        .id(1L)
                                        .code("SAVE10")
                                        .build()
                        ),
                        pageable,
                        1
                ));

        PageResponse<CouponDto> result = couponService.findPaged(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals(1L, result.getTotalElements());
    }

    @Test
    void findById_returnsCoupon() {
        when(couponRepository.findById(5L))
                .thenReturn(Optional.of(
                        Coupon.builder()
                                .id(5L)
                                .code("SAVE10")
                                .build()
                ));

        CouponDto result = couponService.findById(5L);

        assertEquals(5L, result.getId());
        assertEquals("SAVE10", result.getCode());
    }

    @Test
    void create_duplicate_throws() {
        when(couponRepository.existsByCode("SAVE10"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> couponService.create(
                        CouponDto.builder()
                                .code("SAVE10")
                                .build()
                )
        );

        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void create_success_savesCoupon() {
        when(couponRepository.existsByCode("SAVE10"))
                .thenReturn(false);

        when(couponRepository.save(any(Coupon.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CouponDto result = couponService.create(
                CouponDto.builder()
                        .code("SAVE10")
                        .name("Save 10")
                        .build()
        );

        assertEquals("SAVE10", result.getCode());
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void apply_couponNotFound_throws() {
        when(couponRepository.findByCode("NOPE"))
                .thenReturn(Optional.empty());

        CouponApplyRequest request = new CouponApplyRequest();
        request.setCode("NOPE");

        assertThrows(
                IllegalArgumentException.class,
                () -> couponService.apply(request)
        );
    }

    @Test
    void apply_success_returnsDiscount() {
        Coupon coupon = Coupon.builder()
                .id(1L)
                .code("SAVE10")
                .active(true)
                .discountType("PERCENT")
                .discountValue(10D)
                .build();

        when(couponRepository.findByCode("SAVE10"))
                .thenReturn(Optional.of(coupon));

        CouponApplyRequest request = new CouponApplyRequest();
        request.setCode("SAVE10");
        request.setOrderTotal(100D);

        double discount = couponService.apply(request);

        assertEquals(10.0, discount);
    }

    @Test
    void delete_whenMissing_throws() {
        when(couponRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> couponService.delete(99L)
        );

        verify(couponRepository).existsById(99L);

        // Coupon không tồn tại nên không được thực hiện xóa.
        verify(couponRepository, never()).deleteById(99L);
    }
}

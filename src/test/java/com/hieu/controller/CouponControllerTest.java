
package com.hieu.controller;

import com.hieu.payload.dto.CouponDto;
import com.hieu.payload.request.CouponApplyRequest;
import com.hieu.payload.response.PageResponse;
import com.hieu.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CouponControllerTest {

    @Mock
    private CouponService couponService;

    @InjectMocks
    private CouponController couponController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(couponController)
                .build();
    }

    @Test
    void findAll_returnsListOfCoupons() throws Exception {
        when(couponService.findAll())
                .thenReturn(List.of(
                        CouponDto.builder()
                                .id(1L)
                                .code("SAVE10")
                                .build()
                ));

        mockMvc.perform(get("/api/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].code").value("SAVE10"));
    }

    @Test
    void findPaged_returnsPageResponse() throws Exception {
        when(couponService.findPaged(any(Pageable.class)))
                .thenReturn(pageOf());

        mockMvc.perform(get("/api/coupons/paged")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(couponService).findPaged(any(Pageable.class));
    }

    @Test
    void findById_returnsCoupon() throws Exception {
        when(couponService.findById(5L))
                .thenReturn(CouponDto.builder()
                        .id(5L)
                        .code("SAVE10")
                        .build());

        mockMvc.perform(get("/api/coupons/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.code").value("SAVE10"));
    }

    @Test
    void create_returnsCoupon() throws Exception {
        when(couponService.create(any(CouponDto.class)))
                .thenReturn(CouponDto.builder()
                        .id(1L)
                        .code("SAVE10")
                        .build());

        mockMvc.perform(post("/api/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"SAVE10\",\"name\":\"Save 10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        verify(couponService).create(any(CouponDto.class));
    }

    @Test
    void update_returnsCoupon() throws Exception {
        when(couponService.update(eq(5L), any(CouponDto.class)))
                .thenReturn(CouponDto.builder()
                        .id(5L)
                        .code("SAVE20")
                        .build());

        mockMvc.perform(put("/api/coupons/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"SAVE20\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));

        verify(couponService).update(eq(5L), any(CouponDto.class));
    }

    @Test
    void apply_returnsDiscount() throws Exception {
        when(couponService.apply(any(CouponApplyRequest.class)))
                .thenReturn(25.5);

        mockMvc.perform(post("/api/coupons/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"SAVE10\",\"orderTotal\":100}"))
                .andExpect(status().isOk());

        verify(couponService).apply(any(CouponApplyRequest.class));
    }

    @Test
    void delete_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/coupons/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(couponService).delete(5L);
    }

    private static PageResponse<CouponDto> pageOf() {
        return PageResponse.<CouponDto>builder()
                .content(List.of(
                        CouponDto.builder()
                                .id(1L)
                                .code("SAVE10")
                                .build()
                ))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
    }
}

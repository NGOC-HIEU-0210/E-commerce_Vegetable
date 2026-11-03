package com.hieu.controller;

import com.hieu.enums.PaymentStatus;
import com.hieu.payload.dto.OrderDto;
import com.hieu.payload.response.PageResponse;
import com.hieu.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    void findAll_returnsListOfOrders() throws Exception {
        when(orderService.findAll()).thenReturn(List.of(OrderDto.builder().id(1L).orderCode("ORD1").build()));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].orderCode").value("ORD1"));
    }

    @Test
    void findPaged_returnsPageResponse() throws Exception {
        when(orderService.findPaged(any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get("/api/orders/paged").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(orderService).findPaged(any(Pageable.class));
    }

    @Test
    void search_returnsPageResponse() throws Exception {
        when(orderService.search(eq(1L), eq("john"), any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get("/api/orders/search")
                        .param("statusId", "1")
                        .param("keyword", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        verify(orderService).search(eq(1L), eq("john"), any(Pageable.class));
    }

    @Test
    void updateStatus_returnsOrder() throws Exception {
        when(orderService.updateStatus(5L, 2L)).thenReturn(OrderDto.builder().id(5L).build());

        mockMvc.perform(patch("/api/orders/5/status").param("statusId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));

        verify(orderService).updateStatus(5L, 2L);
    }

    @Test
    void delete_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/orders/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(orderService).delete(5L);
    }

    private static PageResponse<OrderDto> pageOf() {
        return PageResponse.<OrderDto>builder()
                .content(List.of(OrderDto.builder().id(1L).orderCode("ORD1").build()))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
    }
}

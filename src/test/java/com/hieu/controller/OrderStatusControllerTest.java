package com.hieu.controller;

import com.hieu.model.OrderStatus;
import com.hieu.service.OrderStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderStatusControllerTest {

    @Mock
    private OrderStatusService orderStatusService;

    @InjectMocks
    private OrderStatusController orderStatusController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderStatusController).build();
    }

    @Test
    void findAll_returnsListOfOrderStatuses() throws Exception {
        when(orderStatusService.findAll()).thenReturn(List.of(
                OrderStatus.builder().id(1L).name("PENDING").description("Cho xu ly").build(),
                OrderStatus.builder().id(2L).name("COMPLETED").description("Hoan thanh").build()));

        mockMvc.perform(get("/api/order-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("PENDING"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].name").value("COMPLETED"));
    }

    @Test
    void findById_returnsOrderStatus() throws Exception {
        when(orderStatusService.findById(2L)).thenReturn(
                OrderStatus.builder().id(2L).name("COMPLETED").description("Hoan thanh").build());

        mockMvc.perform(get("/api/order-statuses/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.name").value("COMPLETED"));
    }
}

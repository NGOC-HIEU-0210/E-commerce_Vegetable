package com.hieu.controller;

import com.hieu.payload.dto.BatchDto;
import com.hieu.payload.request.BatchRequest;
import com.hieu.payload.response.PageResponse;
import com.hieu.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BatchControllerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private BatchController batchController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(batchController).build();
    }

    @Test
    void receive_returnsBatchDto() throws Exception {
        when(inventoryService.receive(any(BatchRequest.class))).thenReturn(batchDto());

        mockMvc.perform(post("/api/admin/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":100,\"costPrice\":50000.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.productId").value(1))
                .andExpect(jsonPath("$.data.productName").value("Milk"));

        ArgumentCaptor<BatchRequest> captor = ArgumentCaptor.forClass(BatchRequest.class);
        verify(inventoryService).receive(captor.capture());
        assertEquals(1L, captor.getValue().getProductId());
        assertEquals(100, captor.getValue().getQuantity());
    }

    @Test
    void byProduct_returnsPagedBatchResponse() throws Exception {
        when(inventoryService.findByProduct(eq(1L), any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get("/api/admin/batches/product/1")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(10))
                .andExpect(jsonPath("$.data.content[0].productId").value(1))
                .andExpect(jsonPath("$.data.content[0].productName").value("Milk"))
                .andExpect(jsonPath("$.data.content[1].id").value(11))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.last").value(true));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(inventoryService).findByProduct(eq(1L), captor.capture());

        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertNotNull(pageable.getSort().getOrderFor("expiryDate"));
        assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("expiryDate").getDirection());
    }

    @Test
    void expiring_returnsPagedBatchResponse() throws Exception {
        when(inventoryService.findExpiring(eq(2), any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get("/api/admin/batches/expiring")
                        .param("days", "2")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(10))
                .andExpect(jsonPath("$.data.content[0].daysLeft").value(3))
                .andExpect(jsonPath("$.data.totalElements").value(2));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(inventoryService).findExpiring(eq(2), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
    }

    private static BatchDto batchDto() {
        return BatchDto.builder()
                .id(10L)
                .productId(1L)
                .productName("Milk")
                .initialQuantity(100)
                .remainingQuantity(60)
                .wastedQuantity(0)
                .costPrice(50000.0)
                .status("ACTIVE")
                .daysLeft(3)
                .build();
    }

    private static PageResponse<BatchDto> pageOf() {
        return PageResponse.<BatchDto>builder()
                .content(List.of(
                        batchDto(),
                        BatchDto.builder()
                                .id(11L)
                                .productId(2L)
                                .productName("Egg")
                                .initialQuantity(50)
                                .remainingQuantity(50)
                                .wastedQuantity(0)
                                .costPrice(30000.0)
                                .status("ACTIVE")
                                .daysLeft(9)
                                .build()))
                .page(0)
                .size(20)
                .totalElements(2)
                .totalPages(1)
                .last(true)
                .build();
    }
}

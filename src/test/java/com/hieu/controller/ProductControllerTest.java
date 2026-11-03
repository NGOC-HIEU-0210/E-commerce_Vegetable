
package com.hieu.controller;

import com.hieu.payload.dto.ProductDto;
import com.hieu.payload.response.PageResponse;
import com.hieu.service.ProductService;
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
import static org.mockito.ArgumentMatchers.anyLong;
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
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    void findAll_returnsPageResponse() throws Exception {
        when(productService.findPaged(any(Pageable.class)))
                .thenReturn(pageOf());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(productService).findPaged(any(Pageable.class));
    }

    @Test
    void findLatest_returnsPageResponse() throws Exception {
        when(productService.findLatest(any(Pageable.class)))
                .thenReturn(pageOf());

        mockMvc.perform(get("/api/products/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        verify(productService).findLatest(any(Pageable.class));
    }

    @Test
    void search_returnsPageResponse() throws Exception {
        when(productService.search(
                eq("milk"),
                eq(null),
                any(Pageable.class)
        )).thenReturn(pageOf());

        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "milk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        verify(productService).search(
                eq("milk"),
                eq(null),
                any(Pageable.class)
        );
    }

    @Test
    void byCategory_returnsPageResponse() throws Exception {
        when(productService.findByCategory(
                eq(3L),
                any(Pageable.class)
        )).thenReturn(pageOf());

        mockMvc.perform(get("/api/products/category/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        verify(productService).findByCategory(
                eq(3L),
                any(Pageable.class)
        );
    }

    @Test
    void findById_returnsProduct() throws Exception {
        when(productService.findById(5L))
                .thenReturn(ProductDto.builder()
                        .id(5L)
                        .name("Milk")
                        .build());

        mockMvc.perform(get("/api/products/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.name").value("Milk"));

        verify(productService).findById(5L);
    }

    @Test
    void create_returnsProduct() throws Exception {
        when(productService.create(any(ProductDto.class)))
                .thenReturn(ProductDto.builder()
                        .id(9L)
                        .name("Milk")
                        .build());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Milk\",\"price\":50000.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(9));

        verify(productService).create(any(ProductDto.class));
    }

    @Test
    void update_returnsProduct() throws Exception {
        when(productService.update(
                eq(5L),
                any(ProductDto.class)
        )).thenReturn(ProductDto.builder()
                .id(5L)
                .name("Milk2")
                .build());

        mockMvc.perform(put("/api/products/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Milk2\",\"price\":55000.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));

        verify(productService).update(
                eq(5L),
                any(ProductDto.class)
        );
    }

    @Test
    void delete_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/products/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(productService).delete(5L);
    }

    private static PageResponse<ProductDto> pageOf() {
        return PageResponse.<ProductDto>builder()
                .content(List.of(
                        ProductDto.builder()
                                .id(1L)
                                .name("Milk")
                                .build()
                ))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
    }
}

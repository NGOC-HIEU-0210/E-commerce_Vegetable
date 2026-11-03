package com.hieu.controller;

import com.hieu.payload.dto.CategoryDto;
import com.hieu.payload.response.PageResponse;
import com.hieu.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void findAll_returnsPageResponse() throws Exception {
        when(categoryService.findPaged(isNull(), any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(categoryService).findPaged(isNull(), any(Pageable.class));
    }

    @Test
    void findActive_returnsPageResponse() throws Exception {
        when(categoryService.findActivePaged(isNull(), any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get("/api/categories/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        verify(categoryService).findActivePaged(isNull(), any(Pageable.class));
    }

    @Test
    void create_returnsCategory() throws Exception {
        when(categoryService.create(any(CategoryDto.class))).thenReturn(CategoryDto.builder().id(1L).name("Thuc").build());

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Thuc\",\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(categoryService).create(any(CategoryDto.class));
    }

    @Test
    void update_returnsCategory() throws Exception {
        when(categoryService.update(eq(5L), any(CategoryDto.class))).thenReturn(CategoryDto.builder().id(5L).name("Moi").build());

        mockMvc.perform(put("/api/categories/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Moi\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));

        verify(categoryService).update(eq(5L), any(CategoryDto.class));
    }

    @Test
    void delete_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/categories/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(categoryService).delete(5L);
    }

    private static PageResponse<CategoryDto> pageOf() {
        return PageResponse.<CategoryDto>builder()
                .content(List.of(CategoryDto.builder().id(1L).name("Thuc").build()))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
    }
}
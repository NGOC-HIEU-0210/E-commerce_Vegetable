package com.hieu.controller;

import com.hieu.config.CurrentUser;
import com.hieu.payload.dto.CartDto;
import com.hieu.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
    }

    @Test
    void get_returnsCart() throws Exception {
        when(currentUser.id(nullable(Authentication.class))).thenReturn(1L);
        when(cartService.getCart(1L)).thenReturn(CartDto.builder().build());

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());

        verify(cartService).getCart(1L);
    }

    @Test
    void add_returnsCart() throws Exception {
        when(currentUser.id(nullable(Authentication.class))).thenReturn(1L);
        when(cartService.addItem(1L, 2L, 3)).thenReturn(CartDto.builder().build());

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":2,\"quantity\":3}"))
                .andExpect(status().isOk());

        verify(cartService).addItem(1L, 2L, 3);
    }

    @Test
    void setQuantity_returnsCart() throws Exception {
        when(currentUser.id(nullable(Authentication.class))).thenReturn(1L);
        when(cartService.setQuantity(1L, 2L, 5)).thenReturn(CartDto.builder().build());

        mockMvc.perform(patch("/api/cart/items/2").param("quantity", "5"))
                .andExpect(status().isOk());

        verify(cartService).setQuantity(1L, 2L, 5);
    }

    @Test
    void remove_returnsCart() throws Exception {
        when(currentUser.id(nullable(Authentication.class))).thenReturn(1L);
        when(cartService.removeItem(1L, 2L)).thenReturn(CartDto.builder().build());

        mockMvc.perform(delete("/api/cart/items/2"))
                .andExpect(status().isOk());

        verify(cartService).removeItem(1L, 2L);
    }

    @Test
    void clear_returnsSuccessMessage() throws Exception {
        when(currentUser.id(nullable(Authentication.class))).thenReturn(1L);

        mockMvc.perform(delete("/api/cart"))
                .andExpect(status().isOk());

        verify(cartService).clear(1L);
    }
}
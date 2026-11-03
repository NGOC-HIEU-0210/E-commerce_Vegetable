package com.hieu.controller;

import com.hieu.config.CurrentUser;
import com.hieu.payload.dto.FavouriteDto;
import com.hieu.service.FavouriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FavouriteControllerTest {

    @Mock
    private FavouriteService favouriteService;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private FavouriteController favouriteController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(favouriteController)
                .build();
    }

    @Test
    void findByUser_returnsListOfFavourites() throws Exception {

        when(currentUser.id(nullable(Authentication.class)))
                .thenReturn(1L);

        when(favouriteService.findByUser(1L))
                .thenReturn(List.of(
                        FavouriteDto.builder()
                                .productId(2L)
                                .build()
                ));

        mockMvc.perform(get("/api/favourites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].productId").value(2));

        verify(currentUser).id(nullable(Authentication.class));
        verify(favouriteService).findByUser(1L);
    }

    @Test
    void toggle_returnsBoolean() throws Exception {

        when(currentUser.id(nullable(Authentication.class)))
                .thenReturn(1L);

        when(favouriteService.toggle(1L, 2L))
                .thenReturn(true);

        mockMvc.perform(post("/api/favourites/toggle")
                        .param("productId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(currentUser).id(nullable(Authentication.class));
        verify(favouriteService).toggle(1L, 2L);
    }

    @Test
    void check_returnsBoolean() throws Exception {

        when(currentUser.id(nullable(Authentication.class)))
                .thenReturn(1L);

        when(favouriteService.isFavourite(1L, 2L))
                .thenReturn(true);

        mockMvc.perform(get("/api/favourites/check")
                        .param("productId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(currentUser).id(nullable(Authentication.class));
        verify(favouriteService).isFavourite(1L, 2L);
    }

    @Test
    void count_returnsLong() throws Exception {

        when(favouriteService.countByProduct(2L))
                .thenReturn(3L);

        mockMvc.perform(get("/api/favourites/count/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(3));

        verify(favouriteService).countByProduct(2L);
    }

    @Test
    void remove_returnsSuccessMessage() throws Exception {

        when(currentUser.id(nullable(Authentication.class)))
                .thenReturn(1L);

        mockMvc.perform(delete("/api/favourites")
                        .param("productId", "2"))
                .andExpect(status().isOk());

        verify(currentUser).id(nullable(Authentication.class));
        verify(favouriteService).remove(1L, 2L);
    }
}
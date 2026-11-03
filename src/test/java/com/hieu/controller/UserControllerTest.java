package com.hieu.controller;

import com.hieu.config.CurrentUser;
import com.hieu.payload.dto.UserDto;
import com.hieu.payload.request.RegisterRequest;
import com.hieu.payload.response.PageResponse;
import com.hieu.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void findPaged_returnsPageResponse() throws Exception {
        when(userService.findPaged(any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get("/api/users/paged").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).findPaged(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(5, captor.getValue().getPageSize());
    }

    @Test
    void search_returnsPageResponse() throws Exception {
        when(userService.search(eq("john"), any(Pageable.class))).thenReturn(pageOf());

        mockMvc.perform(get("/api/users/search").param("keyword", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        verify(userService).search(eq("john"), any(Pageable.class));
    }

    @Test
    void findById_returnsUser() throws Exception {
        when(userService.findById(5L)).thenReturn(UserDto.builder().id(5L).username("john").build());

        mockMvc.perform(get("/api/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.username").value("john"));
    }

    @Test
    void create_returnsUser() throws Exception {
        when(userService.create(any(RegisterRequest.class))).thenReturn(UserDto.builder().id(1L).username("john").build());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"password\":\"secret1\",\"email\":\"john@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(userService).create(captor.capture());
        assertEquals("john", captor.getValue().getUsername());
    }

    @Test
    void updateStatus_returnsUser() throws Exception {
        when(userService.updateStatus(5L, false)).thenReturn(UserDto.builder().id(5L).build());

        mockMvc.perform(patch("/api/users/5/status").param("status", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));

        verify(userService).updateStatus(5L, false);
    }

    @Test
    void delete_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/users/5"))
                .andExpect(status().isOk());

        verify(userService).delete(5L);
    }

    private static PageResponse<UserDto> pageOf() {
        return PageResponse.<UserDto>builder()
                .content(List.of(UserDto.builder().id(1L).username("john").build()))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
    }
}

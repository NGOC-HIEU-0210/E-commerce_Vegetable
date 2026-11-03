
package com.hieu.service;

import com.hieu.model.User;
import com.hieu.payload.dto.UserDto;
import com.hieu.payload.response.PageResponse;
import com.hieu.repository.UserRepository;
import com.hieu.service.UploadService;
import com.hieu.service.impl.UserServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UploadService uploadService;

    @InjectMocks
    private UserServiceImpl userService;

    // ================= FIND PAGED =================

    @Test
    void findPaged_mapsToPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(user(1L)), pageable, 1));

        PageResponse<UserDto> result = userService.findPaged(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals("john", result.getContent().get(0).getUsername());
        assertEquals(1L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        verify(userRepository).findAll(pageable);
    }

    // ================= FIND ALL =================

    @Test
    void findAll_returnsListOfUsers() {
        when(userRepository.findAll())
                .thenReturn(List.of(user(1L)));

        List<UserDto> result = userService.findAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("john", result.get(0).getUsername());

        verify(userRepository).findAll();
    }

    // ================= FIND BY ID =================

    @Test
    void findById_returnsUserDto() {
        when(userRepository.findById(5L))
                .thenReturn(Optional.of(user(5L)));

        UserDto result = userService.findById(5L);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("john", result.getUsername());

        verify(userRepository).findById(5L);
    }

    @Test
    void findById_whenMissing_throws() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> userService.findById(99L)
        );

        verify(userRepository).findById(99L);
    }

    // ================= CREATE =================

    @Test
    void create_whenUsernameExists_throws() {
        when(userRepository.existsByUsername("john"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.create(request())
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_success_savesAndReturnsUser() {
        when(userRepository.existsByUsername("john"))
                .thenReturn(false);

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("secret1"))
                .thenReturn("encoded");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User savedUser = invocation.getArgument(0);
                    savedUser.setId(1L);
                    return savedUser;
                });

        UserDto result = userService.create(request());

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john", result.getUsername());

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("secret1");
    }

    // ================= UPDATE STATUS =================

    @Test
    void updateStatus_updatesStatus() {
        when(userRepository.findById(5L))
                .thenReturn(Optional.of(user(5L)));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateStatus(5L, false);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertFalse(result.getStatus());

        verify(userRepository).findById(5L);
        verify(userRepository).save(any(User.class));
    }

    // ================= DELETE =================

    @Test
    void delete_whenMissing_throws() {
        when(userRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> userService.delete(99L)
        );

        verify(userRepository, never()).deleteById(99L);
    }

    // ================= SEARCH =================

    @Test
    void search_blankKeyword_usesFindAll() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(user(1L)), pageable, 1));

        PageResponse<UserDto> result = userService.search("   ", pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("john", result.getContent().get(0).getUsername());

        verify(userRepository).findAll(pageable);
    }

    // ================= TEST DATA =================

    private com.hieu.payload.request.RegisterRequest request() {
        com.hieu.payload.request.RegisterRequest request =
                new com.hieu.payload.request.RegisterRequest();

        request.setUsername("john");
        request.setPassword("secret1");
        request.setEmail("john@example.com");

        return request;
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .username("john")
                .email("john@example.com")
                .password("secret1")
                .build();
    }
}

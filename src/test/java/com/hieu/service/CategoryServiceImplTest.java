package com.hieu.service;

import com.hieu.model.Category;
import com.hieu.payload.dto.CategoryDto;
import com.hieu.payload.response.PageResponse;
import com.hieu.repository.CategoryRepository;
import com.hieu.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private com.hieu.service.UploadService uploadService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void findPaged_mapsToPageResponse() {
        Pageable pageable = PageRequest.of(0, 20);
        when(categoryRepository.searchAll("", pageable)).thenReturn(new PageImpl<>(List.of(category()), pageable, 1));

        PageResponse<CategoryDto> result = categoryService.findPaged(null, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals(1L, result.getTotalElements());
    }

    @Test
    void findActivePaged_mapsToPageResponse() {
        Pageable pageable = PageRequest.of(0, 20);
        when(categoryRepository.searchActive("", pageable)).thenReturn(new PageImpl<>(List.of(category()), pageable, 1));

        PageResponse<CategoryDto> result = categoryService.findActivePaged(null, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
    }

    @Test
    void findById_returnsCategory() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(Category.builder().id(5L).name("Thuc").build()));

        CategoryDto result = categoryService.findById(5L);

        assertEquals(5L, result.getId());
        assertEquals("Thuc", result.getName());
    }

    @Test
    void findById_whenMissing_throws() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> categoryService.findById(99L));
    }

    @Test
    void create_whenNameExists_throws() {
        when(categoryRepository.findByName("Thuc")).thenReturn(Optional.of(Category.builder().id(1L).name("Thuc").build()));

        assertThrows(IllegalArgumentException.class, () -> categoryService.create(CategoryDto.builder().name("Thuc").build()));

        verify(categoryRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void create_success_savesCategory() {
        when(categoryRepository.findByName("Thuc")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(com.hieu.model.Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryDto result = categoryService.create(CategoryDto.builder().name("Thuc").build());

        assertEquals("Thuc", result.getName());
        verify(categoryRepository).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void update_returnsUpdatedCategory() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(Category.builder().id(5L).name("Thuc").build()));
        when(categoryRepository.save(any(com.hieu.model.Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryDto result = categoryService.update(5L, CategoryDto.builder().name("Moi").build());

        assertEquals(5L, result.getId());
    }

    @Test
    void delete_whenMissing_throws() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> categoryService.delete(99L));
        verify(categoryRepository, org.mockito.Mockito.never()).deleteById(99L);
    }

    private com.hieu.model.Category category() {
        return com.hieu.model.Category.builder().id(1L).name("Thuc").build();
    }
}

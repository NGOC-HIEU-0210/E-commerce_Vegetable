
package com.hieu.service;

import com.hieu.enums.BatchStatus;
import com.hieu.model.Product;
import com.hieu.payload.dto.ProductDto;
import com.hieu.payload.response.PageResponse;
import com.hieu.repository.CategoryRepository;
import com.hieu.repository.ProductImageRepository;
import com.hieu.repository.ProductRepository;
import com.hieu.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private UploadService uploadService;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private ProductServiceImpl productService;

    // Tạo Product mẫu
    private Product createProduct(Long id, String name, Double price) {
        return Product.builder()
                .id(id)
                .name(name)
                .price(price)
                .active(true)
                .shelfLifeDays(7)
                .build();
    }

    // 1. Kiểm tra lấy danh sách sản phẩm có phân trang
    @Test
    void findPaged_mapsToPageResponse() {
        Pageable pageable = PageRequest.of(0, 20);
        Product product = createProduct(1L, "Milk", 50000D);

        when(productRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        when(pricingService.earliestExpiry(any()))
                .thenReturn(new HashMap<>());

        PageResponse<ProductDto> result = productService.findPaged(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals("Milk", result.getContent().get(0).getName());
        assertEquals(1L, result.getTotalElements());
    }

    // 2. Kiểm tra lấy sản phẩm theo danh mục
    @Test
    void findByCategory_returnsPageResponse() {
        Pageable pageable = PageRequest.of(0, 20);
        Product product = createProduct(2L, "Egg", 30000D);

        when(productRepository.findByCategoryIdAndActiveTrue(3L, pageable))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        when(pricingService.earliestExpiry(any()))
                .thenReturn(new HashMap<>());

        PageResponse<ProductDto> result =
                productService.findByCategory(3L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(2L, result.getContent().get(0).getId());
        assertEquals("Egg", result.getContent().get(0).getName());
    }

    // 3. Kiểm tra tìm kiếm sản phẩm
    @Test
    void search_returnsPageResponse() {
        Pageable pageable = PageRequest.of(0, 20);
        Product product = createProduct(3L, "Carrot", 20000D);

        when(productRepository.search(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        when(pricingService.earliestExpiry(any()))
                .thenReturn(new HashMap<>());

        PageResponse<ProductDto> result =
                productService.search("carrot", null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(3L, result.getContent().get(0).getId());
        assertEquals("Carrot", result.getContent().get(0).getName());
    }

    // 4. Kiểm tra lấy sản phẩm mới nhất
    @Test
    void findLatest_returnsPageResponse() {
        Pageable pageable = PageRequest.of(0, 8);
        Product product = createProduct(4L, "Lettuce", 15000D);

        when(productRepository.findLatest(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        when(pricingService.earliestExpiry(any()))
                .thenReturn(new HashMap<>());

        PageResponse<ProductDto> result =
                productService.findLatest(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(4L, result.getContent().get(0).getId());
    }

    // 5. Kiểm tra lấy sản phẩm giảm giá
    @Test
    void findRescue_returnsDiscountedProducts() {
        Product product = createProduct(5L, "Spinach", 25000D);

        when(productRepository.findWithSellableBatches(
                any(BatchStatus.class), any()))
                .thenReturn(List.of(product));

        when(pricingService.earliestExpiry(any()))
                .thenReturn(new HashMap<>());

        when(pricingService.discountPercent(any(), any()))
                .thenReturn(50);

        var result = productService.findRescue();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // 6. Kiểm tra lấy sản phẩm theo ID
    @Test
    void findById_returnsProductDto() {
        Product product = createProduct(5L, "Spinach", 25000D);

        when(productRepository.findById(5L))
                .thenReturn(Optional.of(product));

        when(pricingService.earliestExpiry(any()))
                .thenReturn(new HashMap<>());

        ProductDto result = productService.findById(5L);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Spinach", result.getName());
    }

    // 7. Kiểm tra tạo sản phẩm
    @Test
    void create_returnsProductDto() {
        ProductDto dto = ProductDto.builder()
                .name("Spinach")
                .price(25000D)
                .active(true)
                .shelfLifeDays(5)
                .build();

        // Giả lập database lưu Product và tự sinh ID
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product savedProduct = invocation.getArgument(0);
                    savedProduct.setId(1L);
                    return savedProduct;
                });

        // Giả lập không tìm thấy hạn sử dụng gần nhất
        when(pricingService.earliestExpiry(anyList()))
                .thenReturn(new HashMap<>());

        ProductDto result = productService.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Spinach", result.getName());
        assertEquals(25000D, result.getPrice());

        verify(productRepository).save(any(Product.class));
        verify(pricingService).earliestExpiry(List.of(1L));
    }
    // 8. Kiểm tra cập nhật sản phẩm
    @Test
    void update_returnsUpdatedProduct() {
        Product product = createProduct(5L, "Spinach", 25000D);

        when(productRepository.findById(5L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(pricingService.earliestExpiry(any()))
                .thenReturn(new HashMap<>());

        ProductDto updateDto = ProductDto.builder()
                .name("Spinach2")
                .price(26000D)
                .build();

        ProductDto result = productService.update(5L, updateDto);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Spinach2", result.getName());

        verify(productRepository).save(any(Product.class));
    }

    // 9. Kiểm tra xóa sản phẩm không tồn tại
    @Test
    void delete_whenMissing_throws() {
        when(productRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> productService.delete(99L)
        );

        verify(productRepository).existsById(99L);
        verify(productRepository, never()).deleteById(anyLong());
    }
}

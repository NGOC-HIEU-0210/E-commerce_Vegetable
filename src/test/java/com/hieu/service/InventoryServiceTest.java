package com.hieu.service;

import com.hieu.enums.BatchStatus;
import com.hieu.model.Product;
import com.hieu.model.ProductBatch;
import com.hieu.payload.dto.BatchDto;
import com.hieu.payload.response.PageResponse;
import com.hieu.repository.ProductBatchRepository;
import com.hieu.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductBatchRepository batchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void findExpiring_mapsActiveBatchesToPageResponse() {
        LocalDate until = PricingService.today().plusDays(2);
        Pageable pageable = PageRequest.of(0, 20);
        when(batchRepository.findExpiring(eq(BatchStatus.ACTIVE), eq(until), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(batch(10L, 1L, "Milk")), pageable, 1));

        PageResponse<BatchDto> result = inventoryService.findExpiring(2, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(10L, result.getContent().get(0).getId());
        assertEquals(1L, result.getContent().get(0).getProductId());
        assertEquals("Milk", result.getContent().get(0).getProductName());
        assertEquals(100, result.getContent().get(0).getInitialQuantity());
        assertEquals(60, result.getContent().get(0).getRemainingQuantity());
        assertEquals(1L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
        assertTrue(result.isLast());

        verify(batchRepository).findExpiring(eq(BatchStatus.ACTIVE), eq(until), eq(pageable));
    }

    @Test
    void findExpiring_whenDaysZero_usesTodayAsUntilDate() {
        Pageable pageable = PageRequest.of(0, 20);
        when(batchRepository.findExpiring(eq(BatchStatus.ACTIVE), eq(PricingService.today()), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        PageResponse<BatchDto> result = inventoryService.findExpiring(0, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findByProduct_mapsBatchesToPageResponse() {
        Pageable pageable = PageRequest.of(0, 20);
        when(batchRepository.findByProductIdOrderByExpiryDateAsc(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(batch(10L, 1L, "Milk")), pageable, 1));

        PageResponse<BatchDto> result = inventoryService.findByProduct(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(10L, result.getContent().get(0).getId());
        assertEquals(1L, result.getContent().get(0).getProductId());
        assertEquals("Milk", result.getContent().get(0).getProductName());
        assertEquals(1L, result.getTotalElements());
        assertTrue(result.getContent().get(0).getRemainingQuantity() > 0);
    }

    @Test
    void findByProduct_whenNoBatch_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(batchRepository.findByProductIdOrderByExpiryDateAsc(99L, pageable)).thenReturn(Page.empty(pageable));

        PageResponse<BatchDto> result = inventoryService.findByProduct(99L, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    private ProductBatch batch(Long id, Long productId, String productName) {
        Product product = Product.builder().id(productId).name(productName).build();
        return ProductBatch.builder()
                .id(id)
                .product(product)
                .initialQuantity(100)
                .remainingQuantity(60)
                .wastedQuantity(0)
                .costPrice(50000.0)
                .receivedDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusDays(5))
                .status(BatchStatus.ACTIVE)
                .build();
    }
}

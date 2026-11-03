
package com.hieu.service;

import com.hieu.enums.BatchStatus;
import com.hieu.model.Product;
import com.hieu.repository.ProductBatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private ProductBatchRepository batchRepository;

    @InjectMocks
    private PricingService pricingService;

    @Test
    void discountPercent_lowRatio_returnsFiftyPercent() {
        int percent = pricingService.discountPercent(
                10,
                PricingService.today().plusDays(2)
        );

        assertEquals(50, percent);
    }

    @Test
    void discountPercent_highRatio_returnsZero() {
        int percent = pricingService.discountPercent(
                10,
                PricingService.today().plusDays(9)
        );

        assertEquals(0, percent);
    }

    @Test
    void applyDiscount_returnsDiscountedPrice() {
        double price = pricingService.applyDiscount(100D, 50);

        assertEquals(50.0, price);
    }

    @Test
    void priceFor_returnsDiscountedPrice() {
        Product product = Product.builder()
                .id(1L)
                .name("Milk")
                .price(100D)
                .shelfLifeDays(10)
                .build();

        double price = pricingService.priceFor(
                product,
                PricingService.today().plusDays(2)
        );

        assertEquals(50.0, price);
    }

    @Test
    void earliestExpiry_returnsMap() {
        when(batchRepository.earliestExpiry(
                anyCollection(),
                eq(BatchStatus.ACTIVE),
                any(LocalDate.class)
        )).thenReturn(List.of());

        var result = pricingService.earliestExpiry(List.of(1L));

        assertTrue(result.isEmpty());

        verify(batchRepository).earliestExpiry(
                anyCollection(),
                eq(BatchStatus.ACTIVE),
                any(LocalDate.class)
        );
    }
}

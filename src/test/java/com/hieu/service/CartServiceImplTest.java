package com.hieu.service;

import com.hieu.payload.dto.CartDto;
import com.hieu.payload.dto.CartItemDto;
import com.hieu.model.CartItem;
import com.hieu.model.Product;
import com.hieu.model.User;
import com.hieu.repository.CartItemRepository;
import com.hieu.repository.ProductRepository;
import com.hieu.repository.UserRepository;
import com.hieu.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void getCart_returnsCartDto() {
        CartItem item = CartItem.builder()
                .id(1L)
                .user(User.builder().id(1L).username("john").build())
                .product(Product.builder().id(2L).name("Milk").price(50000D).active(true).shelfLifeDays(7).build())
                .quantity(2)
                .build();
        when(cartItemRepository.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(item));
        when(pricingService.earliestExpiry(any())).thenReturn(new HashMap<>());
        when(pricingService.priceFor(any(), any())).thenReturn(100.0);
        when(pricingService.discountPercent(any(), any())).thenReturn(10);

        CartDto result = cartService.getCart(1L);

        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getTotalQuantity());
    }

    @Test
    void addItem_quantityLessThanOne_throws() {
        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(1L, 2L, 0));
    }

    @Test
    void addItem_productNotFound_throws() {
        when(productRepository.findById(2L)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> cartService.addItem(1L, 2L, 1));
    }

    @Test
    void addItem_success_addsItem() {
        Product product = Product.builder().id(2L).name("Milk").price(50000D).active(true).shelfLifeDays(7).build();
        when(productRepository.findById(2L)).thenReturn(java.util.Optional.of(product));
        when(userRepository.getReferenceById(1L)).thenReturn(User.builder().id(1L).username("john").build());
        when(cartItemRepository.findByUserIdAndProductId(1L, 2L)).thenReturn(java.util.Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(
                CartItem.builder().id(1L).user(User.builder().id(1L).username("john").build())
                        .product(Product.builder().id(2L).name("Milk").price(50000D).active(true).shelfLifeDays(7).build())
                        .quantity(3).build()));
        when(pricingService.earliestExpiry(any())).thenReturn(new HashMap<>());
        when(pricingService.priceFor(any(), any())).thenReturn(100.0);
        when(pricingService.discountPercent(any(), any())).thenReturn(10);

        CartDto result = cartService.addItem(1L, 2L, 3);

        assertEquals(1, result.getItems().size());
    }

    @Test
    void setQuantity_moreThanZero_updatesItem() {
        CartItem item = CartItem.builder()
                .id(1L)
                .user(User.builder().id(1L).username("john").build())
                .product(Product.builder().id(2L).name("Milk").price(50000D).active(true).shelfLifeDays(7).build())
                .quantity(2)
                .build();
        when(cartItemRepository.findByUserIdAndProductId(1L, 2L)).thenReturn(java.util.Optional.of(item));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(item));
        when(pricingService.earliestExpiry(any())).thenReturn(new HashMap<>());
        when(pricingService.priceFor(any(), any())).thenReturn(100.0);
        when(pricingService.discountPercent(any(), any())).thenReturn(10);

        CartDto result = cartService.setQuantity(1L, 2L, 5);

        assertEquals(1, result.getItems().size());
    }

    @Test
    void clear_deletesAllItems() {
        CartItem item = CartItem.builder()
                .id(1L)
                .user(User.builder().id(1L).username("john").build())
                .product(Product.builder().id(2L).name("Milk").price(50000D).active(true).shelfLifeDays(7).build())
                .quantity(2)
                .build();
        when(cartItemRepository.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(item));

        cartService.clear(1L);

        verify(cartItemRepository).deleteAll(List.of(item));
    }
}

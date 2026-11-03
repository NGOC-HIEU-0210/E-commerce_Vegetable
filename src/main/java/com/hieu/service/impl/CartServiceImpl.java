package com.hieu.service.impl;

import com.hieu.model.CartItem;
import com.hieu.model.Product;
import com.hieu.payload.dto.CartDto;
import com.hieu.payload.dto.CartItemDto;
import com.hieu.repository.CartItemRepository;
import com.hieu.repository.ProductRepository;
import com.hieu.repository.UserRepository;
import com.hieu.service.PricingService;
import com.hieu.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final int MAX_QTY = 100;

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PricingService pricingService;

    @Override
    @Transactional(readOnly = true)
    public CartDto getCart(Long userId) {
        return build(userId);
    }

    @Override
    @Transactional
    public CartDto addItem(Long userId, Long productId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new IllegalArgumentException("Product unavailable: " + product.getName());
        }

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> CartItem.builder()
                        .user(userRepository.getReferenceById(userId))
                        .product(product)
                        .quantity(0)
                        .build());
        item.setQuantity(Math.min(item.getQuantity() + quantity, MAX_QTY));
        cartItemRepository.save(item);
        return build(userId);
    }

    @Override
    @Transactional
    public CartDto setQuantity(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            return removeItem(userId, productId);
        }
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("Item not in cart: " + productId));
        item.setQuantity(Math.min(quantity, MAX_QTY));
        cartItemRepository.save(item);
        return build(userId);
    }

    @Override
    @Transactional
    public CartDto removeItem(Long userId, Long productId) {
        cartItemRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(cartItemRepository::delete);
        return build(userId);
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        cartItemRepository.deleteAll(cartItemRepository.findByUserIdOrderByIdAsc(userId));
    }


    private CartDto build(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByIdAsc(userId);
        Map<Long, LocalDate> expiry = pricingService.earliestExpiry(
                cartItems.stream().map(ci -> ci.getProduct().getId()).toList());
        List<CartItemDto> items = cartItems.stream()
                .map(ci -> {
                    Product p = ci.getProduct();
                    LocalDate exp = expiry.get(p.getId());
                    double price = pricingService.priceFor(p, exp);
                    int percent = pricingService.discountPercent(p.getShelfLifeDays(), exp);
                    return CartItemDto.builder()
                            .productId(p.getId())
                            .productName(p.getName())
                            .productImage(p.getImage())
                            .price(price)
                            .originalPrice(p.getPrice() == null ? 0 : p.getPrice())
                            .discountPercent(percent)
                            .quantity(ci.getQuantity())
                            .subtotal(price * ci.getQuantity())
                            .available(Boolean.TRUE.equals(p.getActive()))
                            .build();
                })
                .toList();

        return CartDto.builder()
                .items(items)
                .totalQuantity(items.stream().mapToInt(CartItemDto::getQuantity).sum())
                .subtotal(items.stream().mapToDouble(CartItemDto::getSubtotal).sum())
                .build();
    }
}
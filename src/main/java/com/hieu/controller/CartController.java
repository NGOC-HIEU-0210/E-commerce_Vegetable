package com.hieu.controller;

import com.hieu.config.CurrentUser;
import com.hieu.payload.dto.CartDto;
import com.hieu.payload.request.CartItemRequest;
import com.hieu.payload.response.ApiResponse;
import com.hieu.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Giỏ hàng của người dùng đang đăng nhập")
public class CartController {

    private final CartService cartService;
    private final CurrentUser currentUser;

    @GetMapping
    @Operation(summary = "Xem giỏ hàng")
    public ApiResponse<CartDto> get(Authentication auth) {
        return ApiResponse.success(cartService.getCart(currentUser.id(auth)));
    }

    @PostMapping("/items")
    @Operation(summary = "Thêm sản phẩm vào giỏ (cộng dồn nếu đã có)")
    public ApiResponse<CartDto> add(Authentication auth, @Valid @RequestBody CartItemRequest request) {
        return ApiResponse.success("Added",
                cartService.addItem(currentUser.id(auth), request.getProductId(), request.getQuantity()));
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Đặt số lượng cho sản phẩm trong giỏ (<= 0 thì xóa)")
    public ApiResponse<CartDto> setQuantity(Authentication auth, @PathVariable Long productId,
                                            @RequestParam int quantity) {
        return ApiResponse.success("Updated",
                cartService.setQuantity(currentUser.id(auth), productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Xóa sản phẩm khỏi giỏ")
    public ApiResponse<CartDto> remove(Authentication auth, @PathVariable Long productId) {
        return ApiResponse.success("Removed", cartService.removeItem(currentUser.id(auth), productId));
    }

    @DeleteMapping
    @Operation(summary = "Xóa toàn bộ giỏ hàng")
    public ApiResponse<Void> clear(Authentication auth) {
        cartService.clear(currentUser.id(auth));
        return ApiResponse.success("Cleared", null);
    }
}
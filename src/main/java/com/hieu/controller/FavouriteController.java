package com.hieu.controller;

import com.hieu.config.CurrentUser;
import com.hieu.payload.dto.FavouriteDto;
import com.hieu.payload.response.ApiResponse;
import com.hieu.service.FavouriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favourites")
@RequiredArgsConstructor
@Tag(name = "Favourites", description = "Danh sách sản phẩm yêu thích")
public class FavouriteController {

    private final FavouriteService favouriteService;
    private final CurrentUser currentUser;

    @GetMapping
    @Operation(summary = "Lấy danh sách yêu thích của người dùng đang đăng nhập")
    public ApiResponse<List<FavouriteDto>> findByUser(Authentication auth) {
        return ApiResponse.success(favouriteService.findByUser(currentUser.id(auth)));
    }

    @PostMapping("/toggle")
    @Operation(summary = "Thêm/xóa sản phẩm khỏi danh sách yêu thích (trả về true nếu đã thêm)")
    public ApiResponse<Boolean> toggle(Authentication auth, @RequestParam Long productId) {
        return ApiResponse.success("Toggled", favouriteService.toggle(currentUser.id(auth), productId));
    }

    @GetMapping("/check")
    @Operation(summary = "Kiểm tra sản phẩm có trong danh sách yêu thích không")
    public ApiResponse<Boolean> check(Authentication auth, @RequestParam Long productId) {
        return ApiResponse.success(favouriteService.isFavourite(currentUser.id(auth), productId));
    }

    @GetMapping("/count/{productId}")
    @Operation(summary = "Đếm số lượt yêu thích của sản phẩm")
    public ApiResponse<Long> count(@PathVariable Long productId) {
        return ApiResponse.success(favouriteService.countByProduct(productId));
    }

    @DeleteMapping
    @Operation(summary = "Xóa sản phẩm khỏi danh sách yêu thích")
    public ApiResponse<Void> remove(Authentication auth, @RequestParam Long productId) {
        favouriteService.remove(currentUser.id(auth), productId);
        return ApiResponse.success("Removed", null);
    }
}
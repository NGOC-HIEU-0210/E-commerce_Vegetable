package com.hieu.controller;

import com.hieu.payload.dto.UserDto;
import com.hieu.payload.request.RegisterRequest;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.PageResponse;
import com.hieu.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Quản lý người dùng")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Lấy danh sách người dùng")
    public ApiResponse<List<UserDto>> findAll() {
        return ApiResponse.success(userService.findAll());
    }

    @GetMapping("/paged")
    @Operation(summary = "Lấy danh sách người dùng phân trang")
    public ApiResponse<PageResponse<UserDto>> findPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.findPaged(PageRequest.of(page, size)));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng đang đăng nhập")
    public ApiResponse<UserDto> me(@AuthenticationPrincipal com.hieu.model.User user) {
        if (user == null) {
            throw new RuntimeException("Not authenticated");
        }
        return ApiResponse.success(userService.findByUsername(user.getUsername()));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm người dùng")
    public ApiResponse<PageResponse<UserDto>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.search(keyword,
                PageRequest.of(page, size, Sort.by("id").descending())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy người dùng theo id")
    public ApiResponse<UserDto> findById(@PathVariable Long id) {
        return ApiResponse.success(userService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Tạo người dùng mới")
    public ApiResponse<UserDto> create(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("User created", userService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật người dùng")
    public ApiResponse<UserDto> update(@PathVariable Long id, @RequestBody UserDto dto) {
        return ApiResponse.success("User updated", userService.update(id, dto));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Bật/tắt trạng thái người dùng")
    public ApiResponse<UserDto> updateStatus(@PathVariable Long id, @RequestParam Boolean status) {
        return ApiResponse.success("Status updated", userService.updateStatus(id, status));
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật ảnh đại diện")
    public ApiResponse<UserDto> uploadAvatar(@PathVariable Long id,
                                             @RequestPart("file") MultipartFile file) {
        return ApiResponse.success("Avatar updated", userService.uploadAvatar(id, file));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa người dùng")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success("User deleted", null);
    }
}
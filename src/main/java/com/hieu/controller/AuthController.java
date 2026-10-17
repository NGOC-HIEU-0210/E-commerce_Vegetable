package com.hieu.controller;

import com.hieu.payload.request.ForgotPasswordRequest;
import com.hieu.payload.request.LoginRequest;
import com.hieu.payload.request.RegisterRequest;
import com.hieu.payload.request.ResetPasswordRequest;
import com.hieu.payload.response.ApiResponse;
import com.hieu.payload.response.AuthResponse;
import com.hieu.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đăng ký, đăng nhập và khôi phục mật khẩu")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập và nhận JWT token")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @PostMapping("/signup")
    @Operation(summary = "Đăng ký tài khoản mới")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Registration successful", authService.signup(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Gửi OTP khôi phục mật khẩu qua email")
    public ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.success("OTP has been sent to your email", null);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu bằng OTP")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("Password has been reset", null);
    }
}

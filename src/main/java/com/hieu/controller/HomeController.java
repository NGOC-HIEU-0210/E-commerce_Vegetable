package com.hieu.controller;

import com.hieu.payload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Home", description = "Thông tin chung về API")
public class HomeController {

    @GetMapping("/")
    @Operation(summary = "Kiểm tra API đang chạy")
    public ApiResponse<Map<String, String>> home() {
        return ApiResponse.success(Map.of(
                "application", "IND MART API",
                "status", "running",
                "swagger", "/swagger-ui.html"));
    }
}

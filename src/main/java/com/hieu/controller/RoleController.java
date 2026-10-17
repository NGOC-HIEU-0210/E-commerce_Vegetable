package com.hieu.controller;

import com.hieu.model.Role;
import com.hieu.payload.response.ApiResponse;
import com.hieu.repository.RoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Quản lý vai trò")
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    @Operation(summary = "Lấy danh sách vai trò")
    public ApiResponse<List<Role>> findAll() {
        return ApiResponse.success(roleRepository.findAll());
    }

    @PostMapping
    @Operation(summary = "Tạo vai trò mới")
    public ApiResponse<Role> create(@RequestBody Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new IllegalArgumentException("Role already exists");
        }
        return ApiResponse.success("Role created", roleRepository.save(role));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa vai trò")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleRepository.deleteById(id);
        return ApiResponse.success("Role deleted", null);
    }
}

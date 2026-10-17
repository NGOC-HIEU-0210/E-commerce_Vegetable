package com.hieu.mapper;

import com.hieu.model.Role;
import com.hieu.model.User;
import com.hieu.payload.dto.UserDto;

import java.util.List;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .mobile(user.getMobile())
                .address(user.getAddress())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createdDate(user.getCreatedDate())
                .updatedDate(user.getUpdatedDate())
                .roles(user.getRoles() == null ? List.of()
                        : user.getRoles().stream().map(Role::getName).toList())
                .build();
    }
}

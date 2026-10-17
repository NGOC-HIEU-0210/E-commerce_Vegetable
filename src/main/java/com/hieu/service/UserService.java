package com.hieu.service;

import com.hieu.payload.dto.UserDto;
import com.hieu.payload.request.RegisterRequest;
import com.hieu.payload.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    List<UserDto> findAll();

    PageResponse<UserDto> findPaged(Pageable pageable);

    UserDto findById(Long id);

    UserDto findByUsername(String username);

    UserDto create(RegisterRequest request);

    UserDto update(Long id, UserDto dto);

    UserDto updateStatus(Long id, Boolean status);

    UserDto uploadAvatar(Long id, MultipartFile file);

    void delete(Long id);

    PageResponse<UserDto> search(String keyword, Pageable pageable);
}

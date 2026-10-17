package com.hieu.service.impl;

import com.hieu.mapper.UserMapper;
import com.hieu.model.User;
import com.hieu.payload.dto.UserDto;
import com.hieu.payload.request.RegisterRequest;
import com.hieu.payload.response.PageResponse;
import com.hieu.repository.UserRepository;
import com.hieu.service.UploadService;
import com.hieu.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UploadService uploadService;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(UserMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDto> findPaged(Pageable pageable) {
        return PageResponse.of(userRepository.findAll(pageable), UserMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return UserMapper.toDto(getUser(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findByUsername(String username) {
        return UserMapper.toDto(userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username)));
    }

    @Override
    @Transactional
    public UserDto create(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .mobile(request.getMobile())
                .address(request.getAddress())
                .status(true)
                .build();
        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User user = getUser(id);
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getMobile() != null) user.setMobile(dto.getMobile());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto updateStatus(Long id, Boolean status) {
        User user = getUser(id);
        user.setStatus(status);
        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto uploadAvatar(Long id, MultipartFile file) {
        User user = getUser(id);
        String path = uploadService.save(file, "Account");
        if (path != null) {
            user.setAvatar(path);
        }
        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDto> search(String keyword, Pageable pageable) {
        Page<User> page = (keyword == null || keyword.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.search(keyword, pageable);
        return PageResponse.of(page, UserMapper::toDto);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }
}

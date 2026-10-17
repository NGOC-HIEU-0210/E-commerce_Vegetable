package com.hieu.service.impl;

import com.hieu.config.JWTProvider;
import com.hieu.mapper.UserMapper;
import com.hieu.model.Role;
import com.hieu.model.User;
import com.hieu.payload.request.ForgotPasswordRequest;
import com.hieu.payload.request.LoginRequest;
import com.hieu.payload.request.RegisterRequest;
import com.hieu.payload.request.ResetPasswordRequest;
import com.hieu.payload.response.AuthResponse;
import com.hieu.repository.RoleRepository;
import com.hieu.repository.UserRepository;
import com.hieu.service.AuthService;
import com.hieu.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTProvider jwtProvider;
    private final MailService mailService;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        if (Boolean.FALSE.equals(user.getStatus())) {
            throw new BadCredentialsException("Account is disabled");
        }

        String token = jwtProvider.generateToken(user.getUsername(),
                Map.of("roles", user.getRoles().stream().map(Role::getName).toList()));

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(UserMapper.toDto(user))
                .message("Login successful")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse signup(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_USER")
                        .description("Default customer role").build()));

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
        user.getRoles().add(userRole);
        user = userRepository.save(user);

        String token = jwtProvider.generateToken(user.getUsername(),
                Map.of("roles", user.getRoles().stream().map(Role::getName).toList()));

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(UserMapper.toDto(user))
                .message("Registration successful")
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        mailService.sendOtp(user.getEmail(), otp);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }
}

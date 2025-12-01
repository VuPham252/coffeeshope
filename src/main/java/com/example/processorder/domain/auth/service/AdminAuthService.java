package com.example.processorder.domain.auth.service;

import com.example.processorder.domain.auth.dto.AuthResponse;
import com.example.processorder.domain.auth.dto.LoginRequest;
import com.example.processorder.domain.auth.dto.RegisterRequest;
import com.example.processorder.domain.common.entity.User;
import com.example.processorder.domain.common.enums.UserRole;
import com.example.processorder.domain.common.exception.AuthenticationException;
import com.example.processorder.domain.common.exception.BusinessException;
import com.example.processorder.domain.auth.repository.UserRepository;
import com.example.processorder.domain.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse registerAdmin(RegisterRequest request) {
        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            log.warn("Admin registration failed - Mobile number already exists: {}", request.getMobileNumber());
            throw new BusinessException("Mobile number already registered");
        }

        User admin = User.builder()
                .mobileNumber(request.getMobileNumber())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ADMIN)
                .build();

        admin = userRepository.save(admin);
        log.info("Admin user created successfully with ID: {}, mobile: {}", admin.getId(), admin.getMobileNumber());

        String token = tokenProvider.generateToken(admin.getId(), admin.getMobileNumber());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .customerId(admin.getId())
                .name(admin.getName())
                .mobileNumber(admin.getMobileNumber())
                .role(admin.getRole())
                .build();
    }

    public AuthResponse loginAdmin(LoginRequest request) {

        User admin = userRepository.findByMobileNumber(request.getMobileNumber())
                .filter(u -> u.getRole() == UserRole.ADMIN)
                .orElseThrow(() -> {
                    log.warn("Admin login failed - User not found: {}", request.getMobileNumber());
                    return new AuthenticationException("Invalid credentials");
                });

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            log.warn("Admin login failed - Invalid password for mobile: {}", request.getMobileNumber());
            throw new AuthenticationException("Invalid credentials");
        }

        String token = tokenProvider.generateToken(admin.getId(), admin.getMobileNumber());
        log.info("Admin login successful - User ID: {}, mobile: {}", admin.getId(), admin.getMobileNumber());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .customerId(admin.getId())
                .name(admin.getName())
                .mobileNumber(admin.getMobileNumber())
                .role(admin.getRole())
                .build();
    }
}

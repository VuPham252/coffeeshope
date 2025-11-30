package com.example.processorder.domain.auth.controller;

import com.example.processorder.domain.common.dto.ApiResponse;
import com.example.processorder.domain.auth.dto.AuthResponse;
import com.example.processorder.domain.auth.dto.LoginRequest;
import com.example.processorder.domain.auth.dto.RegisterRequest;
import com.example.processorder.domain.auth.service.CustomerAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/auth")
public class CustomerAuthController {

    private final CustomerAuthService customerAuthService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = customerAuthService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = customerAuthService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}


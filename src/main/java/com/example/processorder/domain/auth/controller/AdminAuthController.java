package com.example.processorder.domain.auth.controller;

import com.example.processorder.domain.common.dto.ApiResponse;
import com.example.processorder.domain.auth.dto.AuthResponse;
import com.example.processorder.domain.auth.dto.LoginRequest;
import com.example.processorder.domain.auth.dto.RegisterRequest;
import com.example.processorder.domain.auth.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Admin registration successful",
                adminAuthService.registerAdmin(request)
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Admin login successful",
                adminAuthService.loginAdmin(request)
        ));
    }
}

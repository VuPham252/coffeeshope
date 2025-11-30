package com.example.processorder.domain.auth.dto;

import com.example.processorder.domain.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long customerId;
    private String name;
    private String mobileNumber;
    private Integer loyaltyScore;
    private UserRole role;
}


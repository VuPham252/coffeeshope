package com.example.processorder.domain.auth.service;

import com.example.processorder.domain.auth.dto.AuthResponse;
import com.example.processorder.domain.auth.dto.LoginRequest;
import com.example.processorder.domain.auth.dto.RegisterRequest;
import com.example.processorder.domain.common.entity.Address;
import com.example.processorder.domain.common.entity.CustomerProfile;
import com.example.processorder.domain.common.entity.User;
import com.example.processorder.domain.common.enums.UserRole;
import com.example.processorder.domain.common.exception.AuthenticationException;
import com.example.processorder.domain.common.exception.BusinessException;
import com.example.processorder.domain.auth.repository.CustomerProfileRepository;
import com.example.processorder.domain.auth.repository.UserRepository;
import com.example.processorder.domain.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerAuthService {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new BusinessException("Mobile number already registered");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }

        User user = User.builder()
                .mobileNumber(request.getMobileNumber())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .build();
        user = userRepository.save(user);

        CustomerProfile customerProfile = CustomerProfile.builder()
                .user(user)
                .totalOrders(0)
                .loyaltyScore(0)
                .build();

        Address address = Address.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .zipCode(request.getZipCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        customerProfile.addAddress(address);
        customerProfile = customerProfileRepository.save(customerProfile);

        String token = tokenProvider.generateToken(user.getId(), user.getMobileNumber());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .customerId(user.getId())
                .name(user.getName())
                .mobileNumber(user.getMobileNumber())
                .loyaltyScore(customerProfile.getLoyaltyScore())
                .build();
    }


    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByMobileNumber(request.getMobileNumber())
                .filter(u -> u.getRole() == UserRole.CUSTOMER)
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        CustomerProfile profile = customerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AuthenticationException("Profile not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getMobileNumber());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .customerId(user.getId())
                .name(user.getName())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRole())
                .loyaltyScore(profile.getLoyaltyScore())
                .build();
    }
}


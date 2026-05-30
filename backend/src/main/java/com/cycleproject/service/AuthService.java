package com.cycleproject.service;

import com.cycleproject.dto.*;
import com.cycleproject.entity.User;
import com.cycleproject.repository.UserRepository;
import com.cycleproject.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public ApiResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return new ApiResponse(false, "Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .businessName(request.getBusinessName())
                .ownerName(request.getOwnerName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .gstNumber(request.getGstNumber())
                .role(User.Role.CUSTOMER)
                .approvalStatus(User.ApprovalStatus.PENDING)
                .build();

        userRepository.save(user);
        return new ApiResponse(true, "Registration successful. Please wait for admin approval.");
    }

    public ApiResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new ApiResponse(false, "Invalid email or password");
        }

        if (user.getRole() == User.Role.CUSTOMER && user.getApprovalStatus() != User.ApprovalStatus.APPROVED) {
            return new ApiResponse(false, "Your account is pending approval from admin");
        }

        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name());
        AuthResponse authResponse = new AuthResponse(token, user.getRole().name(),
                user.getEmail(), user.getBusinessName(), user.getApprovalStatus().name());

        return new ApiResponse(true, "Login successful", authResponse);
    }
}

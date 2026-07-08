package com.decisionhub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.decisionhub.dto.request.authentication.LoginRequest;
import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.dto.response.authentication.ForgotPasswordResponse;
import com.decisionhub.dto.response.authentication.LoginResponse;
import com.decisionhub.dto.response.authentication.ProfileResponse;
import com.decisionhub.dto.response.authentication.RegisterResponse;
import com.decisionhub.service.interfaces.authentication.AuthService;
import jakarta.validation.Valid;
import com.decisionhub.dto.request.authentication.ForgotPasswordRequest;
import com.decisionhub.dto.request.authentication.ResetPasswordRequest;
import com.decisionhub.dto.response.authentication.ResetPasswordResponse;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(
                authService.register(request)
        );
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                new ForgotPasswordResponse(
                        "Password reset token generated successfully"
                )
        );
    }
   
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        return ResponseEntity.ok(
                authService.resetPassword(request)
        );
    }
    
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        return ResponseEntity.ok(authService.getProfile());
    }
}
package com.decisionhub.service.interfaces.authentication;

import com.decisionhub.dto.request.authentication.ForgotPasswordRequest;
import com.decisionhub.dto.request.authentication.LoginRequest;
import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.dto.request.authentication.ResetPasswordRequest;
import com.decisionhub.dto.response.authentication.LoginResponse;
import com.decisionhub.dto.response.authentication.ProfileResponse;
import com.decisionhub.dto.response.authentication.RegisterResponse;
import com.decisionhub.dto.response.authentication.ResetPasswordResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    ProfileResponse getProfile();

    void forgotPassword(ForgotPasswordRequest request);
    
    ResetPasswordResponse resetPassword(ResetPasswordRequest request);
}
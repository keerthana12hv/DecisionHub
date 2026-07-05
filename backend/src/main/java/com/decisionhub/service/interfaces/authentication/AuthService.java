package com.decisionhub.service.interfaces.authentication;

import com.decisionhub.dto.request.authentication.LoginRequest;
import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.dto.response.authentication.LoginResponse;
import com.decisionhub.dto.response.authentication.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
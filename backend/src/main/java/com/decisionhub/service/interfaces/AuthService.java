package com.decisionhub.service.interfaces;

import com.decisionhub.dto.request.LoginRequest;
import com.decisionhub.dto.request.RegisterRequest;
import com.decisionhub.dto.response.LoginResponse;
import com.decisionhub.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
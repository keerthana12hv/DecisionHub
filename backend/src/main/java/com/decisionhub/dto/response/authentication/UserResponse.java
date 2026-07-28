package com.decisionhub.dto.response.authentication;

import com.decisionhub.enums.authentication.PlatformRole;
import com.decisionhub.enums.authentication.UserStatus;

public record UserResponse(
        Long id,
        String username,
        String email,
        PlatformRole role,
        UserStatus status
) {}
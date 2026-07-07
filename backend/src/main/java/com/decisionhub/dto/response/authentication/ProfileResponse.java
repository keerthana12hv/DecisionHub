package com.decisionhub.dto.response.authentication;

import com.decisionhub.enums.authentication.PlatformRole;
import com.decisionhub.enums.authentication.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileResponse {

    private Long id;

    private String username;

    private String email;

    private PlatformRole role;

    private UserStatus status;
}
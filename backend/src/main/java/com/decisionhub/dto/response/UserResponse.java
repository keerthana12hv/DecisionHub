package com.decisionhub.dto.response;

import com.decisionhub.enums.authentication.PlatformRole;
import com.decisionhub.enums.authentication.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private PlatformRole role;
    private UserStatus status;
}
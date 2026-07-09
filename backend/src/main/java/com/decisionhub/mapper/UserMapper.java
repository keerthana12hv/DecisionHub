package com.decisionhub.mapper;

import com.decisionhub.dto.response.authentication.UserResponse;
import com.decisionhub.entity.authentication.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getStatus()
        );
    }
}

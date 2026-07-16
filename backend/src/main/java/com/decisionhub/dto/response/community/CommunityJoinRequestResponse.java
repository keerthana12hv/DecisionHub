package com.decisionhub.dto.response.community;

import java.time.LocalDateTime;

public record CommunityJoinRequestResponse(
    Long memberId,
    Long userId,
    String username,
    String email,
    LocalDateTime requestedAt
) {}
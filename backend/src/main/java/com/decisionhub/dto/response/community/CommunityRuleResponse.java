package com.decisionhub.dto.response.community;

import java.time.LocalDateTime;

public record CommunityRuleResponse(
    Long id,
    Long communityId,
    String title,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

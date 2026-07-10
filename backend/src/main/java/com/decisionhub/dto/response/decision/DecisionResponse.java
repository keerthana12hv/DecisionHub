package com.decisionhub.dto.response.decision;

import com.decisionhub.dto.response.authentication.UserResponse;
import com.decisionhub.enums.decision.DecisionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record DecisionResponse(
    Long id,
    String title,
    String description,
    UserResponse creator,
    String categoryName,
    String communityName,
    DecisionStatus status,
    LocalDateTime deadline,
    List<OptionResponseDto> options,
    List<ComparisonFactorResponse> factors,
    LocalDateTime createdAt
) {}

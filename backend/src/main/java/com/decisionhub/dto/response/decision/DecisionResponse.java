package com.decisionhub.dto.response.decision;

import com.decisionhub.dto.response.authentication.UserResponse;
import com.decisionhub.enums.decision.DecisionStatus;

import com.decisionhub.enums.decision.VotingType;

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
    VotingType votingType,
    LocalDateTime votingEndTime,
    List<OptionResponseDto> options,
    List<ComparisonFactorResponse> factors,
    LocalDateTime createdAt
) {
    @Deprecated
    public DecisionResponse(
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
    ) {
        this(id, title, description, creator, categoryName, communityName, status, deadline,
             VotingType.RATING_BASED, deadline != null ? deadline.minusHours(2) : null, options, factors, createdAt);
    }
}

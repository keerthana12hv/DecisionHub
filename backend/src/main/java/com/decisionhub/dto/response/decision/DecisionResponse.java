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
    LocalDateTime createdAt,
    boolean pinned,
    boolean locked
) {
    // 13-argument version from HEAD branch (with votingType, votingEndTime; defaults pinned=false, locked=false)
    public DecisionResponse(
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
        this(id, title, description, creator, categoryName, communityName, status, deadline,
             votingType, votingEndTime, options, factors, createdAt, false, false);
    }

    // 13-argument version from backend branch (with pinned, locked; defaults votingType=RATING_BASED, votingEndTime=calculated)
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
        LocalDateTime createdAt,
        boolean pinned,
        boolean locked
    ) {
        this(id, title, description, creator, categoryName, communityName, status, deadline,
             VotingType.RATING_BASED, deadline != null ? deadline.minusHours(2) : null, options, factors, createdAt, pinned, locked);
    }

    // 11-argument deprecated constructor (without votingType, votingEndTime, pinned, locked)
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
             VotingType.RATING_BASED, deadline != null ? deadline.minusHours(2) : null, options, factors, createdAt, false, false);
    }
}

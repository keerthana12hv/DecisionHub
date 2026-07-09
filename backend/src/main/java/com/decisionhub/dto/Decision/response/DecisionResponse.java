package com.decisionhub.dto;

import com.decisionhub.entity.AnonymityType;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.entity.VotingType;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record DecisionResponse(
    UUID id,
    String title,
    String description,
    UserSummaryDto creator,
    String categoryName,
    String communityName,
    VotingType votingType,
    AnonymityType anonymityType,
    DecisionStatus status,
    Instant deadline,
    Long version,
    List<OptionResponseDto> options,
    List<PollResponse> polls,
    List<ComparisonFactorResponse> factors,
    Set<String> tags,
    Instant createdAt
) {}

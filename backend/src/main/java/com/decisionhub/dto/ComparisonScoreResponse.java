package com.decisionhub.dto;

import java.time.Instant;
public record ComparisonScoreResponse(
    Long optionId,
    Long factorId,
    Long userId,
    int score,
    String remarks,
    Instant createdAt,
    Instant updatedAt
) {}

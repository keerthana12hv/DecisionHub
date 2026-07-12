package com.decisionhub.dto.response.decision;

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

package com.decisionhub.dto;

import java.time.Instant;
import java.util.UUID;

public record ComparisonScoreResponse(
    UUID optionId,
    UUID factorId,
    UUID userId,
    int score,
    String remarks,
    Instant createdAt,
    Instant updatedAt
) {}

package com.decisionhub.dto.response.decision;

import java.time.Instant;
public record ComparisonFactorResponse(
    Long id,
    Long decisionId,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt,
    Long version
) {}

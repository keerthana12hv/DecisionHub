package com.decisionhub.dto;

import java.time.Instant;
import java.util.UUID;

public record ComparisonFactorResponse(
    UUID id,
    UUID decisionId,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt,
    Long version
) {}

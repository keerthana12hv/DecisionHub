package com.decisionhub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ComparisonScoreRequest(
    @NotNull(message = "Option ID is required")
    Long optionId,

    @NotNull(message = "Factor ID is required")
    Long factorId,

    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score cannot exceed 100")
    int score,

    String remarks
) {}

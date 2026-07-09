package com.decisionhub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CriteriaDto(
    @NotBlank(message = "Criterion name is required")
    String criterionName,

    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score cannot exceed 100")
    int score,

    String remarks
) {}

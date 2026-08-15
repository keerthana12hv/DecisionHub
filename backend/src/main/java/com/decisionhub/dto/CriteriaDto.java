package com.decisionhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body containing details for option scoring criteria")
public record CriteriaDto(
    @NotBlank(message = "Criterion name is required")
    @Schema(description = "The name of the scoring criterion.", example = "Speed", requiredMode = Schema.RequiredMode.REQUIRED)
    String criterionName,

    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score cannot exceed 100")
    @Schema(description = "The score value (must be between 0 and 100 inclusive).", example = "85", requiredMode = Schema.RequiredMode.REQUIRED)
    int score,

    @Schema(description = "Optional remarks explaining the score evaluation.", example = "Highly optimized bundle sizes.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String remarks
) {}

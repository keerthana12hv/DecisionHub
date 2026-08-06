package com.decisionhub.dto.request.decision;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body containing details for creating a comparison factor")
public record ComparisonFactorRequest(
    @NotBlank(message = "Factor name is required")
    @Size(max = 100, message = "Factor name cannot exceed 100 characters")
    @Schema(description = "The name of the comparison factor. Must not be blank and cannot exceed 100 characters.", example = "Performance", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,
    
    @Schema(description = "Description explaining the criteria of this comparison factor.", example = "Speed, responsiveness, and memory footprint.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String description
) {}

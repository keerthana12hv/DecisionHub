package com.decisionhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComparisonFactorRequest(
    @NotBlank(message = "Factor name is required")
    @Size(max = 100, message = "Factor name cannot exceed 100 characters")
    String name,
    
    String description
) {}

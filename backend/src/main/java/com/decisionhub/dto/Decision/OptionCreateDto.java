package com.decisionhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OptionCreateDto(
    @NotBlank(message = "Option title is required")
    @Size(max = 150, message = "Option title must be less than 150 characters")
    String title,

    String description,

    List<CriteriaDto> criteria
) {}

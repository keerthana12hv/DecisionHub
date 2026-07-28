package com.decisionhub.dto.request.decision;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

import com.decisionhub.dto.CriteriaDto;

public record OptionCreateDto(
    @NotBlank(message = "Option title is required")
    @Size(max = 150, message = "Option title must be less than 150 characters")
    @JsonAlias("text")
    String title,

    String description,

    List<CriteriaDto> criteria
) {}

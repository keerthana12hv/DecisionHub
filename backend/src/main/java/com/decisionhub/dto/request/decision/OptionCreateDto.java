package com.decisionhub.dto.request.decision;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

import com.decisionhub.dto.CriteriaDto;

@Schema(description = "Request body containing details for creating a decision option")
public record OptionCreateDto(
    @NotBlank(message = "Option title is required")
    @Size(max = 150, message = "Option title must be less than 150 characters")
    @JsonAlias("text")
    @Schema(description = "The title of the decision option. Must not be blank and cannot exceed 150 characters.", example = "React Framework", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(description = "Detailed description of the option.", example = "A component-based library for building user interfaces.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String description,

    @Schema(description = "Optional criteria list associated with this option.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    List<CriteriaDto> criteria
) {}

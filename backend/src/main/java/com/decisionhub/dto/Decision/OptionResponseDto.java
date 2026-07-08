package com.decisionhub.dto;

import java.util.List;
import java.util.UUID;

public record OptionResponseDto(
    UUID id,
    String title,
    String description,
    @Deprecated
    List<CriteriaDto> criteria,
    List<ComparisonScoreResponse> comparisonScores
) {}

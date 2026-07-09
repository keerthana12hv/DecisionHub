package com.decisionhub.dto;

import java.util.List;

public record OptionResponseDto(
    Long id,
    String title,
    String description,
    List<ComparisonScoreResponse> comparisonScores
) {}

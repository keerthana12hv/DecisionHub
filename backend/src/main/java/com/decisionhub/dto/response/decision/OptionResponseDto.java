package com.decisionhub.dto.response.decision;

import java.util.List;

public record OptionResponseDto(
    Long id,
    String title,
    String description,
    List<ComparisonScoreResponse> comparisonScores,
    long voteCount
) {
    // Backward-compat constructor for existing mapper calls
    public OptionResponseDto(Long id, String title, String description,
                              List<ComparisonScoreResponse> comparisonScores) {
        this(id, title, description, comparisonScores, 0L);
    }
}

package com.decisionhub.dto;

import java.util.List;
import java.util.UUID;

public record OptionRankingDto(
    UUID optionId,
    String optionTitle,
    int rank,
    double score,
    List<FactorScoreDto> factorBreakdown,
    boolean isTied
) {}

package com.decisionhub.dto;

import java.util.List;

public record OptionRankingDto(
    Long optionId,
    String optionTitle,
    int rank,
    double score,
    List<FactorScoreDto> factorBreakdown,
    boolean isTied
) {}

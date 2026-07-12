package com.decisionhub.dto.response.decision;

public record OptionSummaryRankingDto(
    Long optionId,
    String optionTitle,
    int rank,
    double score
) {}

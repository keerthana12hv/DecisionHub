package com.decisionhub.dto;

public record OptionSummaryRankingDto(
    Long optionId,
    String optionTitle,
    int rank,
    double score
) {}

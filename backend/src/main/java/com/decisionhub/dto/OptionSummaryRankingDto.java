package com.decisionhub.dto;

import java.util.UUID;

public record OptionSummaryRankingDto(
    UUID optionId,
    String optionTitle,
    int rank,
    double score
) {}

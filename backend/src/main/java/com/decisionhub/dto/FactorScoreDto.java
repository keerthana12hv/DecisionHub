package com.decisionhub.dto;

public record FactorScoreDto(
    Long factorId,
    String factorName,
    double averageScore,
    double weight,
    double weightedScore
) {}

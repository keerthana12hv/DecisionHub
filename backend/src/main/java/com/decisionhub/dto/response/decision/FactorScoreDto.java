package com.decisionhub.dto.response.decision;

public record FactorScoreDto(
    Long factorId,
    String factorName,
    double averageScore,
    double weight,
    double weightedScore
) {}

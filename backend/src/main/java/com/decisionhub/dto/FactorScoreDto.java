package com.decisionhub.dto;

import java.util.UUID;

public record FactorScoreDto(
    UUID factorId,
    String factorName,
    double averageScore,
    double weight,
    double weightedScore
) {}

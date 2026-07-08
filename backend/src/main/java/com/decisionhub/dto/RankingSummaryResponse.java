package com.decisionhub.dto;

import com.decisionhub.entity.DecisionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RankingSummaryResponse(
    UUID decisionId,
    String decisionTitle,
    DecisionStatus status,
    Instant rankingTimestamp,
    List<OptionSummaryRankingDto> options
) {}

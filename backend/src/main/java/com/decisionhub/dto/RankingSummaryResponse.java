package com.decisionhub.dto;

import com.decisionhub.enums.decision.DecisionStatus;
import java.time.Instant;
import java.util.List;

public record RankingSummaryResponse(
    Long decisionId,
    String decisionTitle,
    DecisionStatus status,
    Instant rankingTimestamp,
    List<OptionSummaryRankingDto> options
) {}

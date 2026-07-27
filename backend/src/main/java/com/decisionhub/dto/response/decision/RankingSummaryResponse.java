package com.decisionhub.dto.response.decision;

import java.time.Instant;
import java.util.List;

import com.decisionhub.enums.decision.DecisionStatus;

public record RankingSummaryResponse(
    Long decisionId,
    String decisionTitle,
    DecisionStatus status,
    Instant rankingTimestamp,
    List<OptionSummaryRankingDto> options
) {}

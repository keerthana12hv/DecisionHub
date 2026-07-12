package com.decisionhub.dto.response.decision;

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

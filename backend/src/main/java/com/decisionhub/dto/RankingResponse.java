package com.decisionhub.dto;

import com.decisionhub.enums.decision.DecisionStatus;
import java.time.Instant;
import java.util.List;

public record RankingResponse(
    Long decisionId,
    String decisionTitle,
    DecisionStatus status,
    Instant rankingTimestamp,
    List<OptionRankingDto> options
) {}

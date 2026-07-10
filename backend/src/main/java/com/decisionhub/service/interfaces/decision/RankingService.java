package com.decisionhub.service.interfaces.decision;

import com.decisionhub.dto.response.decision.RankingResponse;
import com.decisionhub.dto.response.decision.RankingSummaryResponse;

/**
 * Service interface for generating collaborative ranking reports.
 */
public interface RankingService {
    RankingResponse getRanking(Long decisionId);
    RankingSummaryResponse getRankingSummary(Long decisionId);
}

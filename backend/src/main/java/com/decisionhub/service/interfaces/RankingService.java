package com.decisionhub.service;

import com.decisionhub.dto.RankingResponse;
import com.decisionhub.dto.RankingSummaryResponse;

/**
 * Service interface for generating collaborative ranking reports.
 */
public interface RankingService {
    RankingResponse getRanking(Long decisionId);
    RankingSummaryResponse getRankingSummary(Long decisionId);
}

package com.decisionhub.service;

import com.decisionhub.dto.RankingResponse;
import com.decisionhub.dto.RankingSummaryResponse;
import java.util.UUID;

public interface RankingService {
    RankingResponse getRanking(UUID decisionId);
    RankingSummaryResponse getRankingSummary(UUID decisionId);
}

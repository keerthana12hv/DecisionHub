package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminDecisionStatisticsResponse {

    private long totalDecisions;
    private long draftDecisions;
    private long activeDecisions;
    private long closedDecisions;
    private long publicDecisions;
    private long communityDecisions;

}
package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDecisionStatisticsResponse {

    private long totalDecisions;

    private long activeDecisions;

    private long closedDecisions;

}

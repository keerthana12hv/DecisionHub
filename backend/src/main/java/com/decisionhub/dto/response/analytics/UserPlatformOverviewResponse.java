package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPlatformOverviewResponse {

    private Long totalVotes;

    private Long activeDecisions;

    private Double participationRate;

    private String mostPopularDecision;

}

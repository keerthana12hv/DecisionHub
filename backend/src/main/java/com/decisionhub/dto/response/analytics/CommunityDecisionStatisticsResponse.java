package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommunityDecisionStatisticsResponse {

    private Long communityId;

    private String communityName;

    private Long totalDecisions;

    private Long activeDecisions;

    private Long closedDecisions;

    private Long otherDecisions;

}
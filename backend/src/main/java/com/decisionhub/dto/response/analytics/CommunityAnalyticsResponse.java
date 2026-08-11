package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityAnalyticsResponse {

    private Long totalCommunities;

    private Long publicCommunities;

    private Long privateCommunities;

    private String mostActiveCommunity;

}
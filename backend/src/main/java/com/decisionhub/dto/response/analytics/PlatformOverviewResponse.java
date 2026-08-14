package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformOverviewResponse {

    private Long totalUsers;

    private Long totalCommunities;

    private Long totalDecisions;

    private Long totalVotes;

    private Long totalComments;

    private Long totalReplies;

}
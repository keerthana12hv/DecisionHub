package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityModerationAnalyticsResponse {

    private Long lockedDiscussions;

    private Long unlockedDiscussions;

    private Long pinnedDecisions;

    private Long reportedComments;

    private Long removedComments;

}
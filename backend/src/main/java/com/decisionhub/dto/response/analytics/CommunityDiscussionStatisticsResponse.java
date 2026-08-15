package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommunityDiscussionStatisticsResponse {

    private Long communityId;

    private String communityName;

    private Long totalComments;

    private Long totalReplies;

    private Long totalDiscussions;

}
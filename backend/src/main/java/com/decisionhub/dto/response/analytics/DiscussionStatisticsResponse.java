package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiscussionStatisticsResponse {

    private Long totalComments;

    private Long totalReplies;

    private Long totalDiscussionMessages;
}
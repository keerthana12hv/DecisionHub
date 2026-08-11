package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlatformDiscussionAnalyticsResponse {

    private Long totalComments;

    private Long totalReplies;

    private Double averageCommentsPerDecision;

    private String mostActiveDecision;

}
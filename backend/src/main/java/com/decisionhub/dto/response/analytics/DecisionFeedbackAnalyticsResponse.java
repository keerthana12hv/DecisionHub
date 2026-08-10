package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionFeedbackAnalyticsResponse {

    private Double averageRating;

    private Long feedbackCount;

    private Long fiveStar;

    private Long fourStar;

    private Long threeStar;

    private Long twoStar;

    private Long oneStar;

}
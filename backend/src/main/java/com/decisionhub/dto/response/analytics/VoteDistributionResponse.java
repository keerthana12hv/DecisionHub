package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteDistributionResponse {

    private Long optionId;

    private String optionName;

    private Long voteCount;

    private Double percentage;
}
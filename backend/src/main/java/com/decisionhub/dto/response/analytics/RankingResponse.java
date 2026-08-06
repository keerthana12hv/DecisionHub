package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RankingResponse {

    private Integer rank;

    private Long optionId;

    private String optionName;

    private Long voteCount;

    private Double percentage;
}
package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteStatisticsResponse {

    private Long totalVotes;

    private Long totalParticipants;

    private Double votePercentage;

    private Long numberOfOptions;

}
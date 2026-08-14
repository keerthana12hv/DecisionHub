package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommunityVotingStatisticsResponse {

    private Long communityId;

    private String communityName;

    private Long totalPolls;

    private Long openPolls;

    private Long closedPolls;

    private Long totalVotes;

    private Double averageVotesPerPoll;

}
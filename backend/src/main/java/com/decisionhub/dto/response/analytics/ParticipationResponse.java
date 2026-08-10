package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationResponse {

    private Long eligibleUsers;

    private Long usersVoted;

    private Long usersNotVoted;

    private Double participationPercentage;

}
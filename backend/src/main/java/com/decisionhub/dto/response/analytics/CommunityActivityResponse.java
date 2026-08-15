package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityActivityResponse {

    private List<MemberActivityResponse> mostActiveMembers;

    private List<MemberActivityResponse> highestParticipationMembers;

    private List<MemberActivityResponse> topDecisionCreators;

}
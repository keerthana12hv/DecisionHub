package com.decisionhub.service.interfaces.community;

import com.decisionhub.dto.request.community.CommunityRuleRequest;
import com.decisionhub.dto.response.community.CommunityRuleResponse;

import java.util.List;

public interface CommunityRuleService {
    CommunityRuleResponse createRule(Long communityId, CommunityRuleRequest request);
    CommunityRuleResponse updateRule(Long ruleId, CommunityRuleRequest request);
    void deleteRule(Long ruleId);
    List<CommunityRuleResponse> getRulesByCommunity(Long communityId);
}

package com.decisionhub.mapper.community;

import com.decisionhub.dto.response.community.CommunityRuleResponse;
import com.decisionhub.entity.community.CommunityRule;

public final class CommunityRuleMapper {

    private CommunityRuleMapper() {}

    public static CommunityRuleResponse toResponse(CommunityRule rule) {
        return new CommunityRuleResponse(
            rule.getId(),
            rule.getCommunity().getId(),
            rule.getTitle(),
            rule.getDescription(),
            rule.getCreatedAt(),
            rule.getUpdatedAt()
        );
    }
}

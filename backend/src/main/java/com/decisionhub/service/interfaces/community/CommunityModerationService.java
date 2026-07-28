package com.decisionhub.service.interfaces.community;

import com.decisionhub.dto.response.decision.DecisionResponse;

public interface CommunityModerationService {
    DecisionResponse pinDecision(Long decisionId);
    DecisionResponse unpinDecision(Long decisionId);
    DecisionResponse lockDiscussion(Long decisionId);
    DecisionResponse unlockDiscussion(Long decisionId);
}

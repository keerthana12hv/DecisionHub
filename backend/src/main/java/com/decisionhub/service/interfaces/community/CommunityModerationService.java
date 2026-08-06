package com.decisionhub.service.interfaces.community;

import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.dto.response.discussion.CommentResponse;
import java.util.Optional;

public interface CommunityModerationService {
    DecisionResponse pinDecision(Long decisionId);
    DecisionResponse unpinDecision(Long decisionId);
    DecisionResponse lockDiscussion(Long decisionId);
    DecisionResponse unlockDiscussion(Long decisionId);

    CommentResponse deleteComment(Long commentId);
    CommentResponse pinComment(Long commentId);
    CommentResponse unpinComment(Long commentId);
    Optional<CommentResponse> getPinnedComment(Long decisionId);
}

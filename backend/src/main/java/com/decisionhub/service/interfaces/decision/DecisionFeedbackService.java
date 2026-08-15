package com.decisionhub.service.interfaces.decision;

import java.util.List;

import com.decisionhub.dto.request.decision.DecisionFeedbackRequest;
import com.decisionhub.dto.response.decision.DecisionFeedbackResponse;

public interface DecisionFeedbackService {

    /**
     * Submit feedback for a closed decision.
     *
     * @param decisionId the decision ID
     * @param request feedback request
     * @param ipAddress the client IP for audit logging
     * @param userAgent the client User-Agent for audit logging
     * @return submitted feedback
     */
    DecisionFeedbackResponse submitFeedback(
            Long decisionId, 
            DecisionFeedbackRequest request, 
            String ipAddress, 
            String userAgent
    );

    /**
     * Get feedback for a specific decision. (Owner only)
     *
     * @param decisionId the decision ID
     * @return feedback details
     */
    DecisionFeedbackResponse getFeedbackByDecision(Long decisionId);

    /**
     * Get all decision feedbacks.
     * (Admin only)
     *
     * @return list of all feedbacks
     */
    List<DecisionFeedbackResponse> getAllFeedback();
}
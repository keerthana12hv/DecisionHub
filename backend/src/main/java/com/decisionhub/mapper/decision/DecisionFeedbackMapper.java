package com.decisionhub.mapper.decision;

import org.springframework.stereotype.Component;

import com.decisionhub.dto.request.decision.DecisionFeedbackRequest;
import com.decisionhub.dto.response.decision.DecisionFeedbackResponse;
import com.decisionhub.entity.decision.DecisionFeedback;

@Component
public class DecisionFeedbackMapper {

    public DecisionFeedback toEntity(DecisionFeedbackRequest request) {
        if (request == null) {
            return null;
        }

        DecisionFeedback feedback = new DecisionFeedback();
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());

        return feedback;
    }

    public DecisionFeedbackResponse toResponse(DecisionFeedback feedback) {
        if (feedback == null) {
            return null;
        }

        DecisionFeedbackResponse response = new DecisionFeedbackResponse();
        response.setId(feedback.getId());
        response.setDecisionId(feedback.getDecision().getId());
        response.setRating(feedback.getRating());
        response.setComment(feedback.getComment());
        response.setCreatedAt(feedback.getCreatedAt());

        return response;
    }
}
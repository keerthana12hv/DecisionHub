package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when a comparison score is created, updated, or deleted.
 * Act as an extension point for future modules like the Ranking Engine.
 */
public class ComparisonScoreChangedEvent extends ApplicationEvent {
    private final Long decisionId;
    private final Long userId;
    private final String action;

    public ComparisonScoreChangedEvent(Object source, Long decisionId, Long userId, String action) {
        super(source);
        this.decisionId = decisionId;
        this.userId = userId;
        this.action = action;
    }

    public Long getDecisionId() {
        return decisionId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }
}

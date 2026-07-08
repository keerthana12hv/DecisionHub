package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Event published when a comparison score is created, updated, or deleted.
 * Act as an extension point for future modules like the Ranking Engine.
 */
public class ComparisonScoreChangedEvent extends ApplicationEvent {
    private final UUID decisionId;
    private final UUID userId;
    private final String action;

    public ComparisonScoreChangedEvent(Object source, UUID decisionId, UUID userId, String action) {
        super(source);
        this.decisionId = decisionId;
        this.userId = userId;
        this.action = action;
    }

    public UUID getDecisionId() {
        return decisionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }
}

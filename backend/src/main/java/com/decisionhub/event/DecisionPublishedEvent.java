package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when a decision is published (DRAFT -> ACTIVE).
 * Acts as an integration hook for automatic Poll creation.
 */
public class DecisionPublishedEvent extends ApplicationEvent {
    private final Long decisionId;

    public DecisionPublishedEvent(Object source, Long decisionId) {
        super(source);
        this.decisionId = decisionId;
    }

    public Long getDecisionId() {
        return decisionId;
    }
}

package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when a decision is closed (ACTIVE -> CLOSED).
 * Acts as an integration hook for notifications (e.g. POLL_CLOSED).
 */
public class DecisionClosedEvent extends ApplicationEvent {
    private final Long decisionId;
    private String decisionTitle;
    private Long communityId;
    private String communityName;

    public DecisionClosedEvent(Object source, Long decisionId) {
        super(source);
        this.decisionId = decisionId;
    }

    public DecisionClosedEvent(Object source, Long decisionId, String decisionTitle, Long communityId, String communityName) {
        super(source);
        this.decisionId = decisionId;
        this.decisionTitle = decisionTitle;
        this.communityId = communityId;
        this.communityName = communityName;
    }

    public Long getDecisionId() {
        return decisionId;
    }

    public String getDecisionTitle() {
        return decisionTitle;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public String getCommunityName() {
        return communityName;
    }
}
package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when a decision is published (DRAFT -> ACTIVE).
 * Acts as an integration hook for automatic Poll creation and notifications.
 */
public class DecisionPublishedEvent extends ApplicationEvent {
    private final Long decisionId;
    private String decisionTitle;
    private Long communityId;
    private String communityName;

    public DecisionPublishedEvent(Object source, Long decisionId) {
        super(source);
        this.decisionId = decisionId;
    }

    public DecisionPublishedEvent(Object source, Long decisionId, String decisionTitle, Long communityId, String communityName) {
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

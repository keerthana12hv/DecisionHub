package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;

public class DecisionUnlockedEvent extends ApplicationEvent {
    private final Long decisionId;
    private final String decisionTitle;
    private final Long moderatorId;

    public DecisionUnlockedEvent(Object source, Long decisionId, String decisionTitle, Long moderatorId) {
        super(source);
        this.decisionId = decisionId;
        this.decisionTitle = decisionTitle;
        this.moderatorId = moderatorId;
    }

    public Long getDecisionId() { return decisionId; }
    public String getDecisionTitle() { return decisionTitle; }
    public Long getModeratorId() { return moderatorId; }
}

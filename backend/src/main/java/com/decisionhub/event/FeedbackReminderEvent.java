package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;

public class FeedbackReminderEvent extends ApplicationEvent {
    private final Long decisionId;
    private final String decisionTitle;
    private final Long recipientId;

    public FeedbackReminderEvent(Object source, Long decisionId, String decisionTitle, Long recipientId) {
        super(source);
        this.decisionId = decisionId;
        this.decisionTitle = decisionTitle;
        this.recipientId = recipientId;
    }

    public Long getDecisionId() { return decisionId; }
    public String getDecisionTitle() { return decisionTitle; }
    public Long getRecipientId() { return recipientId; }
}

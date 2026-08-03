package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;

public class PollClosedEvent extends ApplicationEvent {
    private final Long pollId;
    private final Long decisionId;
    private final String decisionTitle;

    public PollClosedEvent(Object source, Long pollId, Long decisionId, String decisionTitle) {
        super(source);
        this.pollId = pollId;
        this.decisionId = decisionId;
        this.decisionTitle = decisionTitle;
    }

    public Long getPollId() { return pollId; }
    public Long getDecisionId() { return decisionId; }
    public String getDecisionTitle() { return decisionTitle; }
}

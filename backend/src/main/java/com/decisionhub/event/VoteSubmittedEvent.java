package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;

public class VoteSubmittedEvent extends ApplicationEvent {
    private final Long decisionId;
    private final String decisionTitle;
    private final Long voterId;
    private final String voterUsername;

    public VoteSubmittedEvent(Object source, Long decisionId, String decisionTitle, Long voterId, String voterUsername) {
        super(source);
        this.decisionId = decisionId;
        this.decisionTitle = decisionTitle;
        this.voterId = voterId;
        this.voterUsername = voterUsername;
    }

    public Long getDecisionId() { return decisionId; }
    public String getDecisionTitle() { return decisionTitle; }
    public Long getVoterId() { return voterId; }
    public String getVoterUsername() { return voterUsername; }
}

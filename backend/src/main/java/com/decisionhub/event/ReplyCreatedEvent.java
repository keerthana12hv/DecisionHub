package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;

public class ReplyCreatedEvent extends ApplicationEvent {
    private final Long replyId;
    private final Long parentCommentId;
    private final Long decisionId;
    private final String decisionTitle;
    private final Long replierId;
    private final String replierUsername;

    public ReplyCreatedEvent(Object source, Long replyId, Long parentCommentId, Long decisionId, String decisionTitle, Long replierId, String replierUsername) {
        super(source);
        this.replyId = replyId;
        this.parentCommentId = parentCommentId;
        this.decisionId = decisionId;
        this.decisionTitle = decisionTitle;
        this.replierId = replierId;
        this.replierUsername = replierUsername;
    }

    public Long getReplyId() { return replyId; }
    public Long getParentCommentId() { return parentCommentId; }
    public Long getDecisionId() { return decisionId; }
    public String getDecisionTitle() { return decisionTitle; }
    public Long getReplierId() { return replierId; }
    public String getReplierUsername() { return replierUsername; }
}

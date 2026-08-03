package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;

public class CommentCreatedEvent extends ApplicationEvent {
    private final Long commentId;
    private final Long decisionId;
    private final String decisionTitle;
    private final Long commenterId;
    private final String commenterUsername;

    public CommentCreatedEvent(Object source, Long commentId, Long decisionId, String decisionTitle, Long commenterId, String commenterUsername) {
        super(source);
        this.commentId = commentId;
        this.decisionId = decisionId;
        this.decisionTitle = decisionTitle;
        this.commenterId = commenterId;
        this.commenterUsername = commenterUsername;
    }

    public Long getCommentId() { return commentId; }
    public Long getDecisionId() { return decisionId; }
    public String getDecisionTitle() { return decisionTitle; }
    public Long getCommenterId() { return commenterId; }
    public String getCommenterUsername() { return commenterUsername; }
}

package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;

public class CommentRemovedEvent extends ApplicationEvent {
    private final Long commentId;
    private final Long decisionId;
    private final String decisionTitle;
    private final Long moderatorId;
    private final String contentSnippet;

    public CommentRemovedEvent(Object source, Long commentId, Long decisionId, String decisionTitle, Long moderatorId, String contentSnippet) {
        super(source);
        this.commentId = commentId;
        this.decisionId = decisionId;
        this.decisionTitle = decisionTitle;
        this.moderatorId = moderatorId;
        this.contentSnippet = contentSnippet;
    }

    public Long getCommentId() { return commentId; }
    public Long getDecisionId() { return decisionId; }
    public String getDecisionTitle() { return decisionTitle; }
    public Long getModeratorId() { return moderatorId; }
    public String getContentSnippet() { return contentSnippet; }
}

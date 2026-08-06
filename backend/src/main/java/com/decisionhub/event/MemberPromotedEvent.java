package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;

public class MemberPromotedEvent extends ApplicationEvent {
    private final Long communityId;
    private final String communityName;
    private final Long userId;
    private final String newRole;

    public MemberPromotedEvent(Object source, Long communityId, String communityName, Long userId, String newRole) {
        super(source);
        this.communityId = communityId;
        this.communityName = communityName;
        this.userId = userId;
        this.newRole = newRole;
    }

    public Long getCommunityId() { return communityId; }
    public String getCommunityName() { return communityName; }
    public Long getUserId() { return userId; }
    public String getNewRole() { return newRole; }
}

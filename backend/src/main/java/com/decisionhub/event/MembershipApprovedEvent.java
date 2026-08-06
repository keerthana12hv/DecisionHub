package com.decisionhub.event;

import org.springframework.context.ApplicationEvent;

public class MembershipApprovedEvent extends ApplicationEvent {
    private final Long communityId;
    private final String communityName;
    private final Long userId;

    public MembershipApprovedEvent(Object source, Long communityId, String communityName, Long userId) {
        super(source);
        this.communityId = communityId;
        this.communityName = communityName;
        this.userId = userId;
    }

    public Long getCommunityId() { return communityId; }
    public String getCommunityName() { return communityName; }
    public Long getUserId() { return userId; }
}

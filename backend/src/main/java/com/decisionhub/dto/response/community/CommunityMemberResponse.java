package com.decisionhub.dto.response.community;

import java.time.LocalDateTime;
import com.decisionhub.enums.community.CommunityMemberRole;
import com.decisionhub.enums.community.MembershipStatus;

public record CommunityMemberResponse(
    Long memberId,
    Long userId,
    String username,
    String email,
    CommunityMemberRole role,
    MembershipStatus status,
    LocalDateTime joinedAt
) {}
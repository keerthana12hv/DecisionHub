package com.decisionhub.mapper.community;

import com.decisionhub.dto.response.community.CommunityResponse;
import com.decisionhub.entity.community.Community;

public final class CommunityMapper {

    private CommunityMapper() {
    }

    public static CommunityResponse toResponse(Community community, Boolean isMember) {

        return new CommunityResponse(
                community.getId(),
                community.getName(),
                community.getSlug(),
                community.getDescription(),
                community.getCategory().getId(),
                community.getCategory().getName(),
                community.getOwner().getId(),
                community.getOwner().getUsername(),
                community.getVisibility(),
                community.getMemberCount(), // 👈 Added comma here
                isMember
        );
    }
}
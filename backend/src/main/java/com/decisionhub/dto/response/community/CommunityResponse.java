package com.decisionhub.dto.response.community;

import com.decisionhub.enums.community.CommunityVisibility;

public record CommunityResponse(
        Long id,
        String name,
        String slug,
        String description,
        Long categoryId,
        String categoryName,
        Long ownerId,
        String ownerUsername,
        CommunityVisibility visibility,
        Integer memberCount
) {}
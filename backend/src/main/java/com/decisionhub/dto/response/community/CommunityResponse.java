package com.decisionhub.dto.response.community;

import com.decisionhub.enums.community.CommunityVisibility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommunityResponse {

    private Long id;

    private String name;

    private String slug;

    private String description;

    private Long categoryId;

    private String categoryName;

    private Long ownerId;

    private String ownerUsername;

    private CommunityVisibility visibility;

    private Integer memberCount;

}
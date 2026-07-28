package com.decisionhub.dto.request.community;

import com.decisionhub.enums.community.CommunityVisibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCommunityRequest(
        
        @NotBlank(message = "Community name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Slug is required")
        @Size(max = 100)
        String slug,

        @Size(max = 500)
        String description,

        @NotNull(message = "Category is required")
        Long categoryId,

        @NotNull(message = "Visibility is required")
        CommunityVisibility visibility
) {}
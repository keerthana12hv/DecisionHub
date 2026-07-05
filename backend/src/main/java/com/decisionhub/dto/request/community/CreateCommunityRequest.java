package com.decisionhub.dto.request.community;

import com.decisionhub.enums.community.CommunityVisibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCommunityRequest {

    @NotBlank(message = "Community name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 100)
    private String slug;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Visibility is required")
    private CommunityVisibility visibility;

}
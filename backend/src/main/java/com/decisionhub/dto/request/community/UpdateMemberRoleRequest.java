package com.decisionhub.dto.request.community;

import com.decisionhub.enums.community.CommunityMemberRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
    @NotNull(message = "Role is required")
    CommunityMemberRole role
) {}

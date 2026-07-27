package com.decisionhub.dto.request.decision;

import com.decisionhub.enums.decision.AnonymityType;
import com.decisionhub.enums.decision.VotingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Schema(description = "Request body containing details for creating or updating a decision")
public record DecisionRequest(
    @NotBlank(message = "Decision title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    @Schema(description = "The title of the decision. Must not be blank and cannot exceed 255 characters.", example = "Select Project Framework", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(description = "Detailed description of the decision context.", example = "Decide on which frontend framework to use for our next milestone.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String description,

    @Schema(description = "Optional ID of the community this decision belongs to. If null, visibility defaults to PUBLIC; if set, visibility defaults to COMMUNITY.", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Long communityId,

    @Schema(description = "Reserved for future invite-only implementation; currently ignored by the backend.", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    boolean isPublic,

    @NotNull(message = "Voting type is required")
    @Schema(description = "The type of voting mechanism to use.", example = "RATING_BASED", requiredMode = Schema.RequiredMode.REQUIRED)
    VotingType votingType,

    @Schema(description = "The anonymity level for voters. Reserved for future voting integration; currently ignored by the backend.", example = "PUBLIC", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    AnonymityType anonymityType,

    @NotNull(message = "Voting end time is required")
    @Schema(description = "The timestamp when voting ends. Must be in the future and strictly before the decision deadline.", example = "2026-08-01T12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime votingEndTime,

    @NotNull(message = "Decision deadline is required")
    @Schema(description = "The final deadline for the decision. Must be in the future and strictly after the voting end time.", example = "2026-08-02T12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime deadline,

    @Schema(description = "Optional tags to label this decision.", example = "[\"tech\", \"frontend\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Set<String> tags,

    @Schema(description = "List of decision options. At least 2 options are required before publishing the decision.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    List<@Valid OptionCreateDto> options,

    @Schema(description = "List of comparison factors (required only when votingType is RATING_BASED). Must contain at least 1 factor before publishing.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    List<ComparisonFactorRequest> factors
) {
    // Overloaded constructor to preserve backward compatibility (ignores categoryId)
    @Deprecated
    public DecisionRequest(
        String title,
        String description,
        Long categoryId,
        Long communityId,
        boolean isPublic,
        VotingType votingType,
        AnonymityType anonymityType,
        LocalDateTime deadline,
        Set<String> tags,
        List<OptionCreateDto> options,
        List<ComparisonFactorRequest> factors
    ) {
        this(title, description, communityId, isPublic,
             votingType != null ? votingType : VotingType.RATING_BASED,
             anonymityType,
             deadline != null ? deadline.minusHours(2) : LocalDateTime.now().plusDays(1).minusHours(2),
             deadline != null ? deadline : LocalDateTime.now().plusDays(1),
             tags, options, factors);
    }
}

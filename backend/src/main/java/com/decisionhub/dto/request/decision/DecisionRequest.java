package com.decisionhub.dto.request.decision;

import com.decisionhub.enums.decision.AnonymityType;
import com.decisionhub.enums.decision.VotingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record DecisionRequest(
    @NotBlank(message = "Decision title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    String title,

    String description,

    Long communityId,

    boolean isPublic,

    VotingType votingType,

    AnonymityType anonymityType,

    LocalDateTime deadline,

    Set<String> tags,

    List<@Valid OptionCreateDto> options,

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
        this(title, description, communityId, isPublic, votingType, anonymityType, deadline, tags, options, factors);
    }
}

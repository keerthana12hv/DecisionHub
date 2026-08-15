package com.decisionhub.enums.decision;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Specifies the voting algorithm and method utilized to determine decision outcome options.",
    example = "RATING_BASED"
)
public enum VotingType {
    @Schema(description = "Voters select exactly one option")
    SINGLE_CHOICE,

    @Schema(description = "Voters can select multiple options")
    MULTIPLE_CHOICE,

    @Schema(description = "Voters rate each option across multiple comparison factors (1 to 5 scale)")
    RATING_BASED
}

package com.decisionhub.dto.request.support;

import com.decisionhub.enums.support.SupportTicketType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportRequest {

    @NotNull(message = "Support ticket type is required.")
    private SupportTicketType type;

    private String subject;

    private String description;

    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating cannot be more than 5.")
    private Integer rating;

}
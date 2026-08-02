package com.decisionhub.dto.request.support;

import com.decisionhub.enums.support.SupportTicketStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSupportStatusRequest {

    @NotNull(message = "Status is required.")
    private SupportTicketStatus status;

}
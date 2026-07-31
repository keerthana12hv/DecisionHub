package com.decisionhub.dto.response.support;

import java.time.LocalDateTime;

import com.decisionhub.enums.support.SupportTicketStatus;
import com.decisionhub.enums.support.SupportTicketType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportResponse {

    private Long id;

    private Long userId;

    private String userName;

    private SupportTicketType type;

    private String subject;

    private String description;

    private Integer rating;

    private SupportTicketStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
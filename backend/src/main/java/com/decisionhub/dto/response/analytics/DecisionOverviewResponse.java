package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionOverviewResponse {

    private String decisionTitle;

    private String decisionStatus;

    private String pollStatus;

    private LocalDateTime votingEndTime;

    private LocalDateTime decisionDeadline;

}
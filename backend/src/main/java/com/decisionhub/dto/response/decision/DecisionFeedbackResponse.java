package com.decisionhub.dto.response.decision;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecisionFeedbackResponse {

    private Long id;

    private Long decisionId;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;
}
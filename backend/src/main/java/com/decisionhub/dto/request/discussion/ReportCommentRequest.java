package com.decisionhub.dto.request.discussion;

import jakarta.validation.constraints.NotBlank;

public record ReportCommentRequest(
        @NotBlank(message = "Report reason is required.")
        String reason
) {
}

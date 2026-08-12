package com.decisionhub.dto.response.discussion;

import java.time.LocalDateTime;

public record CommentReportResponse(
        Long id,
        Long commentId,
        Long reporterId,
        String reporterUsername,
        String reason,
        LocalDateTime createdAt
) {
}

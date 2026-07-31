package com.decisionhub.dto.response.discussion;

import java.time.LocalDateTime;

public record CommentResponse(
    Long id,
    String content,
    Long userId,
    String username,
    Long decisionId,
    Long parentCommentId,
    LocalDateTime createdAt,
    boolean pinned
) {}

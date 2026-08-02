package com.decisionhub.dto.response.discussion;

import java.time.LocalDateTime;

/**
 * Response DTO representing a single discussion comment.
 *
 * Replies are loaded lazily through a separate endpoint.
 */
public record CommentResponse(

        Long id,

        Long decisionId,

        Long parentCommentId,

        Long userId,

        String username,

        String content,

        boolean deleted,

        int depth,

        int replyCount,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {}

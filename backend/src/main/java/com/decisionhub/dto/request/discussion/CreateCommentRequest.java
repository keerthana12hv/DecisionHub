package com.decisionhub.dto.request.discussion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO used to create a new top-level comment
 * for a Decision.
 */
public record CreateCommentRequest(

        @NotBlank(message = "Comment content is required")
        @Size(
                max = 2000,
                message = "Comment cannot exceed 2000 characters"
        )
        String content

) {}
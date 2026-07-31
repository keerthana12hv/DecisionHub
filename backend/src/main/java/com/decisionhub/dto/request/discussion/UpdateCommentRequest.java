package com.decisionhub.dto.request.discussion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO used to update an existing comment.
 */
public record UpdateCommentRequest(

        @NotBlank(message = "Comment content is required")
        @Size(
                max = 2000,
                message = "Comment cannot exceed 2000 characters"
        )
        String content

) {}
package com.decisionhub.mapper.discussion;

import com.decisionhub.dto.response.discussion.CommentResponse;
import com.decisionhub.entity.discussion.Comment;

import org.springframework.stereotype.Component;

/**
 * Mapper responsible for converting Comment entities
 * into CommentResponse DTOs.
 *
 * Replies are loaded lazily through a separate endpoint,
 * therefore recursive mapping is intentionally avoided.
 */
@Component
public class CommentMapper {

    /**
     * Converts a Comment entity into a CommentResponse.
     */
    public CommentResponse toResponse(Comment comment) {

        if (comment == null) {
            return null;
        }

        return new CommentResponse(

                comment.getId(),

                comment.getDecision().getId(),

                comment.getParentComment() == null
                        ? null
                        : comment.getParentComment().getId(),

                comment.getUser().getId(),

                comment.getUser().getUsername(),

                comment.getDeletedAt() == null
                        ? comment.getContent()
                        : "[deleted]",

                comment.getDeletedAt() != null,

                comment.getDepth(),

                comment.getReplies() == null
                        ? 0
                        : comment.getReplies().size(),

                comment.isPinned(),

                comment.getCreatedAt(),

                comment.getUpdatedAt()
        );
    }
}
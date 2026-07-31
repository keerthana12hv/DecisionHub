package com.decisionhub.controller.discussion;

import com.decisionhub.dto.request.discussion.CreateCommentRequest;
import com.decisionhub.dto.request.discussion.UpdateCommentRequest;
import com.decisionhub.dto.response.discussion.CommentResponse;
import com.decisionhub.service.interfaces.discussion.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing endpoints for managing
 * discussions on Decisions.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Discussion",
        description = "Endpoints for managing comments and replies on Decisions"
)
public class CommentController {

    private final CommentService commentService;

    /**
     * Creates a new top-level comment for a Decision.
     */
    @PostMapping("/decisions/{decisionId}/comments")
    @Operation(
            summary = "Create comment",
            description = "Creates a new top-level comment for a Decision.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long decisionId,
            @Valid @RequestBody CreateCommentRequest request
    ) {

        log.info(
                "REST request to create comment for decision ID: {}",
                decisionId
        );

        CommentResponse response =
                commentService.createComment(
                        decisionId,
                        request
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Creates a reply to an existing comment.
     */
    @PostMapping("/decisions/{decisionId}/comments/{parentCommentId}/replies")
    @Operation(
            summary = "Reply to comment",
            description = "Creates a reply to an existing discussion comment.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CommentResponse> replyToComment(
            @PathVariable Long decisionId,
            @PathVariable Long parentCommentId,
            @Valid @RequestBody CreateCommentRequest request
    ) {

        log.info(
                "REST request to reply to comment ID: {}",
                parentCommentId
        );

        CommentResponse response =
                commentService.replyToComment(
                        decisionId,
                        parentCommentId,
                        request
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Updates an existing comment.
     */
    @PutMapping("/comments/{commentId}")
    @Operation(
            summary = "Update comment",
            description = "Updates an existing discussion comment.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request
    ) {

        log.info(
                "REST request to update comment ID: {}",
                commentId
        );

        CommentResponse response =
                commentService.updateComment(
                        commentId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Soft deletes a comment.
     */
    @DeleteMapping("/comments/{commentId}")
    @Operation(
            summary = "Delete comment",
            description = "Soft deletes a discussion comment while preserving replies.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId
    ) {

        log.info(
                "REST request to delete comment ID: {}",
                commentId
        );

        commentService.deleteComment(commentId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all top-level comments for a Decision.
     */
    @GetMapping("/decisions/{decisionId}/comments")
    @Operation(
            summary = "Get comments",
            description = "Retrieves all top-level discussion comments for a Decision.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long decisionId
    ) {

        log.info(
                "REST request to retrieve comments for decision ID: {}",
                decisionId
        );

        List<CommentResponse> response =
                commentService.getCommentsByDecision(
                        decisionId
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the direct replies of a comment.
     */
    @GetMapping("/comments/{commentId}/replies")
    @Operation(
            summary = "Get replies",
            description = "Retrieves the direct replies of a discussion comment.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<CommentResponse>> getReplies(
            @PathVariable Long commentId
    ) {

        log.info(
                "REST request to retrieve replies for comment ID: {}",
                commentId
        );

        List<CommentResponse> response =
                commentService.getReplies(
                        commentId
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single discussion comment.
     */
    @GetMapping("/comments/{commentId}")
    @Operation(
            summary = "Get comment",
            description = "Retrieves a discussion comment by its ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable Long commentId
    ) {

        log.info(
                "REST request to retrieve comment ID: {}",
                commentId
        );

        CommentResponse response =
                commentService.getComment(
                        commentId
                );

        return ResponseEntity.ok(response);
    }
}

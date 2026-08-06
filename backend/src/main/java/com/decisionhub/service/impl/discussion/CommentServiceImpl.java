package com.decisionhub.service.impl.discussion;

import com.decisionhub.dto.request.discussion.CreateCommentRequest;
import com.decisionhub.dto.request.discussion.UpdateCommentRequest;
import com.decisionhub.dto.response.discussion.CommentResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.discussion.Comment;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.discussion.CommentMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.discussion.CommentRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.security.decision.DecisionAuthorizationService;
import com.decisionhub.service.interfaces.discussion.CommentService;
import com.decisionhub.validator.decision.DecisionModificationValidator;
import com.decisionhub.validator.discussion.CommentValidator;
import com.decisionhub.event.CommentCreatedEvent;
import com.decisionhub.event.ReplyCreatedEvent;
import com.decisionhub.event.CommentRemovedEvent;
import org.springframework.context.ApplicationEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private static final String DELETED_COMMENT_TEXT = "[deleted]";

    private final CommentRepository commentRepository;
    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;

    private final CommentMapper commentMapper;

    private final AuthenticationFacade authenticationFacade;
    private final DecisionAuthorizationService decisionAuthorizationService;

    private final CommentValidator commentValidator;
    private final DecisionModificationValidator decisionModificationValidator;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    @Transactional
    public CommentResponse createComment(
            Long decisionId,
            CreateCommentRequest request
    ) {
        log.info(
                "Creating comment for decision ID: {}",
                decisionId
        );

        User currentUser = getCurrentUserOrThrow();

        Decision decision = getDecisionOrThrow(decisionId);

        // Verify user is allowed to comment.
        if (!decisionAuthorizationService.canComment(
                decisionId,
                currentUser.getId()
        )) {
            throw new UnauthorizedActionException(
                    "Not authorized to comment on this decision"
            );
        }

        // Ensure the Decision currently accepts comments.
        decisionModificationValidator.validateCommentAllowed(
                decisionId
        );

        Comment comment = new Comment();

        comment.setContent(request.content().trim());
        comment.setUser(currentUser);
        comment.setDecision(decision);
        comment.setParentComment(null);
        comment.setDepth(0);

        Comment savedComment = commentRepository.save(comment);

        log.info(
                "Comment created successfully with ID: {}",
                savedComment.getId()
        );

        if (eventPublisher != null) {
            eventPublisher.publishEvent(new CommentCreatedEvent(
                    this,
                    savedComment.getId(),
                    decision.getId(),
                    decision.getTitle(),
                    currentUser.getId(),
                    currentUser.getUsername()
            ));
        }

        return commentMapper.toResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse replyToComment(
            Long decisionId,
            Long parentCommentId,
            CreateCommentRequest request
    ) {
        log.info(
                "Creating reply for parent comment ID: {} on decision ID: {}",
                parentCommentId,
                decisionId
        );

        User currentUser = getCurrentUserOrThrow();

        Decision decision = getDecisionOrThrow(decisionId);

        Comment parentComment = getCommentOrThrow(parentCommentId);

        // Ensure the parent comment belongs to the same Decision.
        if (!parentComment.getDecision().getId().equals(decisionId)) {
            throw new ResourceNotFoundException(
                    "Parent comment does not belong to the specified decision"
            );
        }

        // Verify user is allowed to comment.
        if (!decisionAuthorizationService.canComment(
                decisionId,
                currentUser.getId()
        )) {
            throw new UnauthorizedActionException(
                    "Not authorized to comment on this decision"
            );
        }

        // Ensure the Decision currently accepts comments.
        decisionModificationValidator.validateCommentAllowed(
                decisionId
        );

        commentValidator.validateReplyAllowed(
                parentComment
        );

        commentValidator.validateReplyDepth(
                parentComment
        );

        Comment reply = new Comment();

        reply.setContent(request.content().trim());
        reply.setUser(currentUser);
        reply.setDecision(decision);
        reply.setParentComment(parentComment);
        reply.setDepth(parentComment.getDepth() + 1);

        Comment savedReply = commentRepository.save(reply);

        log.info(
                "Reply created successfully with ID: {}",
                savedReply.getId()
        );

        if (eventPublisher != null) {
            eventPublisher.publishEvent(new ReplyCreatedEvent(
                    this,
                    savedReply.getId(),
                    parentComment.getId(),
                    decision.getId(),
                    decision.getTitle(),
                    currentUser.getId(),
                    currentUser.getUsername()
            ));
        }

        return commentMapper.toResponse(savedReply);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(
            Long commentId,
            UpdateCommentRequest request
    ) {
        log.info(
                "Updating comment ID: {}",
                commentId
        );

        Long currentUserId = getCurrentUserIdOrThrow();

        Comment comment = getCommentOrThrow(commentId);

        // Verify ownership.
        if (!decisionAuthorizationService.canEditComment(
                commentId,
                currentUserId
        )) {
            throw new UnauthorizedActionException(
                    "Not authorized to edit this comment"
            );
        }

        // Ensure the Decision currently accepts comments.
        decisionModificationValidator.validateCommentAllowed(
                comment.getDecision().getId()
        );

        // Discussion-specific validation.
        commentValidator.validateEditable(comment);

        comment.setContent(request.content().trim());

        Comment updatedComment = commentRepository.save(comment);

        log.info(
                "Comment updated successfully with ID: {}",
                updatedComment.getId()
        );

        return commentMapper.toResponse(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        log.info(
                "Deleting comment ID: {}",
                commentId
        );

        Long currentUserId = getCurrentUserIdOrThrow();

        Comment comment = getCommentOrThrow(commentId);

        // Verify permission.
        if (!decisionAuthorizationService.canDeleteComment(
                commentId,
                currentUserId
        )) {
            throw new UnauthorizedActionException(
                    "Not authorized to delete this comment"
            );
        }

        // Already deleted?
        if (comment.getDeletedAt() != null) {
            return;
        }

        // Soft delete.
        comment.setDeletedAt(java.time.LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        log.info(
                "Comment soft deleted successfully with ID: {}",
                commentId
        );

        String contentSnippet = savedComment.getContent() != null ? 
                savedComment.getContent().substring(0, Math.min(30, savedComment.getContent().length())) : "";

        if (eventPublisher != null) {
            eventPublisher.publishEvent(new CommentRemovedEvent(
                    this,
                    savedComment.getId(),
                    savedComment.getDecision().getId(),
                    savedComment.getDecision().getTitle(),
                    currentUserId,
                    contentSnippet
            ));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByDecision(
            Long decisionId
    ) {
        log.info(
                "Retrieving top-level comments for decision ID: {}",
                decisionId
        );

        Long currentUserId = authenticationFacade
                .getCurrentUserId()
                .orElse(null);

        // Verify the user is allowed to view the Decision.
        if (!decisionAuthorizationService.canViewDecision(
                decisionId,
                currentUserId
        )) {
            throw new UnauthorizedActionException(
                    "Not authorized to view comments for this decision"
            );
        }

        List<Comment> comments = commentRepository.findByDecisionIdAndParentCommentIsNullOrderByCreatedAtAsc(decisionId);
        for (Comment c : comments) {
            log.info("Inspecting comment ID: {}, content: {}, deletedAt: {}, repliesSize: {}",
                     c.getId(), c.getContent(), c.getDeletedAt(), c.getReplies() != null ? c.getReplies().size() : 0);
        }
        return comments.stream()
                .filter(c -> c.getDeletedAt() == null || (c.getReplies() != null && !c.getReplies().isEmpty()))
                .map(commentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getReplies(
            Long parentCommentId
    ) {
        log.info(
                "Retrieving replies for parent comment ID: {}",
                parentCommentId
        );

        Comment parentComment = getCommentOrThrow(parentCommentId);

        Long currentUserId = authenticationFacade
                .getCurrentUserId()
                .orElse(null);

        // Verify the user is allowed to view the parent Decision.
        if (!decisionAuthorizationService.canViewDecision(
                parentComment.getDecision().getId(),
                currentUserId
        )) {
            throw new UnauthorizedActionException(
                    "Not authorized to view replies for this comment"
            );
        }

        return commentRepository
                .findByParentCommentIdOrderByCreatedAtAsc(parentCommentId)
                .stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getComment(
            Long commentId
    ) {
        log.info(
                "Retrieving comment ID: {}",
                commentId
        );

        Comment comment = getCommentOrThrow(commentId);

        Long currentUserId = authenticationFacade
                .getCurrentUserId()
                .orElse(null);

        // Verify the user is allowed to view the parent Decision.
        if (!decisionAuthorizationService.canViewDecision(
                comment.getDecision().getId(),
                currentUserId
        )) {
            throw new UnauthorizedActionException(
                    "Not authorized to view this comment"
            );
        }

        return commentMapper.toResponse(comment);
    }

    private Long getCurrentUserIdOrThrow() {

        return authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException(
                        "User is not authenticated"
                ));
    }

    private User getCurrentUserOrThrow() {

        Long currentUserId = getCurrentUserIdOrThrow();

        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + currentUserId
                ));
    }

    private Decision getDecisionOrThrow(Long decisionId) {

        return decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Decision not found with ID: " + decisionId
                ));
    }

    private Comment getCommentOrThrow(Long commentId) {

        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comment not found with ID: " + commentId
                ));
    }
}
package com.decisionhub.service.impl.community;

import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.dto.response.discussion.CommentResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.discussion.Comment;
import com.decisionhub.enums.authentication.PlatformRole;
import com.decisionhub.enums.community.CommunityMemberRole;
import com.decisionhub.enums.community.MembershipStatus;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.decision.DecisionMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.community.CommunityMemberRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.discussion.CommentRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.service.interfaces.community.CommunityModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommunityModerationServiceImpl implements CommunityModerationService {

    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final AuthenticationFacade authenticationFacade;
    private final DecisionMapper decisionMapper;
    private final CommentRepository commentRepository;
    private final CommunityMemberRepository communityMemberRepository;

    @Override
    public DecisionResponse pinDecision(Long decisionId) {
        log.info("Pinning decision: {}", decisionId);
        Decision decision = getAndValidateDecision(decisionId);
        decision.setPinned(true);
        Decision saved = decisionRepository.save(decision);
        return decisionMapper.toResponse(saved);
    }

    @Override
    public DecisionResponse unpinDecision(Long decisionId) {
        log.info("Unpinning decision: {}", decisionId);
        Decision decision = getAndValidateDecision(decisionId);
        decision.setPinned(false);
        Decision saved = decisionRepository.save(decision);
        return decisionMapper.toResponse(saved);
    }

    @Override
    public DecisionResponse lockDiscussion(Long decisionId) {
        log.info("Locking discussion on decision: {}", decisionId);
        Decision decision = getAndValidateDecision(decisionId);
        decision.setLocked(true);
        Decision saved = decisionRepository.save(decision);
        return decisionMapper.toResponse(saved);
    }

    @Override
    public DecisionResponse unlockDiscussion(Long decisionId) {
        log.info("Unlocking discussion on decision: {}", decisionId);
        Decision decision = getAndValidateDecision(decisionId);
        decision.setLocked(false);
        Decision saved = decisionRepository.save(decision);
        return decisionMapper.toResponse(saved);
    }

    @Override
    public CommentResponse deleteComment(Long commentId) {
        log.info("Moderator soft-deleting comment: {}", commentId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        authorizeCommentModeration(comment);

        comment.setDeletedAt(LocalDateTime.now());
        Comment saved = commentRepository.save(comment);
        return toCommentResponse(saved);
    }

    @Override
    public CommentResponse pinComment(Long commentId) {
        log.info("Moderator pinning comment: {}", commentId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        authorizeCommentModeration(comment);

        // Enforce single pinned comment rule: unpin other comments on this decision
        Optional<Comment> currentPinned = commentRepository
                .findFirstByDecisionIdAndPinnedTrueAndDeletedAtIsNull(comment.getDecision().getId());
        if (currentPinned.isPresent() && !currentPinned.get().getId().equals(commentId)) {
            Comment existing = currentPinned.get();
            existing.setPinned(false);
            commentRepository.save(existing);
        }

        comment.setPinned(true);
        Comment saved = commentRepository.save(comment);
        return toCommentResponse(saved);
    }

    @Override
    public CommentResponse unpinComment(Long commentId) {
        log.info("Moderator unpinning comment: {}", commentId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        authorizeCommentModeration(comment);

        comment.setPinned(false);
        Comment saved = commentRepository.save(comment);
        return toCommentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommentResponse> getPinnedComment(Long decisionId) {
        log.info("Fetching pinned comment for decision: {}", decisionId);
        return commentRepository.findFirstByDecisionIdAndPinnedTrueAndDeletedAtIsNull(decisionId)
                .map(this::toCommentResponse);
    }

    private void authorizeCommentModeration(Comment comment) {
        if (comment == null || comment.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Comment not found");
        }
        User currentUser = authenticationFacade.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedActionException("User must be authenticated"));

        // 1. Platform Admin has universal access
        if (currentUser.getRole() == PlatformRole.ADMIN) {
            return;
        }

        Community community = comment.getDecision().getCommunity();
        if (community == null) {
            throw new UnauthorizedActionException("Not authorized to moderate this comment");
        }

        // 2. Community Owner has access
        if (community.getOwner().getId().equals(currentUser.getId())) {
            return;
        }

        // 3. Approved Community Moderator has access
        boolean isModerator = communityMemberRepository.findByCommunityIdAndUserId(community.getId(), currentUser.getId())
                .map(m -> m.getStatus() == MembershipStatus.APPROVED && m.getRole() == CommunityMemberRole.MODERATOR)
                .orElse(false);

        if (!isModerator) {
            throw new UnauthorizedActionException("Not authorized to moderate this comment");
        }
    }

    private CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getDecision().getId(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getCreatedAt(),
                comment.isPinned()
        );
    }

    private Decision getAndValidateDecision(Long decisionId) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with ID: " + decisionId));

        Community community = decision.getCommunity();
        if (community == null) {
            throw new BadRequestException("Decision does not belong to any community");
        }

        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        Long currentUserId = authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User must be authenticated"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Platform Admin has universal access
        if (currentUser.getRole() == PlatformRole.ADMIN) {
            return decision;
        }

        // Community Owner has access
        if (community.getOwner().getId().equals(currentUser.getId())) {
            return decision;
        }

        // Approved Community Moderator has access
        boolean isModerator = communityMemberRepository.findByCommunityIdAndUserId(community.getId(), currentUser.getId())
                .map(m -> m.getStatus() == MembershipStatus.APPROVED && m.getRole() == CommunityMemberRole.MODERATOR)
                .orElse(false);

        if (!isModerator) {
            throw new UnauthorizedActionException("Only the community moderator can perform this action");
        }

        return decision;
    }
}

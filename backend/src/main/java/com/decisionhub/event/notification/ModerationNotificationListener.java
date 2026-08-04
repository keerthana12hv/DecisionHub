package com.decisionhub.event.notification;

import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.discussion.Comment;
import com.decisionhub.enums.notification.NotificationType;
import com.decisionhub.enums.notification.ReferenceType;
import com.decisionhub.event.*;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.discussion.CommentRepository;
import com.decisionhub.service.interfaces.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModerationNotificationListener {

    private final NotificationService notificationService;
    private final DecisionRepository decisionRepository;
    private final CommentRepository commentRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCommentRemoved(CommentRemovedEvent event) {
        log.info("Handling CommentRemovedEvent for comment ID: {}", event.getCommentId());

        Comment comment = commentRepository.findById(event.getCommentId()).orElse(null);
        if (comment == null) {
            log.warn("Comment not found with ID: {}", event.getCommentId());
            return;
        }

        Long recipientId = comment.getUser().getId();
        if (recipientId.equals(event.getModeratorId())) {
            // Self-moderation check (e.g. moderator deletes their own comment)
            return;
        }

        String title = "Comment Removed";
        String message = String.format("Your comment starting with '%s' on decision '%s' was deleted by a moderator.", 
                event.getContentSnippet(), event.getDecisionTitle());
        String actionUrl = "/decisions/" + event.getDecisionId();

        notificationService.createNotification(
                recipientId,
                title,
                message,
                NotificationType.COMMENT_REMOVED,
                ReferenceType.DECISION,
                event.getDecisionId(),
                actionUrl
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDecisionLocked(DecisionLockedEvent event) {
        log.info("Handling DecisionLockedEvent for decision ID: {}", event.getDecisionId());

        Decision decision = decisionRepository.findById(event.getDecisionId()).orElse(null);
        if (decision == null) return;

        Long recipientId = decision.getCreator().getId();
        if (recipientId.equals(event.getModeratorId())) return;

        String title = "Decision Locked";
        String message = String.format("Your decision '%s' has been locked for editing by a moderator.", event.getDecisionTitle());
        String actionUrl = "/decisions/" + event.getDecisionId();

        notificationService.createNotification(
                recipientId,
                title,
                message,
                NotificationType.DECISION_LOCKED,
                ReferenceType.DECISION,
                event.getDecisionId(),
                actionUrl
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDecisionUnlocked(DecisionUnlockedEvent event) {
        log.info("Handling DecisionUnlockedEvent for decision ID: {}", event.getDecisionId());

        Decision decision = decisionRepository.findById(event.getDecisionId()).orElse(null);
        if (decision == null) return;

        Long recipientId = decision.getCreator().getId();
        if (recipientId.equals(event.getModeratorId())) return;

        String title = "Decision Unlocked";
        String message = String.format("Your decision '%s' has been unlocked by a moderator.", event.getDecisionTitle());
        String actionUrl = "/decisions/" + event.getDecisionId();

        notificationService.createNotification(
                recipientId,
                title,
                message,
                NotificationType.DECISION_UNLOCKED,
                ReferenceType.DECISION,
                event.getDecisionId(),
                actionUrl
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDecisionPinned(DecisionPinnedEvent event) {
        log.info("Handling DecisionPinnedEvent for decision ID: {}", event.getDecisionId());

        Decision decision = decisionRepository.findById(event.getDecisionId()).orElse(null);
        if (decision == null) return;

        Long recipientId = decision.getCreator().getId();
        if (recipientId.equals(event.getModeratorId())) return;

        String title = "Decision Pinned";
        String message = String.format("Your decision '%s' has been pinned to the community board.", event.getDecisionTitle());
        String actionUrl = "/decisions/" + event.getDecisionId();

        notificationService.createNotification(
                recipientId,
                title,
                message,
                NotificationType.DECISION_PINNED,
                ReferenceType.DECISION,
                event.getDecisionId(),
                actionUrl
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDecisionUnpinned(DecisionUnpinnedEvent event) {
        log.info("Handling DecisionUnpinnedEvent for decision ID: {}", event.getDecisionId());

        Decision decision = decisionRepository.findById(event.getDecisionId()).orElse(null);
        if (decision == null) return;

        Long recipientId = decision.getCreator().getId();
        if (recipientId.equals(event.getModeratorId())) return;

        String title = "Decision Unpinned";
        String message = String.format("Your decision '%s' has been unpinned by a moderator.", event.getDecisionTitle());
        String actionUrl = "/decisions/" + event.getDecisionId();

        notificationService.createNotification(
                recipientId,
                title,
                message,
                NotificationType.DECISION_UNPINNED,
                ReferenceType.DECISION,
                event.getDecisionId(),
                actionUrl
        );
    }
}

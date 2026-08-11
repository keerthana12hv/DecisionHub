package com.decisionhub.event.notification;

import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.discussion.Comment;
import com.decisionhub.enums.notification.NotificationType;
import com.decisionhub.enums.notification.ReferenceType;
import com.decisionhub.event.CommentCreatedEvent;
import com.decisionhub.event.ReplyCreatedEvent;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.discussion.CommentRepository;
import com.decisionhub.service.interfaces.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscussionNotificationListener {

    private final NotificationService notificationService;
    private final DecisionRepository decisionRepository;
    private final CommentRepository commentRepository;

    @EventListener
    public void handleCommentCreated(CommentCreatedEvent event) {
        log.info("Handling CommentCreatedEvent for comment ID: {}", event.getCommentId());

        Decision decision = decisionRepository.findById(event.getDecisionId()).orElse(null);
        if (decision == null) {
            log.warn("Decision not found with ID: {}", event.getDecisionId());
            return;
        }

        Long recipientId = decision.getCreator().getId();
        if (recipientId.equals(event.getCommenterId())) {
            // Do not notify creator about their own comment
            return;
        }

        String title = "New Comment";
        String message = String.format("A new comment was posted on your decision '%s' by @%s",
                event.getDecisionTitle(), event.getCommenterUsername());
        String actionUrl = "/decisions/" + event.getDecisionId();

        notificationService.createNotification(
                recipientId,
                title,
                message,
                NotificationType.COMMENT_CREATED,
                ReferenceType.DECISION,
                event.getDecisionId(),
                actionUrl
        );
    }

    @EventListener
    public void handleReplyCreated(ReplyCreatedEvent event) {
        log.info("Handling ReplyCreatedEvent for reply ID: {}", event.getReplyId());

        Comment parentComment = commentRepository.findById(event.getParentCommentId()).orElse(null);
        if (parentComment == null) {
            log.warn("Parent comment not found with ID: {}", event.getParentCommentId());
            return;
        }

        Long recipientId = parentComment.getUser().getId();
        if (recipientId.equals(event.getReplierId())) {
            // Do not notify the original commenter if they replied to their own comment
            return;
        }

        String title = "New Reply";
        String message = String.format("@%s replied to your comment on '%s'",
                event.getReplierUsername(), event.getDecisionTitle());
        String actionUrl = "/decisions/" + event.getDecisionId();

        notificationService.createNotification(
                recipientId,
                title,
                message,
                NotificationType.REPLY_CREATED,
                ReferenceType.DECISION,
                event.getDecisionId(),
                actionUrl
        );
    }
}
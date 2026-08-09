package com.decisionhub.event.notification;

import com.decisionhub.entity.decision.Decision;
import com.decisionhub.enums.notification.NotificationType;
import com.decisionhub.enums.notification.ReferenceType;
import com.decisionhub.event.FeedbackReminderEvent;
import com.decisionhub.event.PollClosedEvent;
import com.decisionhub.event.VoteSubmittedEvent;
import com.decisionhub.repository.decision.DecisionRepository;
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
public class VotingNotificationListener {

    private final NotificationService notificationService;
    private final DecisionRepository decisionRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleVoteSubmitted(VoteSubmittedEvent event) {
        log.info("Handling VoteSubmittedEvent for decision ID: {}", event.getDecisionId());

        Decision decision = decisionRepository.findById(event.getDecisionId()).orElse(null);
        if (decision == null) return;

        Long recipientId = decision.getCreator().getId();
        if (recipientId.equals(event.getVoterId())) {
            // Do not notify creator about their own vote
            return;
        }

        String title = "New Vote Cast";
        String message = String.format("@%s cast a vote on your decision '%s'", 
                event.getVoterUsername(), event.getDecisionTitle());
        String actionUrl = "/decisions/" + event.getDecisionId();

        notificationService.createNotification(
                recipientId,
                title,
                message,
                NotificationType.VOTE_SUBMITTED,
                ReferenceType.DECISION,
                event.getDecisionId(),
                actionUrl
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePollClosed(PollClosedEvent event) {
        log.info("Handling PollClosedEvent for poll ID: {}", event.getPollId());

        Decision decision = decisionRepository.findById(event.getDecisionId()).orElse(null);
        if (decision == null) return;

        Long recipientId = decision.getCreator().getId();

        String title = "Poll Closed";
        String message = String.format("Voting has concluded and the poll for '%s' is closed.", event.getDecisionTitle());
        String actionUrl = "/decisions/" + event.getDecisionId();

        notificationService.createNotification(
                recipientId,
                title,
                message,
                NotificationType.POLL_CLOSED,
                ReferenceType.DECISION,
                event.getDecisionId(),
                actionUrl
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFeedbackReminder(FeedbackReminderEvent event) {
        log.info("Handling FeedbackReminderEvent for recipient ID: {} on decision ID: {}", 
                event.getRecipientId(), event.getDecisionId());

        String title = "Feedback Reminder";
        String message = String.format("Reminder: Please provide your feedback on the active decision '%s'.", event.getDecisionTitle());
        String actionUrl = "/decisions/" + event.getDecisionId();

        notificationService.createNotification(
                event.getRecipientId(),
                title,
                message,
                NotificationType.FEEDBACK_REMINDER,
                ReferenceType.DECISION,
                event.getDecisionId(),
                actionUrl
        );
    }
}

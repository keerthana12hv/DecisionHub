package com.decisionhub.event.notification;

import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.community.CommunityMember;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.enums.community.MembershipStatus;
import com.decisionhub.enums.notification.NotificationType;
import com.decisionhub.enums.notification.ReferenceType;
import com.decisionhub.event.DecisionPublishedEvent;
import com.decisionhub.event.voting.DecisionClosedEvent;
import com.decisionhub.repository.community.CommunityMemberRepository;
import com.decisionhub.repository.community.CommunityRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.service.interfaces.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DecisionNotificationListener {

    private final NotificationService notificationService;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final DecisionRepository decisionRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDecisionPublished(DecisionPublishedEvent event) {
        log.info("Handling DecisionPublishedEvent for decision ID: {}", event.getDecisionId());

        Decision decision = decisionRepository.findById(event.getDecisionId()).orElse(null);
        if (decision == null) {
            log.warn("Decision not found with ID: {}", event.getDecisionId());
            return;
        }

        // Resolving the community context
        Community community = decision.getCommunity();
        if (community == null) {
            log.info("Decision is public/not bound to a community. Skipping member notification.");
            return;
        }

        List<CommunityMember> members = communityMemberRepository.findByCommunityAndStatus(community, MembershipStatus.APPROVED);
        Long publisherId = decision.getCreator().getId();

        String title = "New Decision Published";
        String message = String.format("A new decision '%s' has been published in community '%s'.", 
                decision.getTitle(), community.getName());
        String actionUrl = "/decisions/" + decision.getId();

        for (CommunityMember member : members) {
            Long recipientId = member.getUser().getId();
            // Do not notify the publisher of their own decision
            if (!recipientId.equals(publisherId)) {
                notificationService.createNotification(
                        recipientId,
                        title,
                        message,
                        NotificationType.DECISION_PUBLISHED,
                        ReferenceType.DECISION,
                        decision.getId(),
                        actionUrl
                );
            }
        }
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDecisionClosed(DecisionClosedEvent event) {
        log.info("Handling DecisionClosedEvent for decision ID: {}", event.getDecisionId());

        Decision decision = decisionRepository.findById(event.getDecisionId()).orElse(null);
        if (decision == null) {
            log.warn("Decision not found with ID: {}", event.getDecisionId());
            return;
        }

        Community community = decision.getCommunity();
        if (community == null) {
            log.info("Decision is public/not bound to a community. Skipping member notification.");
            return;
        }

        List<CommunityMember> members = communityMemberRepository.findByCommunityAndStatus(community, MembershipStatus.APPROVED);
        Long closerId = decision.getCreator().getId(); // adjust if closer should be tracked separately

        String title = "Poll Closed";
        String message = String.format("Voting has ended for the decision '%s'.", decision.getTitle());
        String actionUrl = "/decisions/" + decision.getId();

        for (CommunityMember member : members) {
            Long recipientId = member.getUser().getId();
            notificationService.createNotification(
                    recipientId,
                    title,
                    message,
                    NotificationType.POLL_CLOSED,
                    ReferenceType.DECISION,
                    decision.getId(),
                    actionUrl
            );
        }
    }
}

package com.decisionhub.service.impl;

import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.community.CommunityMember;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.discussion.Comment;
import com.decisionhub.enums.community.CommunityMemberRole;
import com.decisionhub.enums.community.MembershipStatus;
import com.decisionhub.enums.notification.NotificationType;
import com.decisionhub.enums.notification.ReferenceType;
import com.decisionhub.event.*;
import com.decisionhub.event.notification.*;
import com.decisionhub.repository.community.CommunityMemberRepository;
import com.decisionhub.repository.community.CommunityRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.discussion.CommentRepository;
import com.decisionhub.service.interfaces.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private CommunityMemberRepository communityMemberRepository;

    private DiscussionNotificationListener discussionListener;
    private DecisionNotificationListener decisionListener;
    private ModerationNotificationListener moderationListener;
    private VotingNotificationListener votingListener;
    private CommunityNotificationListener communityListener;

    private User creator;
    private User otherUser;
    private Decision decision;

    @BeforeEach
    void setUp() {
        discussionListener = new DiscussionNotificationListener(notificationService, decisionRepository, commentRepository);
        decisionListener = new DecisionNotificationListener(notificationService, communityRepository, communityMemberRepository, decisionRepository);
        moderationListener = new ModerationNotificationListener(notificationService, decisionRepository, commentRepository);
        votingListener = new VotingNotificationListener(notificationService, decisionRepository);
        communityListener = new CommunityNotificationListener(notificationService);

        creator = new User();
        creator.setId(1L);
        creator.setUsername("creatorUser");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otherUser");

        decision = new Decision();
        decision.setId(100L);
        decision.setTitle("Sample Decision");
        decision.setCreator(creator);
    }

    @Test
    void handleCommentCreated_success() {
        CommentCreatedEvent event = new CommentCreatedEvent(
                this, 50L, 100L, "Sample Decision", 2L, "otherUser"
        );

        when(decisionRepository.findById(100L)).thenReturn(Optional.of(decision));

        discussionListener.handleCommentCreated(event);

        verify(notificationService, times(1)).createNotification(
                eq(1L), // Recipient: Decision creator
                eq("New Comment"),
                contains("posted on your decision 'Sample Decision' by @otherUser"),
                eq(NotificationType.COMMENT_CREATED),
                eq(ReferenceType.DECISION),
                eq(100L),
                eq("/decisions/100")
        );
    }

    @Test
    void handleCommentCreated_selfComment_noNotification() {
        // Commenter is the decision creator themselves
        CommentCreatedEvent event = new CommentCreatedEvent(
                this, 50L, 100L, "Sample Decision", 1L, "creatorUser"
        );

        when(decisionRepository.findById(100L)).thenReturn(Optional.of(decision));

        discussionListener.handleCommentCreated(event);

        verify(notificationService, never()).createNotification(anyLong(), anyString(), anyString(), any(), any(), anyLong(), anyString());
    }

    @Test
    void handleReplyCreated_success() {
        ReplyCreatedEvent event = new ReplyCreatedEvent(
                this, 51L, 50L, 100L, "Sample Decision", 2L, "otherUser"
        );

        Comment parentComment = new Comment();
        parentComment.setId(50L);
        parentComment.setUser(creator); // Author is decision creator

        when(commentRepository.findById(50L)).thenReturn(Optional.of(parentComment));

        discussionListener.handleReplyCreated(event);

        verify(notificationService, times(1)).createNotification(
                eq(1L),
                eq("New Reply"),
                contains("@otherUser replied to your comment on decision 'Sample Decision'"),
                eq(NotificationType.REPLY_CREATED),
                eq(ReferenceType.DECISION),
                eq(100L),
                eq("/decisions/100")
        );
    }

    @Test
    void handleDecisionPublished_success() {
        Community community = new Community();
        community.setId(10L);
        community.setName("Tech Board");

        decision.setCommunity(community);

        CommunityMember member = new CommunityMember();
        member.setUser(otherUser);

        when(decisionRepository.findById(100L)).thenReturn(Optional.of(decision));
        when(communityMemberRepository.findByCommunityAndStatus(community, MembershipStatus.APPROVED))
                .thenReturn(List.of(member));

        DecisionPublishedEvent event = new DecisionPublishedEvent(
                this, 100L, "Sample Decision", 10L, "Tech Board"
        );

        decisionListener.handleDecisionPublished(event);

        verify(notificationService, times(1)).createNotification(
                eq(2L), // Recipient: otherUser
                eq("New Decision Published"),
                contains("decision 'Sample Decision' has been published in community 'Tech Board'"),
                eq(NotificationType.DECISION_PUBLISHED),
                eq(ReferenceType.DECISION),
                eq(100L),
                eq("/decisions/100")
        );
    }

    @Test
    void handleDecisionLocked_success() {
        DecisionLockedEvent event = new DecisionLockedEvent(
                this, 100L, "Sample Decision", 2L // Moderator ID 2
        );

        when(decisionRepository.findById(100L)).thenReturn(Optional.of(decision));

        moderationListener.handleDecisionLocked(event);

        verify(notificationService, times(1)).createNotification(
                eq(1L),
                eq("Decision Locked"),
                contains("decision 'Sample Decision' has been locked for editing"),
                eq(NotificationType.DECISION_LOCKED),
                eq(ReferenceType.DECISION),
                eq(100L),
                eq("/decisions/100")
        );
    }

    @Test
    void handleVoteSubmitted_success() {
        VoteSubmittedEvent event = new VoteSubmittedEvent(
                this, 100L, "Sample Decision", 2L, "otherUser"
        );

        when(decisionRepository.findById(100L)).thenReturn(Optional.of(decision));

        votingListener.handleVoteSubmitted(event);

        verify(notificationService, times(1)).createNotification(
                eq(1L),
                eq("New Vote Cast"),
                contains("@otherUser cast a vote on your decision 'Sample Decision'"),
                eq(NotificationType.VOTE_SUBMITTED),
                eq(ReferenceType.DECISION),
                eq(100L),
                eq("/decisions/100")
        );
    }

    @Test
    void handleMembershipApproved_success() {
        MembershipApprovedEvent event = new MembershipApprovedEvent(
                this, 10L, "Tech Board", 2L
        );

        communityListener.handleMembershipApproved(event);

        verify(notificationService, times(1)).createNotification(
                eq(2L),
                eq("Join Request Approved"),
                contains("join request to the community 'Tech Board' has been approved"),
                eq(NotificationType.MEMBERSHIP_APPROVED),
                eq(ReferenceType.COMMUNITY),
                eq(10L),
                eq("/communities/10")
        );
    }
}

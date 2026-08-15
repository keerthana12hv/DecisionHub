package com.decisionhub.service;

import com.decisionhub.dto.response.discussion.CommentResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.community.CommunityMember;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.discussion.Comment;
import com.decisionhub.enums.authentication.PlatformRole;
import com.decisionhub.enums.community.CommunityMemberRole;
import com.decisionhub.enums.community.MembershipStatus;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.community.CommunityMemberRepository;
import com.decisionhub.repository.discussion.CommentRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.service.impl.community.CommunityModerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentModerationTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommunityMemberRepository communityMemberRepository;
    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private CommunityModerationServiceImpl communityModerationService;

    private User adminUser;
    private User ownerUser;
    private User moderatorUser;
    private User regularUser;

    private Community community;
    private Decision decision;
    private Comment comment;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(10L);
        adminUser.setRole(PlatformRole.ADMIN);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@test.com");

        ownerUser = new User();
        ownerUser.setId(11L);
        ownerUser.setRole(PlatformRole.USER);
        ownerUser.setUsername("owner");
        ownerUser.setEmail("owner@test.com");

        moderatorUser = new User();
        moderatorUser.setId(12L);
        moderatorUser.setRole(PlatformRole.USER);
        moderatorUser.setUsername("moderator");
        moderatorUser.setEmail("mod@test.com");

        regularUser = new User();
        regularUser.setId(13L);
        regularUser.setRole(PlatformRole.USER);
        regularUser.setUsername("user");
        regularUser.setEmail("user@test.com");

        community = new Community();
        community.setId(20L);
        community.setOwner(ownerUser);

        decision = new Decision();
        decision.setId(30L);
        decision.setCommunity(community);

        comment = new Comment();
        comment.setId(40L);
        comment.setContent("Inappropriate comment");
        comment.setUser(regularUser);
        comment.setDecision(decision);
        comment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void deleteComment_Admin_Succeeds() {
        when(commentRepository.findById(40L)).thenReturn(Optional.of(comment));
        when(authenticationFacade.getCurrentUser()).thenReturn(Optional.of(adminUser));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = communityModerationService.deleteComment(40L);

        assertNotNull(response);
        assertNotNull(comment.getDeletedAt());
    }

    @Test
    void deleteComment_Owner_Succeeds() {
        when(commentRepository.findById(40L)).thenReturn(Optional.of(comment));
        when(authenticationFacade.getCurrentUser()).thenReturn(Optional.of(ownerUser));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = communityModerationService.deleteComment(40L);

        assertNotNull(response);
        assertNotNull(comment.getDeletedAt());
    }

    @Test
    void deleteComment_CommunityModerator_Succeeds() {
        when(commentRepository.findById(40L)).thenReturn(Optional.of(comment));
        when(authenticationFacade.getCurrentUser()).thenReturn(Optional.of(moderatorUser));

        CommunityMember member = new CommunityMember();
        member.setStatus(MembershipStatus.APPROVED);
        member.setRole(CommunityMemberRole.MODERATOR);
        when(communityMemberRepository.findByCommunityIdAndUserId(20L, 12L)).thenReturn(Optional.of(member));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = communityModerationService.deleteComment(40L);

        assertNotNull(response);
        assertNotNull(comment.getDeletedAt());
    }

    @Test
    void deleteComment_RegularUser_ThrowsUnauthorized() {
        when(commentRepository.findById(40L)).thenReturn(Optional.of(comment));
        when(authenticationFacade.getCurrentUser()).thenReturn(Optional.of(regularUser));

        assertThrows(UnauthorizedActionException.class, () ->
            communityModerationService.deleteComment(40L)
        );
    }

    @Test
    void deleteComment_DifferentCommunityModerator_ThrowsUnauthorized() {
        when(commentRepository.findById(40L)).thenReturn(Optional.of(comment));
        when(authenticationFacade.getCurrentUser()).thenReturn(Optional.of(moderatorUser));
        when(communityMemberRepository.findByCommunityIdAndUserId(20L, 12L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedActionException.class, () ->
            communityModerationService.deleteComment(40L)
        );
    }

    @Test
    void deleteComment_AlreadyDeleted_ThrowsNotFound() {
        comment.setDeletedAt(LocalDateTime.now());
        when(commentRepository.findById(40L)).thenReturn(Optional.of(comment));

        assertThrows(ResourceNotFoundException.class, () ->
            communityModerationService.deleteComment(40L)
        );
    }

    @Test
    void pinComment_Succeeds() {
        when(commentRepository.findById(40L)).thenReturn(Optional.of(comment));
        when(authenticationFacade.getCurrentUser()).thenReturn(Optional.of(ownerUser));
        when(commentRepository.findFirstByDecisionIdAndPinnedTrueAndDeletedAtIsNull(30L)).thenReturn(Optional.empty());
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = communityModerationService.pinComment(40L);

        assertNotNull(response);
        assertTrue(comment.isPinned());
    }

    @Test
    void pinComment_UnpinsPreviousComment() {
        Comment previouslyPinned = new Comment();
        previouslyPinned.setId(41L);
        previouslyPinned.setPinned(true);
        previouslyPinned.setDecision(decision);

        when(commentRepository.findById(40L)).thenReturn(Optional.of(comment));
        when(authenticationFacade.getCurrentUser()).thenReturn(Optional.of(ownerUser));
        when(commentRepository.findFirstByDecisionIdAndPinnedTrueAndDeletedAtIsNull(30L)).thenReturn(Optional.of(previouslyPinned));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = communityModerationService.pinComment(40L);

        assertNotNull(response);
        assertTrue(comment.isPinned());
        assertFalse(previouslyPinned.isPinned());
    }

    @Test
    void unpinComment_Succeeds() {
        comment.setPinned(true);
        when(commentRepository.findById(40L)).thenReturn(Optional.of(comment));
        when(authenticationFacade.getCurrentUser()).thenReturn(Optional.of(ownerUser));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = communityModerationService.unpinComment(40L);

        assertNotNull(response);
        assertFalse(comment.isPinned());
    }

    @Test
    void getPinnedComment_Exists_ReturnsOptional() {
        comment.setPinned(true);
        when(commentRepository.findFirstByDecisionIdAndPinnedTrueAndDeletedAtIsNull(30L)).thenReturn(Optional.of(comment));

        Optional<CommentResponse> pinned = communityModerationService.getPinnedComment(30L);

        assertTrue(pinned.isPresent());
        assertEquals(40L, pinned.get().id());
    }

    @Test
    void getPinnedComment_None_ReturnsEmpty() {
        when(commentRepository.findFirstByDecisionIdAndPinnedTrueAndDeletedAtIsNull(30L)).thenReturn(Optional.empty());

        Optional<CommentResponse> pinned = communityModerationService.getPinnedComment(30L);

        assertFalse(pinned.isPresent());
    }
}

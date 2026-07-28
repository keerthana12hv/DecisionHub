package com.decisionhub.security.decision;

import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.community.CommunityMember;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.enums.community.MembershipStatus;
import com.decisionhub.enums.decision.DecisionVisibility;
import com.decisionhub.repository.community.CommunityMemberRepository;
import com.decisionhub.repository.decision.DecisionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecisionAuthorizationServiceImplTest {

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private CommunityMemberRepository communityMemberRepository;

    @InjectMocks
    private DecisionAuthorizationServiceImpl authorizationService;

    private Long decisionId;
    private Long userId;
    private Long ownerId;
    private Long communityId;

    private Decision decision;
    private User owner;
    private Community community;

    @BeforeEach
    void setUp() {

        decisionId = 1L;
        userId = 10L;
        ownerId = 20L;
        communityId = 100L;

        owner = new User();
        owner.setId(ownerId);

        community = new Community();
        community.setId(communityId);

        decision = new Decision();
        decision.setId(decisionId);
        decision.setCreator(owner);
    }

    // =========================================================
    // canParticipateInVoting()
    // =========================================================

    @Test
    void canParticipateInVoting_PublicDecisionAuthenticatedUser_ShouldReturnTrue() {

        decision.setVisibility(DecisionVisibility.PUBLIC);

        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.of(decision));

        boolean result =
                authorizationService.canParticipateInVoting(
                        decisionId,
                        userId
                );

        assertTrue(result);

        verify(decisionRepository)
                .findById(decisionId);

        verifyNoInteractions(communityMemberRepository);
    }

    @Test
    void canParticipateInVoting_NullUserId_ShouldReturnFalse() {

        boolean result =
                authorizationService.canParticipateInVoting(
                        decisionId,
                        null
                );

        assertFalse(result);

        verifyNoInteractions(
                decisionRepository,
                communityMemberRepository
        );
    }

    @Test
    void canParticipateInVoting_NullDecisionId_ShouldReturnFalse() {

        boolean result =
                authorizationService.canParticipateInVoting(
                        null,
                        userId
                );

        assertFalse(result);

        verifyNoInteractions(
                decisionRepository,
                communityMemberRepository
        );
    }

    @Test
    void canParticipateInVoting_DecisionNotFound_ShouldReturnFalse() {

        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.empty());

        boolean result =
                authorizationService.canParticipateInVoting(
                        decisionId,
                        userId
                );

        assertFalse(result);

        verify(decisionRepository)
                .findById(decisionId);

        verifyNoInteractions(communityMemberRepository);
    }

    @Test
    void canParticipateInVoting_CommunityDecisionApprovedMember_ShouldReturnTrue() {

        decision.setVisibility(DecisionVisibility.COMMUNITY);
        decision.setCommunity(community);

        CommunityMember member = mock(CommunityMember.class);

        when(member.getStatus())
                .thenReturn(MembershipStatus.APPROVED);

        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.of(decision));

        when(communityMemberRepository.findByCommunityIdAndUserId(
                communityId,
                userId
        )).thenReturn(Optional.of(member));

        boolean result =
                authorizationService.canParticipateInVoting(
                        decisionId,
                        userId
                );

        assertTrue(result);

        verify(communityMemberRepository)
                .findByCommunityIdAndUserId(
                        communityId,
                        userId
                );
    }

    @Test
    void canParticipateInVoting_CommunityDecisionNonApprovedMember_ShouldReturnFalse() {

        decision.setVisibility(DecisionVisibility.COMMUNITY);
        decision.setCommunity(community);

        CommunityMember member = mock(CommunityMember.class);

        /*
         * Any membership status other than APPROVED must
         * prevent voting participation.
         *
         * Using null here keeps the test independent of the
         * exact names of other MembershipStatus enum values.
         */
        when(member.getStatus())
                .thenReturn(null);

        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.of(decision));

        when(communityMemberRepository.findByCommunityIdAndUserId(
                communityId,
                userId
        )).thenReturn(Optional.of(member));

        boolean result =
                authorizationService.canParticipateInVoting(
                        decisionId,
                        userId
                );

        assertFalse(result);
    }

    @Test
    void canParticipateInVoting_CommunityDecisionNonMember_ShouldReturnFalse() {

        decision.setVisibility(DecisionVisibility.COMMUNITY);
        decision.setCommunity(community);

        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.of(decision));

        when(communityMemberRepository.findByCommunityIdAndUserId(
                communityId,
                userId
        )).thenReturn(Optional.empty());

        boolean result =
                authorizationService.canParticipateInVoting(
                        decisionId,
                        userId
                );

        assertFalse(result);
    }

    @Test
    void canParticipateInVoting_CommunityDecisionWithoutCommunity_ShouldReturnFalse() {

        decision.setVisibility(DecisionVisibility.COMMUNITY);
        decision.setCommunity(null);

        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.of(decision));

        boolean result =
                authorizationService.canParticipateInVoting(
                        decisionId,
                        userId
                );

        assertFalse(result);

        verifyNoInteractions(communityMemberRepository);
    }

    // =========================================================
    // canManagePoll()
    // =========================================================

    @Test
    void canManagePoll_DecisionOwner_ShouldReturnTrue() {

        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.of(decision));

        boolean result =
                authorizationService.canManagePoll(
                        decisionId,
                        ownerId
                );

        assertTrue(result);

        verify(decisionRepository)
                .findById(decisionId);
    }

    @Test
    void canManagePoll_NonOwner_ShouldReturnFalse() {

        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.of(decision));

        boolean result =
                authorizationService.canManagePoll(
                        decisionId,
                        userId
                );

        assertFalse(result);

        verify(decisionRepository)
                .findById(decisionId);
    }

    @Test
    void canManagePoll_NullUserId_ShouldReturnFalse() {

        boolean result =
                authorizationService.canManagePoll(
                        decisionId,
                        null
                );

        assertFalse(result);

        verifyNoInteractions(decisionRepository);
    }

    @Test
    void canManagePoll_DecisionNotFound_ShouldReturnFalse() {

        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.empty());

        boolean result =
                authorizationService.canManagePoll(
                        decisionId,
                        userId
                );

        assertFalse(result);
    }
}
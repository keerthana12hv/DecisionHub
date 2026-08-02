package com.decisionhub.service;

import com.decisionhub.dto.request.community.UpdateMemberRoleRequest;
import com.decisionhub.dto.response.community.CommunityMemberResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.community.CommunityMember;
import com.decisionhub.enums.authentication.PlatformRole;
import com.decisionhub.enums.community.CommunityMemberRole;
import com.decisionhub.enums.community.MembershipStatus;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.community.CommunityMemberRepository;
import com.decisionhub.repository.community.CommunityRepository;
import com.decisionhub.service.impl.community.CommunityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityMemberRoleUpdateTest {

    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private CommunityMemberRepository communityMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CommunityServiceImpl communityService;

    private User owner;
    private User admin;
    private User memberUser;
    private Community community;
    private CommunityMember member;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@email.com");
        owner.setRole(PlatformRole.MODERATOR);

        admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@email.com");
        admin.setRole(PlatformRole.ADMIN);

        memberUser = new User();
        memberUser.setId(3L);
        memberUser.setEmail("member@email.com");
        memberUser.setRole(PlatformRole.USER);

        community = new Community();
        community.setId(10L);
        community.setOwner(owner);

        member = new CommunityMember();
        member.setId(20L);
        member.setCommunity(community);
        member.setUser(memberUser);
        member.setRole(CommunityMemberRole.MEMBER);
        member.setStatus(MembershipStatus.APPROVED);
        member.setJoinedAt(LocalDateTime.now());
    }

    private void mockCurrentUser(User user) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void updateMemberRole_AsOwner_Success() {
        mockCurrentUser(owner);
        when(communityRepository.findById(10L)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByIdAndCommunity(20L, community)).thenReturn(Optional.of(member));
        when(communityMemberRepository.save(any(CommunityMember.class))).thenReturn(member);

        UpdateMemberRoleRequest request = new UpdateMemberRoleRequest(CommunityMemberRole.MODERATOR);
        CommunityMemberResponse response = communityService.updateMemberRole(10L, 20L, request);

        assertNotNull(response);
        assertEquals(CommunityMemberRole.MODERATOR, response.role());
        verify(communityMemberRepository).save(member);
    }

    @Test
    void updateMemberRole_AsAdmin_Success() {
        mockCurrentUser(admin);
        when(communityRepository.findById(10L)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByIdAndCommunity(20L, community)).thenReturn(Optional.of(member));
        when(communityMemberRepository.save(any(CommunityMember.class))).thenReturn(member);

        UpdateMemberRoleRequest request = new UpdateMemberRoleRequest(CommunityMemberRole.MODERATOR);
        CommunityMemberResponse response = communityService.updateMemberRole(10L, 20L, request);

        assertNotNull(response);
        assertEquals(CommunityMemberRole.MODERATOR, response.role());
        verify(communityMemberRepository).save(member);
    }

    @Test
    void updateMemberRole_AsNonOwner_ThrowsUnauthorizedException() {
        mockCurrentUser(memberUser);
        when(communityRepository.findById(10L)).thenReturn(Optional.of(community));

        UpdateMemberRoleRequest request = new UpdateMemberRoleRequest(CommunityMemberRole.MODERATOR);
        assertThrows(UnauthorizedActionException.class, () ->
                communityService.updateMemberRole(10L, 20L, request)
        );
    }

    @Test
    void updateMemberRole_OnOwnerMember_ThrowsBadRequestException() {
        mockCurrentUser(owner);
        CommunityMember ownerMember = new CommunityMember();
        ownerMember.setId(21L);
        ownerMember.setCommunity(community);
        ownerMember.setUser(owner);
        ownerMember.setRole(CommunityMemberRole.MODERATOR);
        ownerMember.setStatus(MembershipStatus.APPROVED);

        when(communityRepository.findById(10L)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByIdAndCommunity(21L, community)).thenReturn(Optional.of(ownerMember));

        UpdateMemberRoleRequest request = new UpdateMemberRoleRequest(CommunityMemberRole.MEMBER);
        assertThrows(BadRequestException.class, () ->
                communityService.updateMemberRole(10L, 21L, request)
        );
    }

    @Test
    void updateMemberRole_NonApprovedMember_ThrowsBadRequestException() {
        mockCurrentUser(owner);
        member.setStatus(MembershipStatus.PENDING);

        when(communityRepository.findById(10L)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByIdAndCommunity(20L, community)).thenReturn(Optional.of(member));

        UpdateMemberRoleRequest request = new UpdateMemberRoleRequest(CommunityMemberRole.MODERATOR);
        assertThrows(BadRequestException.class, () ->
                communityService.updateMemberRole(10L, 20L, request)
        );
    }
}

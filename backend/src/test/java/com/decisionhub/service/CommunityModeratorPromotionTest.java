package com.decisionhub.service;

import com.decisionhub.dto.request.community.CreateCommunityRequest;
import com.decisionhub.dto.response.community.CommunityResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Category;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.community.CommunityMember;
import com.decisionhub.enums.authentication.PlatformRole;
import com.decisionhub.enums.authentication.UserStatus;
import com.decisionhub.enums.community.CommunityVisibility;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.community.CategoryRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityModeratorPromotionTest {

    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private CommunityMemberRepository communityMemberRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CommunityServiceImpl communityService;

    private User user;
    private Category category;
    private CreateCommunityRequest request;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("testuser@email.com");
        user.setRole(PlatformRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        category = new Category();
        category.setId(2L);
        category.setName("Gaming");

        request = new CreateCommunityRequest("Test Community", "test-community", "Description", 2L, CommunityVisibility.PUBLIC);
    }

    @Test
    void createCommunity_FirstCommunity_PromotesUserToModerator() {
        when(authentication.getName()).thenReturn("testuser@email.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByEmail("testuser@email.com")).thenReturn(Optional.of(user));
        when(communityRepository.existsByName(any())).thenReturn(false);
        when(communityRepository.existsBySlug(any())).thenReturn(false);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        
        // Return empty list indicating first community
        when(communityRepository.findByOwner(user)).thenReturn(Collections.emptyList());
        
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> {
            Community c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        CommunityResponse response = communityService.createCommunity(request);

        assertNotNull(response);
        assertEquals(PlatformRole.MODERATOR, user.getRole());
        verify(userRepository).save(user);
        verify(communityRepository).save(any(Community.class));
        verify(communityMemberRepository).save(any(CommunityMember.class));
    }

    @Test
    void createCommunity_AlreadyModerator_DoesNotChangeRole() {
        user.setRole(PlatformRole.MODERATOR);

        when(authentication.getName()).thenReturn("testuser@email.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByEmail("testuser@email.com")).thenReturn(Optional.of(user));
        when(communityRepository.existsByName(any())).thenReturn(false);
        when(communityRepository.existsBySlug(any())).thenReturn(false);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        
        // Even if empty list
        when(communityRepository.findByOwner(user)).thenReturn(Collections.emptyList());
        
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> {
            Community c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        CommunityResponse response = communityService.createCommunity(request);

        assertNotNull(response);
        assertEquals(PlatformRole.MODERATOR, user.getRole());
        verify(userRepository, never()).save(user); // Should not save User
    }

    @Test
    void createCommunity_SecondCommunity_DoesNotChangeRole() {
        when(authentication.getName()).thenReturn("testuser@email.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByEmail("testuser@email.com")).thenReturn(Optional.of(user));
        when(communityRepository.existsByName(any())).thenReturn(false);
        when(communityRepository.existsBySlug(any())).thenReturn(false);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        
        // User already has another active community
        Community existingCommunity = new Community();
        existingCommunity.setId(5L);
        existingCommunity.setDeletedAt(null); // Active community
        when(communityRepository.findByOwner(user)).thenReturn(List.of(existingCommunity));
        
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> {
            Community c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        CommunityResponse response = communityService.createCommunity(request);

        assertNotNull(response);
        assertEquals(PlatformRole.USER, user.getRole()); // Still USER
        verify(userRepository, never()).save(user);
    }

    @Test
    void createCommunity_HasOnlySoftDeletedCommunity_PromotesUserToModerator() {
        when(authentication.getName()).thenReturn("testuser@email.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByEmail("testuser@email.com")).thenReturn(Optional.of(user));
        when(communityRepository.existsByName(any())).thenReturn(false);
        when(communityRepository.existsBySlug(any())).thenReturn(false);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        
        // User has a soft-deleted community
        Community existingCommunity = new Community();
        existingCommunity.setId(5L);
        existingCommunity.setDeletedAt(LocalDateTime.now().minusDays(1)); // Soft-deleted
        when(communityRepository.findByOwner(user)).thenReturn(List.of(existingCommunity));
        
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> {
            Community c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        CommunityResponse response = communityService.createCommunity(request);

        assertNotNull(response);
        assertEquals(PlatformRole.MODERATOR, user.getRole()); // Promoted to MODERATOR
        verify(userRepository).save(user);
    }
}

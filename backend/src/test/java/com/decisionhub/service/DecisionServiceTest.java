package com.decisionhub.service;

import com.decisionhub.dto.request.decision.DecisionRequest;
import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.dto.response.authentication.UserResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.enums.decision.DecisionVisibility;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.decision.DecisionMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.community.CommunityRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.decision.DecisionOptionRepository;
import com.decisionhub.repository.decision.ComparisonFactorRepository;
import com.decisionhub.repository.decision.ComparisonScoreRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.security.decision.DecisionAuthorizationService;
import com.decisionhub.service.impl.decision.DecisionServiceImpl;
import com.decisionhub.service.interfaces.audit.AuditService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private DecisionOptionRepository decisionOptionRepository;
    @Mock
    private ComparisonFactorRepository comparisonFactorRepository;
    @Mock
    private ComparisonScoreRepository comparisonScoreRepository;
    @Mock
    private DecisionMapper decisionMapper;
    @Mock
    private DecisionAuthorizationService decisionAuthorizationService;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private AuditService auditService;
    @Spy
    private com.decisionhub.validator.decision.DecisionValidator decisionValidator = new com.decisionhub.validator.decision.DecisionValidator();
    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DecisionServiceImpl decisionService;

    private User user;
    private Decision decision;
    private DecisionRequest request;
    private DecisionResponse response;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("owner");

        decision = new Decision();
        decision.setId(1L);
        decision.setTitle("Test Title");
        decision.setCreator(user);
        decision.setStatus(DecisionStatus.DRAFT);
        decision.setVisibility(DecisionVisibility.PUBLIC);

        request = new DecisionRequest(
            "Test Title",
            "Description",
            null,
            null,
            true,
            null,
            null,
            LocalDateTime.now().plusDays(1),
            null,
            Collections.emptyList(),
            Collections.emptyList()
        );

        UserResponse userResponse = new UserResponse(1L, "owner", "owner@email.com", null, null);
        response = new DecisionResponse(
            1L,
            "Test Title",
            "Description",
            userResponse,
            null,
            null,
            DecisionStatus.DRAFT,
            request.deadline(),
            Collections.emptyList(),
            Collections.emptyList(),
            LocalDateTime.now(),
            false,
            false
        );
    }

    @Test
    void createDecision_Success() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(decisionMapper.toEntity(any(DecisionRequest.class))).thenReturn(decision);
        when(decisionRepository.save(any(Decision.class))).thenReturn(decision);
        when(decisionMapper.toResponse(any(Decision.class))).thenReturn(response);

        DecisionResponse result = decisionService.createDecision(request, "127.0.0.1", "Mozilla");

        assertNotNull(result);
        assertEquals("Test Title", result.title());
        verify(auditService).log(eq(user), eq("DECISION_CREATED"), eq("decisions"), eq(1L), isNull(), anyString(), anyString(), anyString());
    }

    @Test
    void createDecision_CommunityDecision_Success() {
        Community community = new Community();
        community.setId(2L);
        community.setName("Test Community");

        DecisionRequest communityRequest = new DecisionRequest(
            "Test Title", "Desc", null, 2L, false, null, null, null, null, Collections.emptyList(), Collections.emptyList()
        );

        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(communityRepository.findById(2L)).thenReturn(Optional.of(community));
        when(decisionAuthorizationService.canCreateDecision(2L, 1L)).thenReturn(true);
        when(decisionMapper.toEntity(any(DecisionRequest.class))).thenReturn(decision);
        when(decisionRepository.save(any(Decision.class))).thenReturn(decision);
        when(decisionMapper.toResponse(any(Decision.class))).thenReturn(response);

        DecisionResponse result = decisionService.createDecision(communityRequest, "127.0.0.1", "Mozilla");

        assertNotNull(result);
        verify(decisionRepository).save(any(Decision.class));
    }

    @Test
    void createDecision_PastDeadline_ThrowsBadRequest() {
        DecisionRequest invalidRequest = new DecisionRequest(
            "Test Title", "Desc", null, null, true, null, null, LocalDateTime.now().minusDays(1), null, Collections.emptyList(), Collections.emptyList()
        );

        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> decisionService.createDecision(invalidRequest, "127.0.0.1", "Mozilla"));
    }

    @Test
    void getDecisionById_Success() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(decision));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(decisionAuthorizationService.canViewDecision(1L, 1L)).thenReturn(true);
        when(decisionMapper.toResponse(decision)).thenReturn(response);

        DecisionResponse result = decisionService.getDecisionById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void getDecisionById_Unauthorized_ThrowsUnauthorized() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(decision));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(2L));
        when(decisionAuthorizationService.canViewDecision(1L, 2L)).thenReturn(false);

        assertThrows(UnauthorizedActionException.class, () -> decisionService.getDecisionById(1L));
    }

    @Test
    void getAllDecisions_Success() {
        when(decisionRepository.findAll()).thenReturn(List.of(decision));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(decisionAuthorizationService.canViewDecision(1L, 1L)).thenReturn(true);
        when(decisionMapper.toResponse(decision)).thenReturn(response);

        List<DecisionResponse> results = decisionService.getAllDecisions(null, null, null);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void updateDecision_Success() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(decision));
        when(decisionAuthorizationService.canEditDecision(1L, 1L)).thenReturn(true);
        when(decisionRepository.save(decision)).thenReturn(decision);
        when(decisionMapper.toResponse(decision)).thenReturn(response);

        DecisionResponse result = decisionService.updateDecision(1L, request, "127.0.0.1", "Mozilla");

        assertNotNull(result);
        verify(auditService).log(eq(user), eq("DECISION_UPDATED"), eq("decisions"), eq(1L), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void updateDecision_Unauthorized_ThrowsUnauthorized() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(2L));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(decision));
        when(decisionAuthorizationService.canEditDecision(1L, 2L)).thenReturn(false);

        assertThrows(UnauthorizedActionException.class, () -> decisionService.updateDecision(1L, request, "127.0.0.1", "Mozilla"));
    }

    @Test
    void deleteDecision_Success() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(decision));
        when(decisionAuthorizationService.canDeleteDecision(1L, 1L)).thenReturn(true);
        when(comparisonScoreRepository.findByOptionDecisionId(1L)).thenReturn(Collections.emptyList());
        when(comparisonFactorRepository.findByDecisionId(1L)).thenReturn(Collections.emptyList());
        when(decisionOptionRepository.findByDecisionId(1L)).thenReturn(Collections.emptyList());

        decisionService.deleteDecision(1L, "127.0.0.1", "Mozilla");

        verify(decisionRepository).delete(decision);
        verify(auditService).log(eq(user), eq("DECISION_DELETED"), eq("decisions"), eq(1L), anyString(), isNull(), anyString(), anyString());
    }

    @Test
    void deleteDecision_Unauthorized_ThrowsUnauthorized() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(2L));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(decision));
        when(decisionAuthorizationService.canDeleteDecision(1L, 2L)).thenReturn(false);

        assertThrows(UnauthorizedActionException.class, () -> decisionService.deleteDecision(1L, "127.0.0.1", "Mozilla"));
    }
}

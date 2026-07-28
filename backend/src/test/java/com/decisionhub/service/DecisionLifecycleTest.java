package com.decisionhub.service;

import com.decisionhub.dto.request.decision.ComparisonFactorRequest;
import com.decisionhub.dto.request.decision.ComparisonScoreRequest;
import com.decisionhub.dto.request.decision.DecisionRequest;
import com.decisionhub.dto.request.decision.OptionCreateDto;
import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.enums.decision.VotingType;
import com.decisionhub.exception.DecisionClosedException;
import com.decisionhub.exception.DecisionLockedException;
import com.decisionhub.mapper.decision.DecisionMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.ComparisonFactorRepository;
import com.decisionhub.repository.decision.ComparisonScoreRepository;
import com.decisionhub.repository.decision.DecisionOptionRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.security.decision.DecisionAuthorizationService;
import com.decisionhub.service.impl.decision.ComparisonFactorServiceImpl;
import com.decisionhub.service.impl.decision.ComparisonScoreServiceImpl;
import com.decisionhub.service.impl.decision.DecisionOptionServiceImpl;
import com.decisionhub.service.impl.decision.DecisionServiceImpl;
import com.decisionhub.validator.decision.DecisionModificationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionLifecycleTest {

    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DecisionOptionRepository decisionOptionRepository;
    @Mock
    private ComparisonFactorRepository comparisonFactorRepository;
    @Mock
    private ComparisonScoreRepository comparisonScoreRepository;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private DecisionAuthorizationService decisionAuthorizationService;
    @Mock
    private DecisionMapper decisionMapper;
    @Mock
    private com.decisionhub.service.interfaces.audit.AuditService auditService;
    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Spy
    private DecisionModificationValidator decisionModificationValidator;

    @InjectMocks
    private DecisionServiceImpl decisionService;
    @InjectMocks
    private DecisionOptionServiceImpl decisionOptionService;
    @InjectMocks
    private ComparisonFactorServiceImpl comparisonFactorService;
    @InjectMocks
    private ComparisonScoreServiceImpl comparisonScoreService;

    private Decision closedDecision;
    private Decision lockedAndClosedDecision;
    private Decision activeDecision;
    private User testUser;

    @BeforeEach
    void setUp() {
        decisionModificationValidator = new DecisionModificationValidator(decisionRepository);
        
        injectField(decisionService, "decisionModificationValidator", decisionModificationValidator);
        injectField(decisionOptionService, "decisionModificationValidator", decisionModificationValidator);
        injectField(comparisonFactorService, "decisionModificationValidator", decisionModificationValidator);
        injectField(comparisonScoreService, "decisionModificationValidator", decisionModificationValidator);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");

        closedDecision = new Decision();
        closedDecision.setId(1L);
        closedDecision.setLocked(false);
        closedDecision.setStatus(DecisionStatus.CLOSED);
        closedDecision.setVotingType(VotingType.RATING_BASED);

        lockedAndClosedDecision = new Decision();
        lockedAndClosedDecision.setId(2L);
        lockedAndClosedDecision.setLocked(true);
        lockedAndClosedDecision.setStatus(DecisionStatus.CLOSED);
        lockedAndClosedDecision.setVotingType(VotingType.RATING_BASED);

        activeDecision = new Decision();
        activeDecision.setId(3L);
        activeDecision.setLocked(false);
        activeDecision.setStatus(DecisionStatus.ACTIVE);
        activeDecision.setVotingType(VotingType.RATING_BASED);
    }

    private void injectField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void updateClosedDecision_ThrowsDecisionClosedException() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(closedDecision));

        DecisionRequest request = new DecisionRequest("New Title", "Desc", null, false, VotingType.RATING_BASED, null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), null, null, null);

        assertThrows(DecisionClosedException.class, () ->
            decisionService.updateDecision(1L, request, "127.0.0.1", "agent")
        );
    }

    @Test
    void updateLockedAndClosedDecision_ThrowsDecisionClosedException_EnforcingPrecedence() {
        // Validation precedence check: CLOSED must precede LOCKED, so DecisionClosedException is expected
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(decisionRepository.findById(2L)).thenReturn(Optional.of(lockedAndClosedDecision));

        DecisionRequest request = new DecisionRequest("New Title", "Desc", null, false, VotingType.RATING_BASED, null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), null, null, null);

        assertThrows(DecisionClosedException.class, () ->
            decisionService.updateDecision(2L, request, "127.0.0.1", "agent")
        );
    }

    @Test
    void readClosedDecision_Succeeds() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(closedDecision));
        when(decisionAuthorizationService.canViewDecision(1L, 1L)).thenReturn(true);
        
        DecisionResponse responseObj = new DecisionResponse(1L, "Title", "Desc", null, null, null, DecisionStatus.CLOSED, null, VotingType.RATING_BASED, null, null, null, null, false, false);
        when(decisionMapper.toResponse(closedDecision)).thenReturn(responseObj);

        DecisionResponse result = decisionService.getDecisionById(1L);
        assertNotNull(result);
        assertEquals(DecisionStatus.CLOSED, result.status());
    }

    @Test
    void writeOptionOnClosedDecision_ThrowsDecisionClosedException() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(closedDecision));
        OptionCreateDto dto = new OptionCreateDto("Title", "Desc", null);

        assertThrows(DecisionClosedException.class, () ->
            decisionOptionService.createOption(1L, dto, "127.0.0.1", "agent")
        );
    }

    @Test
    void activeDecision_OperationsAllowed() {
        // Active, unlocked decision allows modifications
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(decisionRepository.findById(3L)).thenReturn(Optional.of(activeDecision));
        when(decisionAuthorizationService.canDeleteDecision(3L, 1L)).thenReturn(true);

        assertDoesNotThrow(() ->
            decisionService.deleteDecision(3L, "127.0.0.1", "agent")
        );
    }
}

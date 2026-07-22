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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionModerationLockTest {

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

    private Decision lockedDecision;
    private Decision unlockedDecision;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Set up the validator with mocked repository
        decisionModificationValidator = new DecisionModificationValidator(decisionRepository);
        
        // Re-inject validators
        injectField(decisionService, "decisionModificationValidator", decisionModificationValidator);
        injectField(decisionOptionService, "decisionModificationValidator", decisionModificationValidator);
        injectField(comparisonFactorService, "decisionModificationValidator", decisionModificationValidator);
        injectField(comparisonScoreService, "decisionModificationValidator", decisionModificationValidator);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");

        lockedDecision = new Decision();
        lockedDecision.setId(1L);
        lockedDecision.setLocked(true);
        lockedDecision.setVotingType(VotingType.RATING_BASED);
        lockedDecision.setStatus(DecisionStatus.ACTIVE);

        unlockedDecision = new Decision();
        unlockedDecision.setId(2L);
        unlockedDecision.setLocked(false);
        unlockedDecision.setVotingType(VotingType.RATING_BASED);
        unlockedDecision.setStatus(DecisionStatus.ACTIVE);
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
    void updateLockedDecision_ThrowsDecisionLockedException() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(lockedDecision));

        DecisionRequest request = new DecisionRequest("New Title", "Description", null, false, VotingType.RATING_BASED, null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), null, null, null);

        assertThrows(DecisionLockedException.class, () ->
            decisionService.updateDecision(1L, request, "127.0.0.1", "agent")
        );
    }

    @Test
    void deleteLockedDecision_ThrowsDecisionLockedException() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(lockedDecision));

        assertThrows(DecisionLockedException.class, () ->
            decisionService.deleteDecision(1L, "127.0.0.1", "agent")
        );
    }

    @Test
    void publishLockedDecision_ThrowsDecisionLockedException() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(lockedDecision));

        assertThrows(DecisionLockedException.class, () ->
            decisionService.publishDecision(1L, "127.0.0.1", "agent")
        );
    }

    @Test
    void createOptionOnLockedDecision_ThrowsDecisionLockedException() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(lockedDecision));

        OptionCreateDto dto = new OptionCreateDto("Option Title", "Description", null);

        assertThrows(DecisionLockedException.class, () ->
            decisionOptionService.createOption(1L, dto, "127.0.0.1", "agent")
        );
    }

    @Test
    void updateOptionOnLockedDecision_ThrowsDecisionLockedException() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(lockedDecision));

        OptionCreateDto dto = new OptionCreateDto("Option Title", "Description", null);

        assertThrows(DecisionLockedException.class, () ->
            decisionOptionService.updateOption(1L, 2L, dto, "127.0.0.1", "agent")
        );
    }

    @Test
    void deleteOptionOnLockedDecision_ThrowsDecisionLockedException() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(lockedDecision));

        assertThrows(DecisionLockedException.class, () ->
            decisionOptionService.deleteOption(1L, 2L, "127.0.0.1", "agent")
        );
    }

    @Test
    void createFactorOnLockedDecision_ThrowsDecisionLockedException() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(lockedDecision));

        ComparisonFactorRequest request = new ComparisonFactorRequest("Factor Name", "Description");

        assertThrows(DecisionLockedException.class, () ->
            comparisonFactorService.createFactor(1L, request, "127.0.0.1", "agent")
        );
    }

    @Test
    void updateFactorOnLockedDecision_ThrowsDecisionLockedException() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(lockedDecision));

        ComparisonFactorRequest request = new ComparisonFactorRequest("Factor Name", "Description");

        assertThrows(DecisionLockedException.class, () ->
            comparisonFactorService.updateFactor(1L, 2L, request, "127.0.0.1", "agent")
        );
    }

    @Test
    void deleteFactorOnLockedDecision_ThrowsDecisionLockedException() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(lockedDecision));

        assertThrows(DecisionLockedException.class, () ->
            comparisonFactorService.deleteFactor(1L, 2L, "127.0.0.1", "agent")
        );
    }

    @Test
    void submitScoreOnLockedDecision_ThrowsDecisionLockedException() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(lockedDecision));

        ComparisonScoreRequest request = new ComparisonScoreRequest(2L, 3L, 5, "Remark");

        assertThrows(DecisionLockedException.class, () ->
            comparisonScoreService.submitScore(1L, request, "127.0.0.1", "agent")
        );
    }

    @Test
    void updateScoreOnLockedDecision_ThrowsDecisionLockedException() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(lockedDecision));

        ComparisonScoreRequest request = new ComparisonScoreRequest(2L, 3L, 5, "Remark");

        assertThrows(DecisionLockedException.class, () ->
            comparisonScoreService.updateScore(1L, "some-id", request, "127.0.0.1", "agent")
        );
    }

    @Test
    void deleteScoreOnLockedDecision_ThrowsDecisionLockedException() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(lockedDecision));

        assertThrows(DecisionLockedException.class, () ->
            comparisonScoreService.deleteScore(1L, 2L, 3L, "127.0.0.1", "agent")
        );
    }

    @Test
    void getDecisionOnLockedDecision_Succeeds() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(lockedDecision));
        when(decisionAuthorizationService.canViewDecision(1L, 1L)).thenReturn(true);
        
        DecisionResponse responseObj = new DecisionResponse(1L, "Title", "Desc", null, null, null, DecisionStatus.ACTIVE, null, VotingType.RATING_BASED, null, null, null, null, false, true);
        when(decisionMapper.toResponse(lockedDecision)).thenReturn(responseObj);

        DecisionResponse result = decisionService.getDecisionById(1L);
        assertNotNull(result);
        assertTrue(result.locked());
    }

    @Test
    void unlockDecision_RestoresOperations() {
        // Unlocked decision should not throw lock exceptions
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(decisionRepository.findById(2L)).thenReturn(Optional.of(unlockedDecision));
        when(decisionAuthorizationService.canDeleteDecision(2L, 1L)).thenReturn(true);

        // Delete should execute successfully without throwing exception (though mock will hit repository calls, it verifies no DecisionLockedException is thrown)
        assertDoesNotThrow(() ->
            decisionService.deleteDecision(2L, "127.0.0.1", "agent")
        );
    }
}

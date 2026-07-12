package com.decisionhub.service;

import com.decisionhub.dto.request.decision.ComparisonScoreRequest;
import com.decisionhub.dto.response.decision.ComparisonScoreResponse;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.entity.decision.ComparisonScore;
import com.decisionhub.entity.decision.ComparisonScoreId;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.decision.ComparisonMapper;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.decision.ComparisonScoreRepository;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.ComparisonFactorRepository;
import com.decisionhub.repository.decision.DecisionOptionRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.security.decision.DecisionAuthorizationService;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.service.impl.decision.ComparisonScoreServiceImpl;
import com.decisionhub.service.interfaces.audit.AuditService;
import com.decisionhub.validator.decision.ComparisonScoreValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComparisonScoreServiceTest {

    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private DecisionOptionRepository decisionOptionRepository;
    @Mock
    private ComparisonFactorRepository comparisonFactorRepository;
    @Mock
    private ComparisonScoreRepository comparisonScoreRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ComparisonMapper comparisonMapper;
    @Mock
    private ComparisonScoreValidator comparisonScoreValidator;
    @Mock
    private DecisionAuthorizationService decisionAuthorizationService;
    @Mock
    private AuditService auditService;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ComparisonScoreServiceImpl comparisonScoreService;

    private Long boardId;
    private Long optionId;
    private Long factorId;
    private Long userId;

    private Decision board;
    private DecisionOption option;
    private ComparisonFactor factor;
    private ComparisonScore score;
    private User user;
    private ComparisonScoreRequest request;
    private ComparisonScoreResponse response;

    @BeforeEach
    void setUp() {
        boardId = 1L;
        optionId = 2L;
        factorId = 3L;
        userId = 4L;

        user = new User();
        user.setId(userId);
        user.setUsername("voter");

        board = new Decision();
        board.setId(boardId);
        board.setStatus(DecisionStatus.ACTIVE);

        option = new DecisionOption();
        option.setId(optionId);
        option.setDecision(board);

        factor = new ComparisonFactor();
        factor.setId(factorId);
        factor.setDecision(board);

        score = new ComparisonScore();
        score.setOption(option);
        score.setFactor(factor);
        score.setUser(user);
        score.setScore(85);
        score.setRemarks("Good startup");

        request = new ComparisonScoreRequest(optionId, factorId, 85, "Good startup");
        response = new ComparisonScoreResponse(optionId, factorId, userId, 85, "Good startup", Instant.now(), Instant.now());
    }

    @Test
    void testSubmitScore_Success_Create() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canSubmitScore(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(decisionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
        when(comparisonFactorRepository.findById(factorId)).thenReturn(Optional.of(factor));
        when(comparisonScoreRepository.findById(any(ComparisonScoreId.class))).thenReturn(Optional.empty());
        when(comparisonMapper.toEntity(request)).thenReturn(score);
        when(comparisonScoreRepository.saveAndFlush(score)).thenReturn(score);
        when(comparisonMapper.toResponse(score)).thenReturn(response);

        ComparisonScoreResponse result = comparisonScoreService.submitScore(boardId, request, "127.0.0.1", "Mozilla");

        assertNotNull(result);
        assertEquals(85, result.score());
        verify(comparisonScoreValidator).validateSubmit(board, option, factor, request);
        verify(comparisonScoreRepository).saveAndFlush(score);
        verify(auditService).log(eq(user), eq("SCORE_CREATED"), eq("comparison_scores"), eq(optionId), eq(null), anyString(), eq("127.0.0.1"), eq("Mozilla"));
    }

    @Test
    void testSubmitScore_Success_Update() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canSubmitScore(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(decisionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
        when(comparisonFactorRepository.findById(factorId)).thenReturn(Optional.of(factor));
        when(comparisonScoreRepository.findById(any(ComparisonScoreId.class))).thenReturn(Optional.of(score));
        when(comparisonScoreRepository.saveAndFlush(score)).thenReturn(score);
        when(comparisonMapper.toResponse(score)).thenReturn(response);

        ComparisonScoreResponse result = comparisonScoreService.submitScore(boardId, request, "127.0.0.1", "Mozilla");

        assertNotNull(result);
        verify(comparisonScoreValidator).validateSubmit(board, option, factor, request);
        verify(auditService).log(eq(user), eq("SCORE_UPDATED"), eq("comparison_scores"), eq(optionId), anyString(), anyString(), eq("127.0.0.1"), eq("Mozilla"));
    }

    @Test
    void testSubmitScore_Unauthorized() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.empty());

        assertThrows(UnauthorizedActionException.class, () ->
            comparisonScoreService.submitScore(boardId, request, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testSubmitScore_Forbidden() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canSubmitScore(boardId, userId)).thenReturn(false);

        assertThrows(UnauthorizedActionException.class, () ->
            comparisonScoreService.submitScore(boardId, request, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testSubmitScore_ValidationFails() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canSubmitScore(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(decisionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
        when(comparisonFactorRepository.findById(factorId)).thenReturn(Optional.of(factor));

        doThrow(new BadRequestException("Score must be between 0 and 100 inclusive"))
                .when(comparisonScoreValidator).validateSubmit(board, option, factor, request);

        assertThrows(BadRequestException.class, () ->
            comparisonScoreService.submitScore(boardId, request, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testUpdateScore_NotFound() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canSubmitScore(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(comparisonScoreRepository.findById(any(ComparisonScoreId.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            comparisonScoreService.updateScore(boardId, "dummyId", request, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testGetScores_Success() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canViewDecision(boardId, userId)).thenReturn(true);
        when(comparisonScoreRepository.findByOptionDecisionId(boardId)).thenReturn(Collections.singletonList(score));
        when(comparisonMapper.toResponse(score)).thenReturn(response);

        List<ComparisonScoreResponse> result = comparisonScoreService.getScoresByDecisionId(boardId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetMyScores_Success() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canViewDecision(boardId, userId)).thenReturn(true);
        when(comparisonScoreRepository.findByOptionDecisionIdAndUserId(boardId, userId)).thenReturn(Collections.singletonList(score));
        when(comparisonMapper.toResponse(score)).thenReturn(response);

        List<ComparisonScoreResponse> result = comparisonScoreService.getMyScoresByDecisionId(boardId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testDeleteScore_Success() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canSubmitScore(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(comparisonScoreRepository.findById(any(ComparisonScoreId.class))).thenReturn(Optional.of(score));

        comparisonScoreService.deleteScore(boardId, optionId, factorId, "127.0.0.1", "Mozilla");

        verify(comparisonScoreRepository).delete(score);
        verify(comparisonScoreRepository).flush();
        verify(auditService).log(eq(user), eq("SCORE_DELETED"), eq("comparison_scores"), eq(optionId), anyString(), eq(null), eq("127.0.0.1"), eq("Mozilla"));
    }

    @Test
    void testDeleteScore_NotFound() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canSubmitScore(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(comparisonScoreRepository.findById(any(ComparisonScoreId.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            comparisonScoreService.deleteScore(boardId, optionId, factorId, "127.0.0.1", "Mozilla")
        );
    }
}

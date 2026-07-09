package com.decisionhub.service;

import com.decisionhub.dto.ComparisonFactorRequest;
import com.decisionhub.dto.ComparisonFactorResponse;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.mapper.ComparisonMapper;
import com.decisionhub.repository.DecisionRepository;
import com.decisionhub.repository.ComparisonFactorRepository;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.security.AuthenticationFacade;
import com.decisionhub.security.DecisionAuthorizationService;
import com.decisionhub.service.impl.ComparisonFactorServiceImpl;
import com.decisionhub.validator.ComparisonFactorValidator;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComparisonFactorServiceTest {

    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private ComparisonFactorRepository comparisonFactorRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ComparisonMapper comparisonMapper;
    @Mock
    private ComparisonFactorValidator comparisonFactorValidator;
    @Mock
    private DecisionAuthorizationService decisionAuthorizationService;
    @Mock
    private AuditService auditService;
    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private ComparisonFactorServiceImpl comparisonFactorService;

    private Long boardId;
    private Long factorId;
    private Long userId;
    private Decision board;
    private ComparisonFactor factor;
    private User user;
    private ComparisonFactorRequest request;
    private ComparisonFactorResponse response;

    @BeforeEach
    void setUp() {
        boardId = 1L;
        factorId = 2L;
        userId = 3L;

        user = new User();
        user.setId(userId);
        user.setUsername("owner");

        board = new Decision();
        board.setId(boardId);
        board.setStatus(DecisionStatus.DRAFT);
        board.setCreator(user);

        factor = new ComparisonFactor();
        factor.setId(factorId);
        factor.setDecision(board);
        factor.setName("Performance");

        request = new ComparisonFactorRequest("Performance", "Factor description");
        response = new ComparisonFactorResponse(factorId, boardId, "Performance", "Factor description", Instant.now(), Instant.now(), 0L);
    }

    @Test
    void testCreateFactor_Success() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canManageComparisonFactors(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(comparisonMapper.toEntity(request)).thenReturn(factor);
        when(comparisonFactorRepository.saveAndFlush(factor)).thenReturn(factor);
        when(comparisonMapper.toResponse(factor)).thenReturn(response);

        ComparisonFactorResponse result = comparisonFactorService.createFactor(boardId, request, "127.0.0.1", "Mozilla");

        assertNotNull(result);
        assertEquals(factorId, result.id());
        verify(comparisonFactorValidator).validateCreate(board, request);
        verify(comparisonFactorRepository).saveAndFlush(factor);
        verify(auditService).log(eq(user), eq("FACTOR_CREATED"), eq("comparison_factors"), any(Long.class), eq(null), anyString(), eq("127.0.0.1"), eq("Mozilla"));
    }

    @Test
    void testCreateFactor_BoardNotFound() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            comparisonFactorService.createFactor(boardId, request, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testCreateFactor_Unauthorized() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.empty());

        assertThrows(UnauthorizedActionException.class, () -> 
            comparisonFactorService.createFactor(boardId, request, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testCreateFactor_Forbidden() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canManageComparisonFactors(boardId, userId)).thenReturn(false);

        assertThrows(UnauthorizedActionException.class, () -> 
            comparisonFactorService.createFactor(boardId, request, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testUpdateFactor_Success() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canManageComparisonFactors(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(comparisonFactorRepository.findById(factorId)).thenReturn(Optional.of(factor));
        when(comparisonFactorRepository.saveAndFlush(factor)).thenReturn(factor);
        when(comparisonMapper.toResponse(factor)).thenReturn(response);

        ComparisonFactorResponse result = comparisonFactorService.updateFactor(boardId, factorId, request, "127.0.0.1", "Mozilla");

        assertNotNull(result);
        verify(comparisonFactorValidator).validateUpdate(board, factor, request);
        verify(auditService).log(eq(user), eq("FACTOR_UPDATED"), eq("comparison_factors"), eq(factorId), anyString(), anyString(), eq("127.0.0.1"), eq("Mozilla"));
    }

    @Test
    void testUpdateFactor_BoardMismatch() {
        Decision anotherBoard = new Decision();
        anotherBoard.setId(99L);
        factor.setDecision(anotherBoard);

        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canManageComparisonFactors(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(comparisonFactorRepository.findById(factorId)).thenReturn(Optional.of(factor));

        assertThrows(BadRequestException.class, () -> 
            comparisonFactorService.updateFactor(boardId, factorId, request, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testDeleteFactor_Success() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canManageComparisonFactors(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(comparisonFactorRepository.findById(factorId)).thenReturn(Optional.of(factor));

        comparisonFactorService.deleteFactor(boardId, factorId, "127.0.0.1", "Mozilla");

        verify(comparisonFactorValidator).validateDelete(board);
        verify(comparisonFactorRepository).delete(factor);
        verify(auditService).log(eq(user), eq("FACTOR_DELETED"), eq("comparison_factors"), eq(factorId), anyString(), eq(null), eq("127.0.0.1"), eq("Mozilla"));
    }

    @Test
    void testGetFactors_Success() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canViewDecision(boardId, userId)).thenReturn(true);
        when(comparisonFactorRepository.findByDecisionId(boardId)).thenReturn(Collections.singletonList(factor));
        when(comparisonMapper.toResponse(factor)).thenReturn(response);

        List<ComparisonFactorResponse> results = comparisonFactorService.getFactorsByDecisionId(boardId);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(factorId, results.get(0).id());
    }
}

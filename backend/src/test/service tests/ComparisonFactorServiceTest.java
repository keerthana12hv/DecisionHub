package com.decisionhub.service;

import com.decisionhub.dto.ComparisonFactorRequest;
import com.decisionhub.dto.ComparisonFactorResponse;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.ComparisonFactor;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.entity.User;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ForbiddenException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedException;
import com.decisionhub.mapper.ComparisonMapper;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.ComparisonFactorRepository;
import com.decisionhub.repository.UserRepository;
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
import java.util.UUID;

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
    private DecisionBoardRepository decisionBoardRepository;
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

    private UUID boardId;
    private UUID factorId;
    private UUID userId;
    private DecisionBoard board;
    private ComparisonFactor factor;
    private User user;
    private ComparisonFactorRequest request;
    private ComparisonFactorResponse response;

    @BeforeEach
    void setUp() {
        boardId = UUID.randomUUID();
        factorId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setUsername("owner");

        board = new DecisionBoard();
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
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
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
        verify(auditService).log(eq(user), eq("FACTOR_CREATED"), eq("comparison_factors"), any(UUID.class), eq(null), anyString(), eq("127.0.0.1"), eq("Mozilla"));
    }

    @Test
    void testCreateFactor_BoardNotFound() {
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            comparisonFactorService.createFactor(boardId, request, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testCreateFactor_Unauthorized() {
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> 
            comparisonFactorService.createFactor(boardId, request, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testCreateFactor_Forbidden() {
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canManageComparisonFactors(boardId, userId)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> 
            comparisonFactorService.createFactor(boardId, request, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testUpdateFactor_Success() {
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
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
        DecisionBoard anotherBoard = new DecisionBoard();
        anotherBoard.setId(UUID.randomUUID());
        factor.setDecision(anotherBoard);

        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
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
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
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
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
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

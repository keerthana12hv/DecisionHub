package com.decisionhub.service;

import com.decisionhub.dto.FactorScoreDto;
import com.decisionhub.dto.OptionRankingDto;
import com.decisionhub.dto.OptionSummaryRankingDto;
import com.decisionhub.dto.RankingResponse;
import com.decisionhub.dto.RankingSummaryResponse;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.entity.decision.ComparisonScore;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.ComparisonFactorRepository;
import com.decisionhub.repository.ComparisonScoreRepository;
import com.decisionhub.repository.DecisionRepository;
import com.decisionhub.repository.DecisionOptionRepository;
import com.decisionhub.security.AuthenticationFacade;
import com.decisionhub.security.DecisionAuthorizationService;
import com.decisionhub.service.impl.RankingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private DecisionOptionRepository decisionOptionRepository;
    @Mock
    private ComparisonFactorRepository comparisonFactorRepository;
    @Mock
    private ComparisonScoreRepository comparisonScoreRepository;
    @Mock
    private DecisionAuthorizationService decisionAuthorizationService;
    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private RankingServiceImpl rankingService;

    private Long decisionId;
    private Decision board;
    private Long userId;
    private DecisionOption optionA;
    private DecisionOption optionB;
    private ComparisonFactor factor1;
    private ComparisonFactor factor2;

    @BeforeEach
    void setUp() {
        decisionId = 1L;
        userId = 2L;

        board = new Decision();
        board.setId(decisionId);
        board.setTitle("Test Decision Board");
        board.setStatus(DecisionStatus.ACTIVE);

        optionA = new DecisionOption();
        optionA.setId(10L);
        optionA.setOptionName("Option A");

        optionB = new DecisionOption();
        optionB.setId(20L);
        optionB.setOptionName("Option B");

        factor1 = new ComparisonFactor();
        factor1.setId(100L);
        factor1.setName("Factor 1");

        factor2 = new ComparisonFactor();
        factor2.setId(200L);
        factor2.setName("Factor 2");
    }

    @Test
    void testGetRanking_Success() {
        when(decisionRepository.findById(decisionId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canViewDecision(decisionId, userId)).thenReturn(true);
        when(decisionOptionRepository.findByDecisionId(decisionId)).thenReturn(Arrays.asList(optionA, optionB));
        when(comparisonFactorRepository.findByDecisionId(decisionId)).thenReturn(Arrays.asList(factor1, factor2));

        User user = new User();
        user.setId(userId);

        ComparisonScore scoreA1 = new ComparisonScore(optionA, factor1, user, 80, "Good A1", Instant.now(), Instant.now());
        ComparisonScore scoreA2 = new ComparisonScore(optionA, factor2, user, 90, "Great A2", Instant.now(), Instant.now());
        ComparisonScore scoreB1 = new ComparisonScore(optionB, factor1, user, 60, "Ok B1", Instant.now(), Instant.now());
        ComparisonScore scoreB2 = new ComparisonScore(optionB, factor2, user, 70, "Fine B2", Instant.now(), Instant.now());

        when(comparisonScoreRepository.findByOptionDecisionId(decisionId)).thenReturn(Arrays.asList(scoreA1, scoreA2, scoreB1, scoreB2));

        RankingResponse response = rankingService.getRanking(decisionId);

        assertNotNull(response);
        assertEquals(decisionId, response.decisionId());
        assertEquals("Test Decision Board", response.decisionTitle());
        assertEquals(DecisionStatus.ACTIVE, response.status());
        assertEquals(2, response.options().size());

        OptionRankingDto rankA = response.options().get(0);
        assertEquals(optionA.getId(), rankA.optionId());
        assertEquals(1, rankA.rank());
        assertEquals(170.0, rankA.score());
        assertFalse(rankA.isTied());

        OptionRankingDto rankB = response.options().get(1);
        assertEquals(optionB.getId(), rankB.optionId());
        assertEquals(2, rankB.rank());
        assertEquals(130.0, rankB.score());
        assertFalse(rankB.isTied());
    }

    @Test
    void testGetRanking_StandardCompetitionTies() {
        when(decisionRepository.findById(decisionId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canViewDecision(decisionId, userId)).thenReturn(true);
        when(decisionOptionRepository.findByDecisionId(decisionId)).thenReturn(Arrays.asList(optionA, optionB));
        when(comparisonFactorRepository.findByDecisionId(decisionId)).thenReturn(Arrays.asList(factor1, factor2));

        User user = new User();
        user.setId(userId);

        ComparisonScore scoreA1 = new ComparisonScore(optionA, factor1, user, 80, "", Instant.now(), Instant.now());
        ComparisonScore scoreA2 = new ComparisonScore(optionA, factor2, user, 80, "", Instant.now(), Instant.now());
        ComparisonScore scoreB1 = new ComparisonScore(optionB, factor1, user, 80, "", Instant.now(), Instant.now());
        ComparisonScore scoreB2 = new ComparisonScore(optionB, factor2, user, 80, "", Instant.now(), Instant.now());

        when(comparisonScoreRepository.findByOptionDecisionId(decisionId)).thenReturn(Arrays.asList(scoreA1, scoreA2, scoreB1, scoreB2));

        RankingResponse response = rankingService.getRanking(decisionId);

        assertEquals(1, response.options().get(0).rank());
        assertEquals(1, response.options().get(1).rank());
        assertTrue(response.options().get(0).isTied());
        assertTrue(response.options().get(1).isTied());
        
        assertEquals(optionA.getId(), response.options().get(0).optionId());
        assertEquals(optionB.getId(), response.options().get(1).optionId());
    }

    @Test
    void testGetRanking_EmptyScores() {
        when(decisionRepository.findById(decisionId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canViewDecision(decisionId, userId)).thenReturn(true);
        when(decisionOptionRepository.findByDecisionId(decisionId)).thenReturn(Arrays.asList(optionA));
        when(comparisonFactorRepository.findByDecisionId(decisionId)).thenReturn(Arrays.asList(factor1));
        when(comparisonScoreRepository.findByOptionDecisionId(decisionId)).thenReturn(Collections.emptyList());

        RankingResponse response = rankingService.getRanking(decisionId);

        assertNotNull(response);
        assertEquals(1, response.options().size());
        assertEquals(0.0, response.options().get(0).score());
        assertEquals(0.0, response.options().get(0).factorBreakdown().get(0).averageScore());
    }

    @Test
    void testGetRanking_BoardNotActive() {
        board.setStatus(DecisionStatus.DRAFT);
        when(decisionRepository.findById(decisionId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canViewDecision(decisionId, userId)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> rankingService.getRanking(decisionId));
    }

    @Test
    void testGetRanking_NoOptions() {
        when(decisionRepository.findById(decisionId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canViewDecision(decisionId, userId)).thenReturn(true);
        when(decisionOptionRepository.findByDecisionId(decisionId)).thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () -> rankingService.getRanking(decisionId));
    }

    @Test
    void testGetRanking_NoFactors() {
        when(decisionRepository.findById(decisionId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canViewDecision(decisionId, userId)).thenReturn(true);
        when(decisionOptionRepository.findByDecisionId(decisionId)).thenReturn(Arrays.asList(optionA));
        when(comparisonFactorRepository.findByDecisionId(decisionId)).thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () -> rankingService.getRanking(decisionId));
    }

    @Test
    void testGetRanking_Forbidden() {
        when(decisionRepository.findById(decisionId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canViewDecision(decisionId, userId)).thenReturn(false);

        assertThrows(UnauthorizedActionException.class, () -> rankingService.getRanking(decisionId));
    }

    @Test
    void testGetRankingSummary_Success() {
        when(decisionRepository.findById(decisionId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canViewDecision(decisionId, userId)).thenReturn(true);
        when(decisionOptionRepository.findByDecisionId(decisionId)).thenReturn(Arrays.asList(optionA, optionB));
        when(comparisonFactorRepository.findByDecisionId(decisionId)).thenReturn(Arrays.asList(factor1));

        User user = new User();
        user.setId(userId);
        ComparisonScore scoreA = new ComparisonScore(optionA, factor1, user, 95, "", Instant.now(), Instant.now());
        ComparisonScore scoreB = new ComparisonScore(optionB, factor1, user, 85, "", Instant.now(), Instant.now());
        when(comparisonScoreRepository.findByOptionDecisionId(decisionId)).thenReturn(Arrays.asList(scoreA, scoreB));

        RankingSummaryResponse response = rankingService.getRankingSummary(decisionId);

        assertNotNull(response);
        assertEquals(2, response.options().size());
        assertEquals(optionA.getId(), response.options().get(0).optionId());
        assertEquals(1, response.options().get(0).rank());
        assertEquals(95.0, response.options().get(0).score());
    }
}

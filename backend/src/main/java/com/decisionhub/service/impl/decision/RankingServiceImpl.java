package com.decisionhub.service.impl.decision;

import com.decisionhub.dto.response.decision.FactorScoreDto;
import com.decisionhub.dto.response.decision.OptionRankingDto;
import com.decisionhub.dto.response.decision.OptionSummaryRankingDto;
import com.decisionhub.dto.response.decision.RankingResponse;
import com.decisionhub.dto.response.decision.RankingSummaryResponse;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.entity.decision.ComparisonScore;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.repository.decision.ComparisonScoreRepository;
import com.decisionhub.repository.decision.ComparisonFactorRepository;
import com.decisionhub.repository.decision.DecisionOptionRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.security.decision.DecisionAuthorizationService;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.service.interfaces.decision.RankingService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation for the Decision Ranking Engine.
 * Fully stateless computation layer for collaborative rankings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RankingServiceImpl implements RankingService {

    private final DecisionRepository decisionRepository;
    private final DecisionOptionRepository decisionOptionRepository;
    private final ComparisonFactorRepository comparisonFactorRepository;
    private final ComparisonScoreRepository comparisonScoreRepository;
    private final DecisionAuthorizationService decisionAuthorizationService;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Transactional(readOnly = true)
    public RankingResponse getRanking(Long decisionId) {
        log.info("Calculating decision rankings for ID: {}", decisionId);
        List<IntermediateOptionRank> intermediateList = computeIntermediateRankings(decisionId);

        List<OptionRankingDto> optionDtos = intermediateList.stream()
                .map(item -> new OptionRankingDto(
                        item.getOptionId(),
                        item.getOptionTitle(),
                        item.getRank(),
                        item.getScore(),
                        item.getFactorBreakdown(),
                        item.isTied()
                ))
                .collect(Collectors.toList());

        Decision board = getBoardOrThrow(decisionId);

        return new RankingResponse(
                board.getId(),
                board.getTitle(),
                board.getStatus(),
                Instant.now(),
                optionDtos
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RankingSummaryResponse getRankingSummary(Long decisionId) {
        log.info("Calculating decision summary rankings for ID: {}", decisionId);
        List<IntermediateOptionRank> intermediateList = computeIntermediateRankings(decisionId);

        List<OptionSummaryRankingDto> optionDtos = intermediateList.stream()
                .map(item -> new OptionSummaryRankingDto(
                        item.getOptionId(),
                        item.getOptionTitle(),
                        item.getRank(),
                        item.getScore()
                ))
                .collect(Collectors.toList());

        Decision board = getBoardOrThrow(decisionId);

        return new RankingSummaryResponse(
                board.getId(),
                board.getTitle(),
                board.getStatus(),
                Instant.now(),
                optionDtos
        );
    }

    private List<IntermediateOptionRank> computeIntermediateRankings(Long decisionId) {
        // 1. Fetch & Validate Decision
        Decision board = getBoardOrThrow(decisionId);

        if (board.getVotingType() != com.decisionhub.enums.decision.VotingType.RATING_BASED) {
            throw new BadRequestException("Ranking calculation is only supported for RATING_BASED decisions");
        }

        // 2. Authorize requester
        Long currentUserId = authenticationFacade.getCurrentUserId().orElse(null);
        if (!decisionAuthorizationService.canViewDecision(decisionId, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to view this decision ranking");
        }

        // 3. Reject if not ACTIVE
        if (board.getStatus() != DecisionStatus.ACTIVE) {
            throw new BadRequestException("Ranking can only be generated for ACTIVE decisions");
        }

        // 4. Fetch Options & Factors
        List<DecisionOption> options = decisionOptionRepository.findByDecisionId(decisionId);
        if (options.isEmpty()) {
            throw new BadRequestException("Decision has no active options");
        }

        List<ComparisonFactor> factors = comparisonFactorRepository.findByDecisionId(decisionId);
        if (factors.isEmpty()) {
            throw new BadRequestException("Decision has no comparison factors configured");
        }

        // 5. Fetch all comparison scores
        List<ComparisonScore> scores = comparisonScoreRepository.findByOptionDecisionId(decisionId);

        // Group scores by Option ID and Factor ID
        Map<Long, Map<Long, List<ComparisonScore>>> scoresMap = scores.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getOption().getId(),
                        Collectors.groupingBy(s -> s.getFactor().getId())
                ));

        // Configurable factor weights (currently default to 1.0)
        double defaultWeight = 1.0;

        List<IntermediateOptionRank> intermediateList = new ArrayList<>();

        for (DecisionOption option : options) {
            double totalWeightedScore = 0.0;
            List<FactorScoreDto> breakdown = new ArrayList<>();

            Map<Long, List<ComparisonScore>> optionScores = scoresMap.getOrDefault(option.getId(), Collections.emptyMap());

            for (ComparisonFactor factor : factors) {
                List<ComparisonScore> factorScores = optionScores.getOrDefault(factor.getId(), Collections.emptyList());

                // Calculate average (default to 0.0 if no scores exist)
                double average = 0.0;
                if (!factorScores.isEmpty()) {
                    double sum = 0.0;
                    for (ComparisonScore score : factorScores) {
                        sum += score.getScore();
                    }
                    average = sum / factorScores.size();
                }

                double weight = defaultWeight; // Configurable weights support
                double weighted = average * weight;
                totalWeightedScore += weighted;

                breakdown.add(new FactorScoreDto(
                        factor.getId(),
                        factor.getName(),
                        average,
                        weight,
                        weighted
                ));
            }

            intermediateList.add(new IntermediateOptionRank(
                    option.getId(),
                    option.getOptionName(),
                    totalWeightedScore,
                    breakdown,
                    1,
                    false
            ));
        }

        // 6. Stable Sorting (Complexity O(N log N))
        // Tie-breaking order:
        //   1. Weighted Score descending
        //   2. Option ID ascending (older first since IDs are auto-incrementing)
        intermediateList.sort((a, b) -> {
            int cmp = Double.compare(b.getScore(), a.getScore()); // descending
            if (cmp != 0) {
                return cmp;
            }
            return a.getOptionId().compareTo(b.getOptionId()); // stable ordering by auto-increment ID
        });

        // 7. Standard Competition Ranking (1-2-2-4)
        int rank = 1;
        int count = 1;
        double prevScore = -1.0;
        for (int i = 0; i < intermediateList.size(); i++) {
            IntermediateOptionRank item = intermediateList.get(i);
            if (i > 0 && item.getScore() < prevScore) {
                rank = count;
            }
            item.setRank(rank);
            prevScore = item.getScore();
            count++;
        }

        // 8. Flag ties (based on identical scores)
        Map<Double, Long> scoreCounts = intermediateList.stream()
                .collect(Collectors.groupingBy(IntermediateOptionRank::getScore, Collectors.counting()));
        for (IntermediateOptionRank item : intermediateList) {
            item.setTied(scoreCounts.get(item.getScore()) > 1);
        }

        return intermediateList;
    }

    private Decision getBoardOrThrow(Long id) {
        return decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with ID: " + id));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class IntermediateOptionRank {
        private final Long optionId;
        private final String optionTitle;
        private final double score;
        private final List<FactorScoreDto> factorBreakdown;
        private int rank;
        private boolean isTied;
    }
}

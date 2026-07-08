package com.decisionhub.service.impl;

import com.decisionhub.dto.FactorScoreDto;
import com.decisionhub.dto.OptionRankingDto;
import com.decisionhub.dto.OptionSummaryRankingDto;
import com.decisionhub.dto.RankingResponse;
import com.decisionhub.dto.RankingSummaryResponse;
import com.decisionhub.entity.ComparisonFactor;
import com.decisionhub.entity.ComparisonScore;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.DecisionOption;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ForbiddenException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.ComparisonFactorRepository;
import com.decisionhub.repository.ComparisonScoreRepository;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.DecisionOptionRepository;
import com.decisionhub.security.AuthenticationFacade;
import com.decisionhub.security.DecisionAuthorizationService;
import com.decisionhub.service.RankingService;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for the Decision Ranking Engine.
 * Fully stateless computation layer for collaborative rankings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RankingServiceImpl implements RankingService {

    private final DecisionBoardRepository decisionBoardRepository;
    private final DecisionOptionRepository decisionOptionRepository;
    private final ComparisonFactorRepository comparisonFactorRepository;
    private final ComparisonScoreRepository comparisonScoreRepository;
    private final DecisionAuthorizationService decisionAuthorizationService;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Transactional(readOnly = true)
    public RankingResponse getRanking(UUID decisionId) {
        log.info("Calculating decision board rankings for ID: {}", decisionId);
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

        DecisionBoard board = getBoardOrThrow(decisionId);

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
    public RankingSummaryResponse getRankingSummary(UUID decisionId) {
        log.info("Calculating decision board summary rankings for ID: {}", decisionId);
        List<IntermediateOptionRank> intermediateList = computeIntermediateRankings(decisionId);

        List<OptionSummaryRankingDto> optionDtos = intermediateList.stream()
                .map(item -> new OptionSummaryRankingDto(
                        item.getOptionId(),
                        item.getOptionTitle(),
                        item.getRank(),
                        item.getScore()
                ))
                .collect(Collectors.toList());

        DecisionBoard board = getBoardOrThrow(decisionId);

        return new RankingSummaryResponse(
                board.getId(),
                board.getTitle(),
                board.getStatus(),
                Instant.now(),
                optionDtos
        );
    }

    private List<IntermediateOptionRank> computeIntermediateRankings(UUID decisionId) {
        // 1. Fetch & Validate DecisionBoard
        DecisionBoard board = getBoardOrThrow(decisionId);

        // 2. Authorize requester
        UUID currentUserId = authenticationFacade.getCurrentUserId().orElse(null);
        if (!decisionAuthorizationService.canViewDecision(decisionId, currentUserId)) {
            throw new ForbiddenException("Not authorized to view this decision board ranking");
        }

        // 3. Reject if not ACTIVE
        if (board.getStatus() != DecisionStatus.ACTIVE) {
            throw new BadRequestException("Ranking can only be generated for ACTIVE decision boards");
        }

        // 4. Fetch Options & Factors
        List<DecisionOption> options = decisionOptionRepository.findByDecisionIdAndDeletedAtIsNull(decisionId);
        if (options.isEmpty()) {
            throw new BadRequestException("Decision board has no active options");
        }

        List<ComparisonFactor> factors = comparisonFactorRepository.findByDecisionId(decisionId);
        if (factors.isEmpty()) {
            throw new BadRequestException("Decision board has no comparison factors configured");
        }

        // 5. Fetch all comparison scores (Batch retrieve O(1) query count to prevent N+1)
        List<ComparisonScore> scores = comparisonScoreRepository.findByOptionDecisionId(decisionId);

        // Group scores by Option ID and Factor ID
        Map<UUID, Map<UUID, List<ComparisonScore>>> scoresMap = scores.stream()
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

            Map<UUID, List<ComparisonScore>> optionScores = scoresMap.getOrDefault(option.getId(), Collections.emptyMap());

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
                    option.getTitle(),
                    option.getCreatedAt() != null ? option.getCreatedAt() : Instant.EPOCH,
                    totalWeightedScore,
                    breakdown,
                    1,
                    false
            ));
        }

        // 6. Stable Sorting (Complexity O(N log N))
        // Tie-breaking order:
        //   1. Weighted Score descending
        //   2. Option createdAt ascending (oldest first)
        //   3. Option UUID ascending (lexicographical stable ordering)
        intermediateList.sort((a, b) -> {
            int cmp = Double.compare(b.getScore(), a.getScore()); // descending
            if (cmp != 0) {
                return cmp;
            }
            cmp = a.getCreatedAt().compareTo(b.getCreatedAt()); // oldest first
            if (cmp != 0) {
                return cmp;
            }
            return a.getOptionId().compareTo(b.getOptionId()); // lexicographical stable UUID ordering
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

    private DecisionBoard getBoardOrThrow(UUID id) {
        DecisionBoard board = decisionBoardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision board not found with ID: " + id));
        if (board.isDeleted()) {
            throw new ResourceNotFoundException("Decision board not found with ID: " + id);
        }
        return board;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class IntermediateOptionRank {
        private final UUID optionId;
        private final String optionTitle;
        private final Instant createdAt;
        private final double score;
        private final List<FactorScoreDto> factorBreakdown;
        private int rank;
        private boolean isTied;
    }
}

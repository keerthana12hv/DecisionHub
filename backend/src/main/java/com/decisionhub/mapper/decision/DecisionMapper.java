package com.decisionhub.mapper.decision;

import com.decisionhub.dto.request.decision.DecisionRequest;
import com.decisionhub.dto.request.decision.OptionCreateDto;
import com.decisionhub.dto.response.decision.ComparisonFactorResponse;
import com.decisionhub.dto.response.decision.ComparisonScoreResponse;
import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.dto.response.decision.OptionResponseDto;
import com.decisionhub.entity.decision.ComparisonScore;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.mapper.authentication.UserMapper;
import com.decisionhub.repository.voting.VoteRepository;
import com.decisionhub.dto.response.authentication.UserResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hand-written mapper (replaces MapStruct abstract class).
 *
 * MapStruct abstract classes do not reliably pick up concrete methods as
 * sub-mappers for collection elements, so the options list was always empty.
 * This plain Spring @Component gives us full control.
 */
@Component
public class DecisionMapper {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ComparisonMapper comparisonMapper;

    // ── Decision → DecisionResponse ──────────────────────────────────────────

    public DecisionResponse toResponse(Decision decision) {
        if (decision == null) return null;

        UserResponse creator = userMapper.toResponse(decision.getCreator());

        String categoryName  = decision.getCommunity() != null
                && decision.getCommunity().getCategory() != null
                ? decision.getCommunity().getCategory().getName()
                : null;

        String communityName = decision.getCommunity() != null
                ? decision.getCommunity().getName()
                : null;

        List<OptionResponseDto> options = decision.getOptions() == null
                ? Collections.emptyList()
                : decision.getOptions().stream()
                        .map(this::toResponseDto)
                        .collect(Collectors.toList());

        List<ComparisonFactorResponse> factors = decision.getComparisonFactors() == null
                ? Collections.emptyList()
                : decision.getComparisonFactors().stream()
                        .map(comparisonMapper::toResponse)
                        .collect(Collectors.toList());

        return new DecisionResponse(
                decision.getId(),
                decision.getTitle(),
                decision.getDescription(),
                creator,
                categoryName,
                communityName,
                decision.getStatus(),
                decision.getDeadline(),
                decision.getVotingType(),
                decision.getVotingEndTime(),
                options,
                factors,
                decision.getCreatedAt(),
                decision.isPinned(),
                decision.isLocked()
        );
    }

    // ── DecisionOption → OptionResponseDto ──────────────────────────────────

    public OptionResponseDto toResponseDto(DecisionOption option) {
        if (option == null) return null;

        List<ComparisonScoreResponse> scores = option.getComparisonScores() == null
                ? Collections.emptyList()
                : option.getComparisonScores().stream()
                        .map(this::mapScore)
                        .collect(Collectors.toList());

        long voteCount = voteRepository != null
                ? voteRepository.countByDecisionOptionId(option.getId())
                : 0L;

        return new OptionResponseDto(
                option.getId(),
                option.getOptionName(),
                option.getDescription(),
                scores,
                voteCount
        );
    }

    // ── DecisionRequest → Decision entity ────────────────────────────────────

    public Decision toEntity(DecisionRequest request) {
        if (request == null) return null;
        Decision decision = new Decision();
        decision.setTitle(request.title());
        decision.setDescription(request.description());
        decision.setVotingType(request.votingType());
        decision.setVotingEndTime(request.votingEndTime());
        decision.setDeadline(request.deadline());
        return decision;
    }

    // ── OptionCreateDto → DecisionOption entity ───────────────────────────────

    public DecisionOption toEntity(OptionCreateDto dto) {
        if (dto == null) return null;
        DecisionOption opt = new DecisionOption();
        opt.setOptionName(dto.title());
        opt.setDescription(dto.description());
        return opt;
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private ComparisonScoreResponse mapScore(ComparisonScore s) {
        return new ComparisonScoreResponse(
                s.getOption() != null ? s.getOption().getId()  : null,
                s.getFactor() != null ? s.getFactor().getId()  : null,
                s.getUser()   != null ? s.getUser().getId()    : null,
                s.getScore(),
                s.getRemarks(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}

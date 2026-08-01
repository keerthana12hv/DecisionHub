package com.decisionhub.mapper.decision;

import com.decisionhub.dto.request.decision.ComparisonFactorRequest;
import com.decisionhub.dto.request.decision.ComparisonScoreRequest;
import com.decisionhub.dto.response.decision.ComparisonFactorResponse;
import com.decisionhub.dto.response.decision.ComparisonScoreResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.entity.decision.ComparisonScore;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-01T15:27:34+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.12 (Oracle Corporation)"
)
@Component
public class ComparisonMapperImpl implements ComparisonMapper {

    @Override
    public ComparisonFactorResponse toResponse(ComparisonFactor factor) {
        if ( factor == null ) {
            return null;
        }

        Long decisionId = null;
        Long id = null;
        String name = null;
        String description = null;

        decisionId = factorDecisionId( factor );
        id = factor.getId();
        name = factor.getName();
        description = factor.getDescription();

        Instant createdAt = null;
        Instant updatedAt = null;
        Long version = null;

        ComparisonFactorResponse comparisonFactorResponse = new ComparisonFactorResponse( id, decisionId, name, description, createdAt, updatedAt, version );

        return comparisonFactorResponse;
    }

    @Override
    public ComparisonFactor toEntity(ComparisonFactorRequest request) {
        if ( request == null ) {
            return null;
        }

        ComparisonFactor comparisonFactor = new ComparisonFactor();

        comparisonFactor.setName( request.name() );
        comparisonFactor.setDescription( request.description() );

        return comparisonFactor;
    }

    @Override
    public ComparisonScoreResponse toResponse(ComparisonScore score) {
        if ( score == null ) {
            return null;
        }

        Long optionId = null;
        Long factorId = null;
        Long userId = null;
        int score1 = 0;
        String remarks = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        optionId = scoreOptionId( score );
        factorId = scoreFactorId( score );
        userId = scoreUserId( score );
        score1 = score.getScore();
        remarks = score.getRemarks();
        createdAt = score.getCreatedAt();
        updatedAt = score.getUpdatedAt();

        ComparisonScoreResponse comparisonScoreResponse = new ComparisonScoreResponse( optionId, factorId, userId, score1, remarks, createdAt, updatedAt );

        return comparisonScoreResponse;
    }

    @Override
    public ComparisonScore toEntity(ComparisonScoreRequest request) {
        if ( request == null ) {
            return null;
        }

        ComparisonScore.ComparisonScoreBuilder comparisonScore = ComparisonScore.builder();

        comparisonScore.score( request.score() );
        comparisonScore.remarks( request.remarks() );

        return comparisonScore.build();
    }

    private Long factorDecisionId(ComparisonFactor comparisonFactor) {
        if ( comparisonFactor == null ) {
            return null;
        }
        Decision decision = comparisonFactor.getDecision();
        if ( decision == null ) {
            return null;
        }
        Long id = decision.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long scoreOptionId(ComparisonScore comparisonScore) {
        if ( comparisonScore == null ) {
            return null;
        }
        DecisionOption option = comparisonScore.getOption();
        if ( option == null ) {
            return null;
        }
        Long id = option.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long scoreFactorId(ComparisonScore comparisonScore) {
        if ( comparisonScore == null ) {
            return null;
        }
        ComparisonFactor factor = comparisonScore.getFactor();
        if ( factor == null ) {
            return null;
        }
        Long id = factor.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long scoreUserId(ComparisonScore comparisonScore) {
        if ( comparisonScore == null ) {
            return null;
        }
        User user = comparisonScore.getUser();
        if ( user == null ) {
            return null;
        }
        Long id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}

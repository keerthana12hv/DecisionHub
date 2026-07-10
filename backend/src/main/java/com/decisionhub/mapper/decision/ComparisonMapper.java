package com.decisionhub.mapper.decision;

import com.decisionhub.dto.request.decision.ComparisonFactorRequest;
import com.decisionhub.dto.request.decision.ComparisonScoreRequest;
import com.decisionhub.dto.response.decision.ComparisonFactorResponse;
import com.decisionhub.dto.response.decision.ComparisonScoreResponse;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.entity.decision.ComparisonScore;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ComparisonMapper {

    @Mapping(target = "decisionId", source = "decision.id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    ComparisonFactorResponse toResponse(ComparisonFactor factor);

    @Mapping(target = "decision", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "optionScores", ignore = true)
    ComparisonFactor toEntity(ComparisonFactorRequest request);

    @Mapping(target = "optionId", source = "option.id")
    @Mapping(target = "factorId", source = "factor.id")
    @Mapping(target = "userId", source = "user.id")
    ComparisonScoreResponse toResponse(ComparisonScore score);

    @Mapping(target = "option", ignore = true)
    @Mapping(target = "factor", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ComparisonScore toEntity(ComparisonScoreRequest request);
}

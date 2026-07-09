package com.decisionhub.mapper;

import com.decisionhub.dto.ComparisonFactorRequest;
import com.decisionhub.dto.ComparisonFactorResponse;
import com.decisionhub.dto.ComparisonScoreRequest;
import com.decisionhub.dto.ComparisonScoreResponse;
import com.decisionhub.entity.ComparisonFactor;
import com.decisionhub.entity.ComparisonScore;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ComparisonMapper {

    @Mapping(target = "decisionId", source = "decision.id")
    ComparisonFactorResponse toResponse(ComparisonFactor factor);

    @Mapping(target = "decision", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
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

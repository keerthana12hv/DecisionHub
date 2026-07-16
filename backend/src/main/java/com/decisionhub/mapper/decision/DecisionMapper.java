package com.decisionhub.mapper.decision;

import com.decisionhub.dto.request.decision.DecisionRequest;
import com.decisionhub.dto.request.decision.OptionCreateDto;
import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.dto.response.decision.OptionResponseDto;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.mapper.authentication.UserMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class, ComparisonMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DecisionMapper {

    @Mapping(target = "categoryName", source = "community.category.name")
    @Mapping(target = "communityName", source = "community.name")
    @Mapping(target = "factors", source = "comparisonFactors")
    DecisionResponse toResponse(Decision decision);

    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "community", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "comparisonFactors", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Decision toEntity(DecisionRequest request);

    // Option mapping
    @Mapping(target = "decision", ignore = true)
    @Mapping(target = "factorScores", ignore = true)
    @Mapping(target = "prosCons", ignore = true)
    @Mapping(target = "votes", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "optionName", source = "title")
    DecisionOption toEntity(OptionCreateDto dto);

    @Mapping(target = "title", source = "optionName")
    @Mapping(target = "comparisonScores", ignore = true)
    OptionResponseDto toResponseDto(DecisionOption option);
}

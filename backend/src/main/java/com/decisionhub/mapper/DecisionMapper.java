package com.decisionhub.mapper;

import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.DecisionResponse;
import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.OptionResponseDto;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class, ComparisonMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DecisionMapper {

    @Mapping(target = "categoryName", ignore = true)
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

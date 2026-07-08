package com.decisionhub.mapper;

import com.decisionhub.dto.CriteriaDto;
import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.DecisionResponse;
import com.decisionhub.dto.DecisionSummaryDto;
import com.decisionhub.dto.DecisionUpdateRequest;
import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.OptionResponseDto;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.DecisionOption;
import com.decisionhub.entity.OptionCriteria;
import com.decisionhub.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class, PollMapper.class, ComparisonMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DecisionMapper {

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "communityName", source = "community.name")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "factors", source = "comparisonFactors")
    DecisionResponse toResponse(DecisionBoard decisionBoard);

    DecisionSummaryDto toSummary(DecisionBoard decisionBoard);

    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "community", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "polls", ignore = true)
    @Mapping(target = "comparisonFactors", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    DecisionBoard toEntity(DecisionRequest request);

    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "community", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "polls", ignore = true)
    @Mapping(target = "comparisonFactors", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(DecisionUpdateRequest request, @MappingTarget DecisionBoard decisionBoard);

    // Option mapping
    @Mapping(target = "decision", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "comparisonScores", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    DecisionOption toEntity(OptionCreateDto dto);

    OptionResponseDto toResponseDto(DecisionOption option);

    // Criteria mapping
    @Mapping(target = "option", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    OptionCriteria toEntity(CriteriaDto dto);

    CriteriaDto toResponseDto(OptionCriteria criteria);

    // Helper conversion for Set<Tag> to Set<String>
    default Set<String> mapTags(Set<Tag> tags) {
        if (tags == null) return null;
        return tags.stream().map(Tag::getName).collect(Collectors.toSet());
    }
}

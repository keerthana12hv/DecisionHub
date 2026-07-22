package com.decisionhub.mapper.voting;

import com.decisionhub.dto.response.voting.PollResponse;
import com.decisionhub.entity.voting.Poll;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting Poll entities to Poll response DTOs.
 */
@Mapper(componentModel = "spring")
public interface PollMapper {

    @Mapping(target = "decisionId", source = "decision.id")
    PollResponse toResponse(Poll poll);
}
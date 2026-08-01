package com.decisionhub.mapper.decision;

import com.decisionhub.dto.request.decision.DecisionRequest;
import com.decisionhub.dto.request.decision.OptionCreateDto;
import com.decisionhub.dto.response.authentication.UserResponse;
import com.decisionhub.dto.response.decision.ComparisonFactorResponse;
import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.dto.response.decision.OptionResponseDto;
import com.decisionhub.entity.community.Category;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.enums.decision.VotingType;
import com.decisionhub.mapper.authentication.UserMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-01T15:27:34+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.12 (Oracle Corporation)"
)
@Component
public class DecisionMapperImpl extends DecisionMapper {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ComparisonMapper comparisonMapper;

    @Override
    public DecisionResponse toResponse(Decision decision) {
        if ( decision == null ) {
            return null;
        }

        String categoryName = null;
        String communityName = null;
        List<ComparisonFactorResponse> factors = null;
        Long id = null;
        String title = null;
        String description = null;
        UserResponse creator = null;
        DecisionStatus status = null;
        LocalDateTime deadline = null;
        VotingType votingType = null;
        LocalDateTime votingEndTime = null;
        List<OptionResponseDto> options = null;
        LocalDateTime createdAt = null;
        boolean pinned = false;
        boolean locked = false;

        categoryName = decisionCommunityCategoryName( decision );
        communityName = decisionCommunityName( decision );
        factors = comparisonFactorListToComparisonFactorResponseList( decision.getComparisonFactors() );
        id = decision.getId();
        title = decision.getTitle();
        description = decision.getDescription();
        creator = userMapper.toResponse( decision.getCreator() );
        status = decision.getStatus();
        deadline = decision.getDeadline();
        votingType = decision.getVotingType();
        votingEndTime = decision.getVotingEndTime();
        options = decisionOptionListToOptionResponseDtoList( decision.getOptions() );
        createdAt = decision.getCreatedAt();
        pinned = decision.isPinned();
        locked = decision.isLocked();

        DecisionResponse decisionResponse = new DecisionResponse( id, title, description, creator, categoryName, communityName, status, deadline, votingType, votingEndTime, options, factors, createdAt, pinned, locked );

        return decisionResponse;
    }

    @Override
    public Decision toEntity(DecisionRequest request) {
        if ( request == null ) {
            return null;
        }

        Decision decision = new Decision();

        decision.setTitle( request.title() );
        decision.setDescription( request.description() );
        decision.setDeadline( request.deadline() );
        decision.setVotingType( request.votingType() );
        decision.setVotingEndTime( request.votingEndTime() );

        return decision;
    }

    @Override
    public DecisionOption toEntity(OptionCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        DecisionOption decisionOption = new DecisionOption();

        decisionOption.setOptionName( dto.title() );
        decisionOption.setDescription( dto.description() );

        return decisionOption;
    }

    private String decisionCommunityCategoryName(Decision decision) {
        if ( decision == null ) {
            return null;
        }
        Community community = decision.getCommunity();
        if ( community == null ) {
            return null;
        }
        Category category = community.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String decisionCommunityName(Decision decision) {
        if ( decision == null ) {
            return null;
        }
        Community community = decision.getCommunity();
        if ( community == null ) {
            return null;
        }
        String name = community.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    protected List<ComparisonFactorResponse> comparisonFactorListToComparisonFactorResponseList(List<ComparisonFactor> list) {
        if ( list == null ) {
            return null;
        }

        List<ComparisonFactorResponse> list1 = new ArrayList<ComparisonFactorResponse>( list.size() );
        for ( ComparisonFactor comparisonFactor : list ) {
            list1.add( comparisonMapper.toResponse( comparisonFactor ) );
        }

        return list1;
    }

    protected List<OptionResponseDto> decisionOptionListToOptionResponseDtoList(List<DecisionOption> list) {
        if ( list == null ) {
            return null;
        }

        List<OptionResponseDto> list1 = new ArrayList<OptionResponseDto>( list.size() );
        for ( DecisionOption decisionOption : list ) {
            list1.add( toResponseDto( decisionOption ) );
        }

        return list1;
    }
}

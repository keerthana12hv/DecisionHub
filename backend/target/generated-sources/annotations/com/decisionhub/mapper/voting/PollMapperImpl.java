package com.decisionhub.mapper.voting;

import com.decisionhub.dto.response.voting.PollResponse;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.voting.Poll;
import com.decisionhub.enums.voting.PollStatus;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-01T15:27:34+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.12 (Oracle Corporation)"
)
@Component
public class PollMapperImpl implements PollMapper {

    @Override
    public PollResponse toResponse(Poll poll) {
        if ( poll == null ) {
            return null;
        }

        Long decisionId = null;
        Long id = null;
        PollStatus status = null;
        LocalDateTime endTime = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        decisionId = pollDecisionId( poll );
        id = poll.getId();
        status = poll.getStatus();
        endTime = poll.getEndTime();
        createdAt = poll.getCreatedAt();
        updatedAt = poll.getUpdatedAt();

        PollResponse pollResponse = new PollResponse( id, decisionId, status, endTime, createdAt, updatedAt );

        return pollResponse;
    }

    private Long pollDecisionId(Poll poll) {
        if ( poll == null ) {
            return null;
        }
        Decision decision = poll.getDecision();
        if ( decision == null ) {
            return null;
        }
        Long id = decision.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}

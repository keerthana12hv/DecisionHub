package com.decisionhub.validator.voting;

import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.voting.Poll;
import com.decisionhub.enums.voting.PollStatus;
import com.decisionhub.exception.BadRequestException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PollValidatorTest {

    private PollValidator pollValidator;

    @BeforeEach
    void setUp() {
        pollValidator = new PollValidator();
    }

    // =========================================================
    // validateEndTimeExtension()
    // =========================================================

    @Test
    void validateEndTimeExtension_shouldPass_whenExtensionIsValid() {

        LocalDateTime now = LocalDateTime.now();

        Decision decision = new Decision();
        decision.setDeadline(now.plusDays(2));

        Poll poll = new Poll();
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(now.plusHours(2));
        poll.setDecision(decision);

        LocalDateTime newEndTime = now.plusHours(4);

        assertDoesNotThrow(
                () -> pollValidator.validateEndTimeExtension(
                        poll,
                        newEndTime
                )
        );
    }

    @Test
    void validateEndTimeExtension_shouldThrow_whenPollIsClosed() {

        LocalDateTime now = LocalDateTime.now();

        Decision decision = new Decision();
        decision.setDeadline(now.plusDays(2));

        Poll poll = new Poll();
        poll.setStatus(PollStatus.CLOSED);
        poll.setEndTime(now.plusHours(2));
        poll.setDecision(decision);

        LocalDateTime newEndTime = now.plusHours(4);

        assertThrows(
                BadRequestException.class,
                () -> pollValidator.validateEndTimeExtension(
                        poll,
                        newEndTime
                )
        );
    }

    @Test
    void validateEndTimeExtension_shouldThrow_whenCurrentEndTimeIsNull() {

        Decision decision = new Decision();
        decision.setDeadline(LocalDateTime.now().plusDays(2));

        Poll poll = new Poll();
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(null);
        poll.setDecision(decision);

        assertThrows(
                BadRequestException.class,
                () -> pollValidator.validateEndTimeExtension(
                        poll,
                        LocalDateTime.now().plusHours(4)
                )
        );
    }

    @Test
    void validateEndTimeExtension_shouldThrow_whenPollAlreadyExpired() {

        LocalDateTime now = LocalDateTime.now();

        Decision decision = new Decision();
        decision.setDeadline(now.plusDays(2));

        Poll poll = new Poll();
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(now.minusHours(1));
        poll.setDecision(decision);

        assertThrows(
                BadRequestException.class,
                () -> pollValidator.validateEndTimeExtension(
                        poll,
                        now.plusHours(4)
                )
        );
    }

    @Test
    void validateEndTimeExtension_shouldThrow_whenNewEndTimeIsNull() {

        LocalDateTime now = LocalDateTime.now();

        Decision decision = new Decision();
        decision.setDeadline(now.plusDays(2));

        Poll poll = new Poll();
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(now.plusHours(2));
        poll.setDecision(decision);

        assertThrows(
                BadRequestException.class,
                () -> pollValidator.validateEndTimeExtension(
                        poll,
                        null
                )
        );
    }

    @Test
    void validateEndTimeExtension_shouldThrow_whenNewEndTimeIsInPast() {

        LocalDateTime now = LocalDateTime.now();

        Decision decision = new Decision();
        decision.setDeadline(now.plusDays(2));

        Poll poll = new Poll();
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(now.plusHours(2));
        poll.setDecision(decision);

        LocalDateTime newEndTime = now.minusHours(1);

        assertThrows(
                BadRequestException.class,
                () -> pollValidator.validateEndTimeExtension(
                        poll,
                        newEndTime
                )
        );
    }

    @Test
    void validateEndTimeExtension_shouldThrow_whenNewEndTimeIsBeforeCurrentEndTime() {

        LocalDateTime now = LocalDateTime.now();

        Decision decision = new Decision();
        decision.setDeadline(now.plusDays(2));

        Poll poll = new Poll();
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(now.plusHours(4));
        poll.setDecision(decision);

        LocalDateTime newEndTime = now.plusHours(2);

        assertThrows(
                BadRequestException.class,
                () -> pollValidator.validateEndTimeExtension(
                        poll,
                        newEndTime
                )
        );
    }

    @Test
    void validateEndTimeExtension_shouldThrow_whenNewEndTimeEqualsCurrentEndTime() {

        LocalDateTime now = LocalDateTime.now();

        Decision decision = new Decision();
        decision.setDeadline(now.plusDays(2));

        LocalDateTime currentEndTime = now.plusHours(2);

        Poll poll = new Poll();
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(currentEndTime);
        poll.setDecision(decision);

        assertThrows(
                BadRequestException.class,
                () -> pollValidator.validateEndTimeExtension(
                        poll,
                        currentEndTime
                )
        );
    }

    @Test
    void validateEndTimeExtension_shouldThrow_whenNewEndTimeExceedsDecisionDeadline() {

        LocalDateTime now = LocalDateTime.now();

        Decision decision = new Decision();
        decision.setDeadline(now.plusHours(5));

        Poll poll = new Poll();
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(now.plusHours(2));
        poll.setDecision(decision);

        LocalDateTime newEndTime = now.plusHours(6);

        assertThrows(
                BadRequestException.class,
                () -> pollValidator.validateEndTimeExtension(
                        poll,
                        newEndTime
                )
        );
    }

    @Test
    void validateEndTimeExtension_shouldPass_whenDecisionDeadlineIsNull() {

        LocalDateTime now = LocalDateTime.now();

        Decision decision = new Decision();
        decision.setDeadline(null);

        Poll poll = new Poll();
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(now.plusHours(2));
        poll.setDecision(decision);

        LocalDateTime newEndTime = now.plusHours(4);

        assertDoesNotThrow(
                () -> pollValidator.validateEndTimeExtension(
                        poll,
                        newEndTime
                )
        );
    }

    // =========================================================
    // validateClose()
    // =========================================================

    @Test
    void validateClose_shouldPass_whenPollIsOpen() {

        Poll poll = new Poll();
        poll.setStatus(PollStatus.OPEN);

        assertDoesNotThrow(
                () -> pollValidator.validateClose(poll)
        );
    }

    @Test
    void validateClose_shouldThrow_whenPollIsClosed() {

        Poll poll = new Poll();
        poll.setStatus(PollStatus.CLOSED);

        assertThrows(
                BadRequestException.class,
                () -> pollValidator.validateClose(poll)
        );
    }

    // =========================================================
    // validateOpenForParticipation()
    // =========================================================

    @Test
    void validateOpenForParticipation_shouldPass_whenPollIsOpenAndNotExpired() {

        Poll poll = new Poll();
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(LocalDateTime.now().plusHours(2));

        assertDoesNotThrow(
                () -> pollValidator.validateOpenForParticipation(poll)
        );
    }

    @Test
    void validateOpenForParticipation_shouldThrow_whenPollIsClosed() {

        Poll poll = new Poll();
        poll.setStatus(PollStatus.CLOSED);
        poll.setEndTime(LocalDateTime.now().plusHours(2));

        assertThrows(
                BadRequestException.class,
                () -> pollValidator.validateOpenForParticipation(poll)
        );
    }

    @Test
    void validateOpenForParticipation_shouldThrow_whenEndTimeIsNull() {

        Poll poll = new Poll();
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(null);

        assertThrows(
                BadRequestException.class,
                () -> pollValidator.validateOpenForParticipation(poll)
        );
    }

    @Test
    void validateOpenForParticipation_shouldThrow_whenPollHasExpired() {

        Poll poll = new Poll();
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(LocalDateTime.now().minusMinutes(1));

        assertThrows(
                BadRequestException.class,
                () -> pollValidator.validateOpenForParticipation(poll)
        );
    }
}
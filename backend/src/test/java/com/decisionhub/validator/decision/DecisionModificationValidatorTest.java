package com.decisionhub.validator.decision;

import com.decisionhub.entity.decision.Decision;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.exception.DecisionClosedException;
import com.decisionhub.exception.DecisionLockedException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.decision.DecisionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecisionModificationValidatorTest {

    @Mock
    private DecisionRepository decisionRepository;

    @InjectMocks
    private DecisionModificationValidator validator;

    private Decision decision;

    @BeforeEach
    void setUp() {

        decision = new Decision();
        decision.setId(1L);
        decision.setStatus(DecisionStatus.ACTIVE);
        decision.setLocked(false);
    }

    // =========================================================
    // validateDecisionEditable(Decision)
    // =========================================================

    @Test
    void validateDecisionEditable_shouldPass_whenDecisionIsEditable() {

        assertDoesNotThrow(
                () -> validator.validateDecisionEditable(decision)
        );
    }

    @Test
    void validateDecisionEditable_shouldPass_whenDecisionIsNull() {

        assertDoesNotThrow(
                () -> validator.validateDecisionEditable((Decision) null)
        );
    }

    @Test
    void validateDecisionEditable_shouldThrow_whenDecisionIsClosed() {

        decision.setStatus(DecisionStatus.CLOSED);

        assertThrows(
                DecisionClosedException.class,
                () -> validator.validateDecisionEditable(decision)
        );
    }

    @Test
    void validateDecisionEditable_shouldThrow_whenDecisionIsLocked() {

        decision.setLocked(true);

        assertThrows(
                DecisionLockedException.class,
                () -> validator.validateDecisionEditable(decision)
        );
    }

    @Test
    void validateDecisionEditable_shouldThrowClosedBeforeLocked_whenBothConditionsExist() {

        decision.setStatus(DecisionStatus.CLOSED);
        decision.setLocked(true);

        assertThrows(
                DecisionClosedException.class,
                () -> validator.validateDecisionEditable(decision)
        );
    }

    // =========================================================
    // validateDecisionEditable(Long)
    // =========================================================

    @Test
    void validateDecisionEditableById_shouldPass_whenDecisionExistsAndEditable() {

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.of(decision));

        assertDoesNotThrow(
                () -> validator.validateDecisionEditable(1L)
        );

        verify(decisionRepository)
                .findById(1L);
    }

    @Test
    void validateDecisionEditableById_shouldThrow_whenDecisionNotFound() {

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> validator.validateDecisionEditable(1L)
        );
    }

    // =========================================================
    // validateDecisionLifecycleForWrite()
    // =========================================================

    @Test
    void validateDecisionLifecycleForWrite_shouldPass_whenDecisionIsEditable() {

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.of(decision));

        assertDoesNotThrow(
                () -> validator.validateDecisionLifecycleForWrite(1L)
        );
    }

    @Test
    void validateDecisionLifecycleForWrite_shouldThrow_whenDecisionIsClosed() {

        decision.setStatus(DecisionStatus.CLOSED);

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.of(decision));

        assertThrows(
                DecisionClosedException.class,
                () -> validator.validateDecisionLifecycleForWrite(1L)
        );
    }

    @Test
    void validateDecisionLifecycleForWrite_shouldThrow_whenDecisionIsLocked() {

        decision.setLocked(true);

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.of(decision));

        assertThrows(
                DecisionLockedException.class,
                () -> validator.validateDecisionLifecycleForWrite(1L)
        );
    }

    // =========================================================
    // validateCommentAllowed()
    // =========================================================

    @Test
    void validateCommentAllowed_shouldPass_whenDecisionIsActiveAndUnlocked() {

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.of(decision));

        assertDoesNotThrow(
                () -> validator.validateCommentAllowed(1L)
        );
    }

    @Test
    void validateCommentAllowed_shouldThrow_whenDecisionIsClosed() {

        decision.setStatus(DecisionStatus.CLOSED);

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.of(decision));

        assertThrows(
                DecisionClosedException.class,
                () -> validator.validateCommentAllowed(1L)
        );
    }

    @Test
    void validateCommentAllowed_shouldThrow_whenDecisionIsDraft() {

        decision.setStatus(DecisionStatus.DRAFT);

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.of(decision));

        assertThrows(
                DecisionClosedException.class,
                () -> validator.validateCommentAllowed(1L)
        );
    }

    @Test
    void validateCommentAllowed_shouldThrow_whenDecisionIsLocked() {

        decision.setLocked(true);

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.of(decision));

        assertThrows(
                DecisionLockedException.class,
                () -> validator.validateCommentAllowed(1L)
        );
    }

    @Test
    void validateCommentAllowed_shouldThrow_whenDecisionNotFound() {

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> validator.validateCommentAllowed(1L)
        );
    }
}
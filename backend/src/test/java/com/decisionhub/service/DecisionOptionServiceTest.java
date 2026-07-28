package com.decisionhub.service;

import com.decisionhub.dto.request.decision.OptionCreateDto;
import com.decisionhub.dto.response.decision.OptionResponseDto;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.decision.DecisionMapper;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.DecisionOptionRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.security.decision.DecisionAuthorizationService;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.service.impl.decision.DecisionOptionServiceImpl;
import com.decisionhub.service.interfaces.audit.AuditService;
import com.decisionhub.validator.decision.DecisionOptionValidator;
import com.decisionhub.validator.decision.DecisionModificationValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionOptionServiceTest {

    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private DecisionOptionRepository decisionOptionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DecisionMapper decisionMapper;
    @Mock
    private DecisionOptionValidator decisionOptionValidator;
    @Mock
    private DecisionAuthorizationService decisionAuthorizationService;
    @Mock
    private AuditService auditService;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private DecisionModificationValidator decisionModificationValidator;

    @InjectMocks
    private DecisionOptionServiceImpl decisionOptionService;

    private Long boardId;
    private Long optionId;
    private Long userId;
    private Decision board;
    private DecisionOption option;
    private User user;
    private OptionCreateDto createDto;
    private OptionResponseDto responseDto;

    @BeforeEach
    void setUp() {
        boardId = 1L;
        optionId = 2L;
        userId = 3L;

        user = new User();
        user.setId(userId);
        user.setUsername("owner");

        board = new Decision();
        board.setId(boardId);
        board.setStatus(DecisionStatus.DRAFT);
        board.setCreator(user);

        option = new DecisionOption();
        option.setId(optionId);
        option.setDecision(board);
        option.setOptionName("Test Option");

        createDto = new OptionCreateDto("Test Option", "Description", Collections.emptyList());
        responseDto = new OptionResponseDto(optionId, "Test Option", "Description", Collections.emptyList());
    }

    @Test
    void testCreateOption_Success() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canManageOptions(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(decisionMapper.toEntity(createDto)).thenReturn(option);
        when(decisionOptionRepository.saveAndFlush(option)).thenReturn(option);
        when(decisionMapper.toResponseDto(option)).thenReturn(responseDto);

        OptionResponseDto result = decisionOptionService.createOption(boardId, createDto, "127.0.0.1", "Mozilla");

        assertNotNull(result);
        assertEquals(optionId, result.id());
        verify(decisionOptionValidator).validateCreate(board, createDto);
        verify(decisionOptionRepository).saveAndFlush(option);
        verify(auditService).log(eq(user), eq("OPTION_CREATED"), eq("decision_options"), any(Long.class), eq(null), anyString(), eq("127.0.0.1"), eq("Mozilla"));
    }

    @Test
    void testCreateOption_BoardNotFound() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            decisionOptionService.createOption(boardId, createDto, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testCreateOption_Unauthorized() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.empty());

        assertThrows(UnauthorizedActionException.class, () -> 
            decisionOptionService.createOption(boardId, createDto, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testCreateOption_Forbidden() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canManageOptions(boardId, userId)).thenReturn(false);

        assertThrows(UnauthorizedActionException.class, () -> 
            decisionOptionService.createOption(boardId, createDto, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testUpdateOption_Success() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canManageOptions(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(decisionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
        when(decisionOptionRepository.save(option)).thenReturn(option);
        when(decisionMapper.toResponseDto(option)).thenReturn(responseDto);

        OptionResponseDto result = decisionOptionService.updateOption(boardId, optionId, createDto, "127.0.0.1", "Mozilla");

        assertNotNull(result);
        verify(decisionOptionValidator).validateUpdate(board, option, createDto);
        verify(auditService).log(eq(user), eq("OPTION_UPDATED"), eq("decision_options"), eq(optionId), anyString(), anyString(), eq("127.0.0.1"), eq("Mozilla"));
    }

    @Test
    void testUpdateOption_BoardMismatch() {
        Decision anotherBoard = new Decision();
        anotherBoard.setId(99L);
        option.setDecision(anotherBoard);

        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canManageOptions(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(decisionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));

        assertThrows(BadRequestException.class, () -> 
            decisionOptionService.updateOption(boardId, optionId, createDto, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testDeleteOption_Success() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canManageOptions(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(decisionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));

        decisionOptionService.deleteOption(boardId, optionId, "127.0.0.1", "Mozilla");

        verify(decisionOptionValidator).validateDelete(board);
        verify(decisionOptionRepository).delete(option);
        verify(auditService).log(eq(user), eq("OPTION_DELETED"), eq("decision_options"), eq(optionId), anyString(), eq(null), eq("127.0.0.1"), eq("Mozilla"));
    }

    @Test
    void testDeleteOption_ValidatorFails() {
        when(decisionRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canManageOptions(boardId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(decisionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
        
        doThrow(new BadRequestException("Cannot delete option")).when(decisionOptionValidator).validateDelete(board);

        assertThrows(BadRequestException.class, () -> 
            decisionOptionService.deleteOption(boardId, optionId, "127.0.0.1", "Mozilla")
        );
    }
}

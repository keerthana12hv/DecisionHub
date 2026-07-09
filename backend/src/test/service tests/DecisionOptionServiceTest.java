package com.decisionhub.service;

import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.OptionResponseDto;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.DecisionOption;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.entity.User;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ForbiddenException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedException;
import com.decisionhub.mapper.DecisionMapper;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.DecisionOptionRepository;
import com.decisionhub.repository.UserRepository;
import com.decisionhub.security.AuthenticationFacade;
import com.decisionhub.security.DecisionAuthorizationService;
import com.decisionhub.service.impl.DecisionOptionServiceImpl;
import com.decisionhub.validator.DecisionOptionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

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
    private DecisionBoardRepository decisionBoardRepository;
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

    @InjectMocks
    private DecisionOptionServiceImpl decisionOptionService;

    private UUID boardId;
    private UUID optionId;
    private UUID userId;
    private DecisionBoard board;
    private DecisionOption option;
    private User user;
    private OptionCreateDto createDto;
    private OptionResponseDto responseDto;

    @BeforeEach
    void setUp() {
        boardId = UUID.randomUUID();
        optionId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setUsername("owner");

        board = new DecisionBoard();
        board.setId(boardId);
        board.setStatus(DecisionStatus.DRAFT);
        board.setCreator(user);

        option = new DecisionOption();
        option.setId(optionId);
        option.setDecision(board);
        option.setTitle("Test Option");

        createDto = new OptionCreateDto("Test Option", "Description", Collections.emptyList());
        responseDto = new OptionResponseDto(optionId, "Test Option", "Description", Collections.emptyList(), Collections.emptyList());
    }

    @Test
    void testCreateOption_Success() {
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
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
        verify(auditService).log(eq(user), eq("OPTION_CREATED"), eq("decision_options"), any(UUID.class), eq(null), anyString(), eq("127.0.0.1"), eq("Mozilla"));
    }

    @Test
    void testCreateOption_BoardNotFound() {
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            decisionOptionService.createOption(boardId, createDto, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testCreateOption_Unauthorized() {
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> 
            decisionOptionService.createOption(boardId, createDto, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testCreateOption_Forbidden() {
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(decisionAuthorizationService.canManageOptions(boardId, userId)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> 
            decisionOptionService.createOption(boardId, createDto, "127.0.0.1", "Mozilla")
        );
    }

    @Test
    void testUpdateOption_Success() {
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
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
        DecisionBoard anotherBoard = new DecisionBoard();
        anotherBoard.setId(UUID.randomUUID());
        option.setDecision(anotherBoard);

        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
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
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
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
        when(decisionBoardRepository.findById(boardId)).thenReturn(Optional.of(board));
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

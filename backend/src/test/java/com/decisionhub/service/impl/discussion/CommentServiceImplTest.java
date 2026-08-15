package com.decisionhub.service.impl.discussion;

import com.decisionhub.dto.request.discussion.CreateCommentRequest;
import com.decisionhub.dto.request.discussion.UpdateCommentRequest;
import com.decisionhub.dto.response.discussion.CommentResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.discussion.Comment;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.discussion.CommentMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.discussion.CommentRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.security.decision.DecisionAuthorizationService;
import com.decisionhub.validator.decision.DecisionModificationValidator;
import com.decisionhub.validator.discussion.CommentValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private DecisionAuthorizationService decisionAuthorizationService;

    @Mock
    private CommentValidator commentValidator;

    @Mock
    private DecisionModificationValidator decisionModificationValidator;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User user;
    private Decision decision;
    private Comment comment;
    private CreateCommentRequest createRequest;
    private UpdateCommentRequest updateRequest;
    private CommentResponse response;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setUsername("chirag");

        decision = new Decision();
        decision.setId(10L);
        decision.setStatus(DecisionStatus.ACTIVE);

        comment = new Comment();
        comment.setId(100L);
        comment.setDecision(decision);
        comment.setUser(user);
        comment.setContent("Hello");
        comment.setDepth(0);

        createRequest = new CreateCommentRequest(
                "Hello World"
        );

        updateRequest = new UpdateCommentRequest(
                "Updated Comment"
        );

        response = mock(CommentResponse.class);
    }

    // =========================================================
    // createComment()
    // =========================================================

    @Test
    void createComment_shouldCreateCommentSuccessfully() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(decisionRepository.findById(10L))
                .thenReturn(Optional.of(decision));

        when(decisionAuthorizationService.canComment(
                10L,
                1L
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {

                    Comment saved = invocation.getArgument(0);
                    saved.setId(100L);

                    return saved;
                });

        when(commentMapper.toResponse(any(Comment.class)))
                .thenReturn(response);

        CommentResponse result =
                commentService.createComment(
                        10L,
                        createRequest
                );

        assertNotNull(result);

        verify(decisionModificationValidator)
                .validateCommentAllowed(10L);

        verify(commentRepository)
                .save(any(Comment.class));

        verify(commentMapper)
                .toResponse(any(Comment.class));
    }

    @Test
    void createComment_shouldTrimContentBeforeSaving() {

        CreateCommentRequest request =
                new CreateCommentRequest(
                        "   Hello World   "
                );

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(decisionRepository.findById(10L))
                .thenReturn(Optional.of(decision));

        when(decisionAuthorizationService.canComment(
                10L,
                1L
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(commentMapper.toResponse(any(Comment.class)))
                .thenReturn(response);

        commentService.createComment(
                10L,
                request
        );

        verify(commentRepository)
                .save(argThat(saved ->
                        saved.getContent().equals("Hello World")
                ));
    }

    @Test
    void createComment_shouldThrow_whenUserIsUnauthenticated() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.empty());

        assertThrows(
                UnauthorizedActionException.class,
                () -> commentService.createComment(
                        10L,
                        createRequest
                )
        );

        verify(commentRepository, never())
                .save(any());
    }

    @Test
    void createComment_shouldThrow_whenUserDoesNotExist() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.createComment(
                        10L,
                        createRequest
                )
        );

        verify(commentRepository, never())
                .save(any());
    }

    @Test
    void createComment_shouldThrow_whenDecisionDoesNotExist() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(decisionRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.createComment(
                        10L,
                        createRequest
                )
        );

        verify(commentRepository, never())
                .save(any());
    }

    @Test
    void createComment_shouldThrow_whenUserCannotComment() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(decisionRepository.findById(10L))
                .thenReturn(Optional.of(decision));

        when(decisionAuthorizationService.canComment(
                10L,
                1L
        )).thenReturn(false);

        assertThrows(
                UnauthorizedActionException.class,
                () -> commentService.createComment(
                        10L,
                        createRequest
                )
        );

        verify(commentRepository, never())
                .save(any());
    }

    @Test
    void createComment_shouldSetTopLevelCommentProperties() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(decisionRepository.findById(10L))
                .thenReturn(Optional.of(decision));

        when(decisionAuthorizationService.canComment(
                10L,
                1L
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(commentMapper.toResponse(any(Comment.class)))
                .thenReturn(response);

        commentService.createComment(
                10L,
                createRequest
        );

        verify(commentRepository)
                .save(argThat(saved ->
                        saved.getParentComment() == null &&
                        saved.getDepth() == 0 &&
                        saved.getDecision().equals(decision) &&
                        saved.getUser().equals(user)
                ));
    }

        // =========================================================
    // replyToComment()
    // =========================================================

    @Test
    void replyToComment_shouldCreateReplySuccessfully() {

        Comment parentComment = new Comment();
        parentComment.setId(50L);
        parentComment.setDecision(decision);
        parentComment.setUser(user);
        parentComment.setDepth(2);

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(decisionRepository.findById(10L))
                .thenReturn(Optional.of(decision));

        when(commentRepository.findById(50L))
                .thenReturn(Optional.of(parentComment));

        when(decisionAuthorizationService.canComment(
                10L,
                1L
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment saved = invocation.getArgument(0);
                    saved.setId(200L);
                    return saved;
                });

        when(commentMapper.toResponse(any(Comment.class)))
                .thenReturn(response);

        CommentResponse result =
                commentService.replyToComment(
                        10L,
                        50L,
                        createRequest
                );

        assertNotNull(result);

        verify(decisionModificationValidator)
                .validateCommentAllowed(10L);

        verify(commentValidator)
                .validateReplyAllowed(parentComment);

        verify(commentValidator)
                .validateReplyDepth(parentComment);

        verify(commentRepository)
                .save(any(Comment.class));

        verify(commentMapper)
                .toResponse(any(Comment.class));
    }

    @Test
    void replyToComment_shouldTrimReplyContent() {

        CreateCommentRequest request =
                new CreateCommentRequest(
                        "   Reply Content   "
                );

        Comment parentComment = new Comment();
        parentComment.setId(50L);
        parentComment.setDecision(decision);
        parentComment.setDepth(1);

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(decisionRepository.findById(10L))
                .thenReturn(Optional.of(decision));

        when(commentRepository.findById(50L))
                .thenReturn(Optional.of(parentComment));

        when(decisionAuthorizationService.canComment(
                10L,
                1L
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(commentMapper.toResponse(any(Comment.class)))
                .thenReturn(response);

        commentService.replyToComment(
                10L,
                50L,
                request
        );

        verify(commentRepository)
                .save(argThat(reply ->
                        reply.getContent().equals("Reply Content")
                ));
    }

    @Test
    void replyToComment_shouldSetCorrectDepth() {

        Comment parentComment = new Comment();
        parentComment.setId(50L);
        parentComment.setDecision(decision);
        parentComment.setDepth(3);

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(decisionRepository.findById(10L))
                .thenReturn(Optional.of(decision));

        when(commentRepository.findById(50L))
                .thenReturn(Optional.of(parentComment));

        when(decisionAuthorizationService.canComment(
                10L,
                1L
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(commentMapper.toResponse(any(Comment.class)))
                .thenReturn(response);

        commentService.replyToComment(
                10L,
                50L,
                createRequest
        );

        verify(commentRepository)
                .save(argThat(reply ->
                        reply.getDepth() == 4 &&
                        reply.getParentComment().equals(parentComment)
                ));
    }

    @Test
    void replyToComment_shouldThrow_whenParentCommentDoesNotExist() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(decisionRepository.findById(10L))
                .thenReturn(Optional.of(decision));

        when(commentRepository.findById(50L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.replyToComment(
                        10L,
                        50L,
                        createRequest
                )
        );

        verify(commentRepository, never())
                .save(any());
    }

    @Test
    void replyToComment_shouldThrow_whenParentBelongsToAnotherDecision() {

        Decision anotherDecision = new Decision();
        anotherDecision.setId(999L);

        Comment parentComment = new Comment();
        parentComment.setDecision(anotherDecision);

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(decisionRepository.findById(10L))
                .thenReturn(Optional.of(decision));

        when(commentRepository.findById(50L))
                .thenReturn(Optional.of(parentComment));

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.replyToComment(
                        10L,
                        50L,
                        createRequest
                )
        );
    }

    @Test
    void replyToComment_shouldThrow_whenUserCannotComment() {

        Comment parentComment = new Comment();
        parentComment.setDecision(decision);

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(decisionRepository.findById(10L))
                .thenReturn(Optional.of(decision));

        when(commentRepository.findById(50L))
                .thenReturn(Optional.of(parentComment));

        when(decisionAuthorizationService.canComment(
                10L,
                1L
        )).thenReturn(false);

        assertThrows(
                UnauthorizedActionException.class,
                () -> commentService.replyToComment(
                        10L,
                        50L,
                        createRequest
                )
        );

        verify(commentRepository, never())
                .save(any());
    }

    @Test
    void replyToComment_shouldInvokeDiscussionValidators() {

        Comment parentComment = new Comment();
        parentComment.setDecision(decision);
        parentComment.setDepth(1);

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(decisionRepository.findById(10L))
                .thenReturn(Optional.of(decision));

        when(commentRepository.findById(50L))
                .thenReturn(Optional.of(parentComment));

        when(decisionAuthorizationService.canComment(
                10L,
                1L
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(commentMapper.toResponse(any(Comment.class)))
                .thenReturn(response);

        commentService.replyToComment(
                10L,
                50L,
                createRequest
        );

        verify(commentValidator, times(1))
                .validateReplyAllowed(parentComment);

        verify(commentValidator, times(1))
                .validateReplyDepth(parentComment);
    }

        // =========================================================
    // updateComment()
    // =========================================================

    @Test
    void updateComment_shouldUpdateCommentSuccessfully() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(commentRepository.findById(100L))
                .thenReturn(Optional.of(comment));

        when(decisionAuthorizationService.canEditComment(
                100L,
                1L
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(commentMapper.toResponse(any(Comment.class)))
                .thenReturn(response);

        CommentResponse result =
                commentService.updateComment(
                        100L,
                        updateRequest
                );

        assertNotNull(result);

        verify(commentValidator)
                .validateEditable(comment);

        verify(commentRepository)
                .save(comment);

        verify(commentMapper)
                .toResponse(comment);

        assertEquals(
                "Updated Comment",
                comment.getContent()
        );
    }

    @Test
    void updateComment_shouldTrimUpdatedContent() {

        UpdateCommentRequest request =
                new UpdateCommentRequest(
                        "   Updated Comment   "
                );

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(commentRepository.findById(100L))
                .thenReturn(Optional.of(comment));

        when(decisionAuthorizationService.canEditComment(
                100L,
                1L
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(commentMapper.toResponse(any(Comment.class)))
                .thenReturn(response);

        commentService.updateComment(
                100L,
                request
        );

        assertEquals(
                "Updated Comment",
                comment.getContent()
        );
    }

    @Test
    void updateComment_shouldThrow_whenCommentNotFound() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(commentRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.updateComment(
                        100L,
                        updateRequest
                )
        );

        verify(commentRepository, never())
                .save(any());
    }

    @Test
    void updateComment_shouldThrow_whenUserCannotEditComment() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(commentRepository.findById(100L))
                .thenReturn(Optional.of(comment));

        when(decisionAuthorizationService.canEditComment(
                100L,
                1L
        )).thenReturn(false);

        assertThrows(
                UnauthorizedActionException.class,
                () -> commentService.updateComment(
                        100L,
                        updateRequest
                )
        );

        verify(commentRepository, never())
                .save(any());
    }

    @Test
    void updateComment_shouldInvokeValidator() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(commentRepository.findById(100L))
                .thenReturn(Optional.of(comment));

        when(decisionAuthorizationService.canEditComment(
                100L,
                1L
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(commentMapper.toResponse(any(Comment.class)))
                .thenReturn(response);

        commentService.updateComment(
                100L,
                updateRequest
        );

        verify(commentValidator, times(1))
                .validateEditable(comment);
    }

    // =========================================================
    // deleteComment()
    // =========================================================

    @Test
    void deleteComment_shouldSoftDeleteCommentSuccessfully() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(commentRepository.findById(100L))
                .thenReturn(Optional.of(comment));

        when(decisionAuthorizationService.canDeleteComment(
                100L,
                1L
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(
                () -> commentService.deleteComment(100L)
        );

        assertNotNull(comment.getDeletedAt());

        verify(commentRepository)
                .save(comment);
    }

    @Test
    void deleteComment_shouldThrow_whenCommentNotFound() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(commentRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.deleteComment(100L)
        );

        verify(commentRepository, never())
                .save(any());
    }

    @Test
    void deleteComment_shouldThrow_whenUserCannotDeleteComment() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(commentRepository.findById(100L))
                .thenReturn(Optional.of(comment));

        when(decisionAuthorizationService.canDeleteComment(
                100L,
                1L
        )).thenReturn(false);

        assertThrows(
                UnauthorizedActionException.class,
                () -> commentService.deleteComment(100L)
        );

        verify(commentRepository, never())
                .save(any());
    }

    @Test
    void deleteComment_shouldNotDeletePhysically() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(commentRepository.findById(100L))
                .thenReturn(Optional.of(comment));

        when(decisionAuthorizationService.canDeleteComment(
                100L,
                1L
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        commentService.deleteComment(100L);

        verify(commentRepository, never())
                .delete(any());

        verify(commentRepository, never())
                .deleteById(anyLong());

        verify(commentRepository)
                .save(comment);
    }

        // =========================================================
    // getCommentsByDecision()
    // =========================================================

    @Test
    void getCommentsByDecision_shouldReturnAllTopLevelComments() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(decisionAuthorizationService.canViewDecision(
                10L,
                1L
        )).thenReturn(true);

        List<Comment> comments = List.of(
                comment,
                new Comment()
        );

        when(commentRepository
                .findByDecisionIdAndParentCommentIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(10L))
                .thenReturn(comments);

        when(commentMapper.toResponse(any(Comment.class)))
                .thenReturn(response);

        List<CommentResponse> result =
                commentService.getCommentsByDecision(10L);

        assertEquals(2, result.size());

        verify(commentRepository)
                .findByDecisionIdAndParentCommentIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(10L);

        verify(commentMapper, times(2))
                .toResponse(any(Comment.class));
    }

    @Test
    void getCommentsByDecision_shouldThrow_whenUserCannotViewDecision() {

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(decisionAuthorizationService.canViewDecision(
                10L,
                1L
        )).thenReturn(false);

        assertThrows(
                UnauthorizedActionException.class,
                () -> commentService.getCommentsByDecision(10L)
        );

        verify(commentRepository, never())
                .findByDecisionIdAndParentCommentIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(anyLong());
    }

    // =========================================================
    // getReplies()
    // =========================================================

    @Test
    void getReplies_shouldReturnRepliesSuccessfully() {

        comment.setDecision(decision);

        when(commentRepository.findById(100L))
                .thenReturn(Optional.of(comment));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(decisionAuthorizationService.canViewDecision(
                decision.getId(),
                1L
        )).thenReturn(true);

        List<Comment> replies = List.of(
                new Comment(),
                new Comment()
        );

        when(commentRepository
                .findByParentCommentIdAndDeletedAtIsNullOrderByCreatedAtAsc(100L))
                .thenReturn(replies);

        when(commentMapper.toResponse(any(Comment.class)))
                .thenReturn(response);

        List<CommentResponse> result =
                commentService.getReplies(100L);

        assertEquals(2, result.size());

        verify(commentMapper, times(2))
                .toResponse(any(Comment.class));
    }

    @Test
    void getReplies_shouldThrow_whenParentCommentNotFound() {

        when(commentRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.getReplies(100L)
        );
    }

    // =========================================================
    // getComment()
    // =========================================================

    @Test
    void getComment_shouldReturnCommentSuccessfully() {

        comment.setDecision(decision);

        when(commentRepository.findById(100L))
                .thenReturn(Optional.of(comment));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(1L));

        when(decisionAuthorizationService.canViewDecision(
                decision.getId(),
                1L
        )).thenReturn(true);

        when(commentMapper.toResponse(comment))
                .thenReturn(response);

        CommentResponse result =
                commentService.getComment(100L);

        assertNotNull(result);

        verify(commentMapper)
                .toResponse(comment);
    }

    @Test
    void getComment_shouldThrow_whenCommentNotFound() {

        when(commentRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.getComment(100L)
        );
    }

}
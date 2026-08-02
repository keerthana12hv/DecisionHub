package com.decisionhub.controller;

import com.decisionhub.dto.response.discussion.CommentResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.discussion.Comment;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.discussion.CommentRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final AuthenticationFacade authenticationFacade;

    @PostMapping("/{decisionId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long decisionId,
            @RequestBody CommentRequest request) {

        User currentUser = authenticationFacade.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedActionException("User must be authenticated"));

        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUser(currentUser);
        comment.setDecision(decision);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setPinned(false);

        Comment saved = commentRepository.save(comment);

        CommentResponse response = new CommentResponse(
                saved.getId(),
                saved.getContent(),
                saved.getUser().getId(),
                saved.getUser().getUsername(),
                saved.getDecision().getId(),
                null,
                saved.getCreatedAt(),
                saved.isPinned()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{decisionId}/comments")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long decisionId) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found"));

        List<Comment> comments = commentRepository.findByDecisionIdAndDeletedAtIsNull(decisionId);

        List<CommentResponse> response = comments.stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getContent(),
                        comment.getUser().getId(),
                        comment.getUser().getUsername(),
                        comment.getDecision().getId(),
                        comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                        comment.getCreatedAt(),
                        comment.isPinned()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    public static class CommentRequest {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}

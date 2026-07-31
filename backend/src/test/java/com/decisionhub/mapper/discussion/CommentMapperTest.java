package com.decisionhub.mapper.discussion;

import com.decisionhub.dto.response.discussion.CommentResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.discussion.Comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    private CommentMapper commentMapper;

    @BeforeEach
    void setUp() {
        commentMapper = new CommentMapper();
    }

    // =========================================================
    // toResponse()
    // =========================================================

    @Test
    void toResponse_shouldReturnNull_whenCommentIsNull() {

        assertNull(commentMapper.toResponse(null));
    }

    @Test
    void toResponse_shouldMapTopLevelComment() {

        Decision decision = new Decision();
        decision.setId(10L);

        User user = new User();
        user.setId(20L);
        user.setUsername("chirag");

        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt.plusMinutes(5);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setDecision(decision);
        comment.setUser(user);
        comment.setContent("Hello World");
        comment.setDepth(0);
        comment.setReplies(List.of());
        comment.setCreatedAt(createdAt);
        comment.setUpdatedAt(updatedAt);

        CommentResponse response = commentMapper.toResponse(comment);

        assertNotNull(response);

        assertEquals(1L, response.id());
        assertEquals(10L, response.decisionId());
        assertNull(response.parentCommentId());

        assertEquals(20L, response.userId());
        assertEquals("chirag", response.username());

        assertEquals("Hello World", response.content());
        assertFalse(response.deleted());

        assertEquals(0, response.depth());
        assertEquals(0, response.replyCount());

        assertEquals(createdAt, response.createdAt());
        assertEquals(updatedAt, response.updatedAt());
    }

    @Test
    void toResponse_shouldMapReplyComment() {

        Decision decision = new Decision();
        decision.setId(100L);

        User user = new User();
        user.setId(200L);
        user.setUsername("alice");

        Comment parent = new Comment();
        parent.setId(50L);

        Comment comment = new Comment();
        comment.setId(60L);
        comment.setDecision(decision);
        comment.setParentComment(parent);
        comment.setUser(user);
        comment.setContent("Reply");
        comment.setDepth(2);
        comment.setReplies(List.of());

        CommentResponse response = commentMapper.toResponse(comment);

        assertEquals(50L, response.parentCommentId());
        assertEquals(2, response.depth());
    }

    @Test
    void toResponse_shouldReturnDeletedPlaceholder_whenCommentIsDeleted() {

        Decision decision = new Decision();
        decision.setId(1L);

        User user = new User();
        user.setId(2L);
        user.setUsername("bob");

        Comment comment = new Comment();
        comment.setId(3L);
        comment.setDecision(decision);
        comment.setUser(user);
        comment.setContent("Original content");
        comment.setDeletedAt(LocalDateTime.now());
        comment.setReplies(List.of());

        CommentResponse response = commentMapper.toResponse(comment);

        assertEquals("[deleted]", response.content());
        assertTrue(response.deleted());
    }

    @Test
    void toResponse_shouldMapReplyCount_whenRepliesExist() {

        Decision decision = new Decision();
        decision.setId(1L);

        User user = new User();
        user.setId(2L);
        user.setUsername("john");

        Comment reply1 = new Comment();
        Comment reply2 = new Comment();
        Comment reply3 = new Comment();

        Comment comment = new Comment();
        comment.setId(5L);
        comment.setDecision(decision);
        comment.setUser(user);
        comment.setReplies(List.of(reply1, reply2, reply3));

        CommentResponse response = commentMapper.toResponse(comment);

        assertEquals(3, response.replyCount());
    }

    @Test
    void toResponse_shouldReturnZeroReplyCount_whenRepliesIsNull() {

        Decision decision = new Decision();
        decision.setId(1L);

        User user = new User();
        user.setId(2L);
        user.setUsername("john");

        Comment comment = new Comment();
        comment.setDecision(decision);
        comment.setUser(user);
        comment.setReplies(null);

        CommentResponse response = commentMapper.toResponse(comment);

        assertEquals(0, response.replyCount());
    }

    @Test
    void toResponse_shouldPreserveCreatedAndUpdatedTimestamps() {

        Decision decision = new Decision();
        decision.setId(1L);

        User user = new User();
        user.setId(2L);
        user.setUsername("john");

        LocalDateTime created = LocalDateTime.now();
        LocalDateTime updated = created.plusHours(1);

        Comment comment = new Comment();
        comment.setDecision(decision);
        comment.setUser(user);
        comment.setReplies(List.of());
        comment.setCreatedAt(created);
        comment.setUpdatedAt(updated);

        CommentResponse response = commentMapper.toResponse(comment);

        assertEquals(created, response.createdAt());
        assertEquals(updated, response.updatedAt());
    }
}
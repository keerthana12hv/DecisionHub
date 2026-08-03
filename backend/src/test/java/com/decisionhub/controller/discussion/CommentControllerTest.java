package com.decisionhub.controller.discussion;

import com.decisionhub.config.JwtService;
import com.decisionhub.controller.discussion.CommentController;
import com.decisionhub.dto.request.discussion.CreateCommentRequest;
import com.decisionhub.dto.request.discussion.UpdateCommentRequest;
import com.decisionhub.dto.response.discussion.CommentResponse;
import com.decisionhub.service.impl.authentication.CustomUserDetailsService;
import com.decisionhub.service.interfaces.discussion.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CommentController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.decisionhub\\.security\\..*"
        )
)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private CreateCommentRequest createRequest;
    private UpdateCommentRequest updateRequest;
    private CommentResponse response;

    @BeforeEach
    void setUp() {

        createRequest = new CreateCommentRequest(
                "Test Comment"
        );

        updateRequest = new UpdateCommentRequest(
                "Updated Comment"
        );

        response = new CommentResponse(
                1L,
                10L,
                null,
                100L,
                "chirag",
                "Test Comment",
                false,
                0,
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // =========================================================
    // createComment()
    // =========================================================

    @Test
    void createComment_withValidPayload_returnsCreated() throws Exception {

        when(commentService.createComment(
                eq(10L),
                any(CreateCommentRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/decisions/10/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Test Comment"));
    }

    @Test
    void createComment_withBlankContent_returnsBadRequest() throws Exception {

        CreateCommentRequest invalid =
                new CreateCommentRequest("");

        mockMvc.perform(
                        post("/api/decisions/10/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalid))
                )
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // replyToComment()
    // =========================================================

    @Test
    void replyToComment_withValidPayload_returnsCreated() throws Exception {

        when(commentService.replyToComment(
                eq(10L),
                eq(5L),
                any(CreateCommentRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/decisions/10/comments/5/replies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Test Comment"));
    }

    @Test
    void replyToComment_withBlankContent_returnsBadRequest() throws Exception {

        CreateCommentRequest invalid =
                new CreateCommentRequest("");

        mockMvc.perform(
                        post("/api/decisions/10/comments/5/replies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalid))
                )
                .andExpect(status().isBadRequest());
    }

        // =========================================================
    // updateComment()
    // =========================================================

    @Test
    void updateComment_withValidPayload_returnsOk() throws Exception {

        when(commentService.updateComment(
                eq(1L),
                any(UpdateCommentRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/comments/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Test Comment"));
    }

    @Test
    void updateComment_withBlankContent_returnsBadRequest() throws Exception {

        UpdateCommentRequest invalid =
                new UpdateCommentRequest("");

        mockMvc.perform(
                        put("/api/comments/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalid))
                )
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // deleteComment()
    // =========================================================

    @Test
    void deleteComment_returnsNoContent() throws Exception {

        doNothing().when(commentService)
                .deleteComment(1L);

        mockMvc.perform(
                        delete("/api/comments/1")
                )
                .andExpect(status().isNoContent());
    }

    // =========================================================
    // getComments()
    // =========================================================

    @Test
    void getComments_returnsOk() throws Exception {

        when(commentService.getCommentsByDecision(10L))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/decisions/10/comments")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].content").value("Test Comment"));
    }

    @Test
    void getComments_returnsEmptyList() throws Exception {

        when(commentService.getCommentsByDecision(10L))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/decisions/10/comments")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================================================
    // getReplies()
    // =========================================================

    @Test
    void getReplies_returnsOk() throws Exception {

        when(commentService.getReplies(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/comments/1/replies")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].content").value("Test Comment"));
    }

    @Test
    void getReplies_returnsEmptyList() throws Exception {

        when(commentService.getReplies(1L))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/comments/1/replies")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================================================
    // getComment()
    // =========================================================

    @Test
    void getComment_returnsOk() throws Exception {

        when(commentService.getComment(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/comments/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Test Comment"))
                .andExpect(jsonPath("$.username").value("chirag"));
    }

    @Test
    void getComment_returnsCorrectDecisionId() throws Exception {

        when(commentService.getComment(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/comments/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decisionId").value(10L))
                .andExpect(jsonPath("$.deleted").value(false))
                .andExpect(jsonPath("$.depth").value(0))
                .andExpect(jsonPath("$.replyCount").value(0));
    }
}
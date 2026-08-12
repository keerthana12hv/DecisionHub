package com.decisionhub.controller.discussion;

import com.decisionhub.dto.request.discussion.ReportCommentRequest;
import com.decisionhub.dto.response.discussion.CommentReportResponse;
import com.decisionhub.service.interfaces.report.CommentReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comment Reporting", description = "Endpoints for reporting inappropriate comments")
public class ReportController {

    private final CommentReportService commentReportService;

    @PostMapping("/{commentId}/report")
    @Operation(
            summary = "Report a comment",
            description = "Submits a report for an inappropriate comment.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CommentReportResponse> reportComment(
            @PathVariable Long commentId,
            @Valid @RequestBody ReportCommentRequest request
    ) {
        CommentReportResponse response = commentReportService.reportComment(commentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

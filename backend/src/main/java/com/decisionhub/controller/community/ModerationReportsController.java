package com.decisionhub.controller.community;

import com.decisionhub.dto.response.discussion.CommentReportResponse;
import com.decisionhub.service.interfaces.report.CommentReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("moderationReportsController")
@RequestMapping("/api/moderation/reports")
@RequiredArgsConstructor
@Tag(name = "Moderation Reports", description = "Moderator endpoints for reported comments")
public class ModerationReportsController {

    private final CommentReportService commentReportService;

    @GetMapping
    @Operation(summary = "List reported comments", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<CommentReportResponse>> listReports() {
        return ResponseEntity.ok(commentReportService.listReportsForModerator());
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "Dismiss report", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteReport(@PathVariable Long reportId) {
        commentReportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }
}

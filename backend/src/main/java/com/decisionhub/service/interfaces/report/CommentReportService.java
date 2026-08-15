package com.decisionhub.service.interfaces.report;

import com.decisionhub.dto.request.discussion.ReportCommentRequest;
import com.decisionhub.dto.response.discussion.CommentReportResponse;

public interface CommentReportService {

    CommentReportResponse reportComment(
            Long commentId,
            ReportCommentRequest request
    );

    java.util.List<com.decisionhub.dto.response.discussion.CommentReportResponse> listReportsForModerator();

    void deleteReport(Long reportId);
}

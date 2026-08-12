package com.decisionhub.service.impl.report;

import com.decisionhub.dto.request.discussion.ReportCommentRequest;
import com.decisionhub.dto.response.discussion.CommentReportResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.discussion.Comment;
import com.decisionhub.entity.reports.CommentReport;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.discussion.CommentMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.discussion.CommentRepository;
import com.decisionhub.repository.reports.CommentReportRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.service.interfaces.report.CommentReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentReportServiceImpl implements CommentReportService {

    private final CommentReportRepository commentReportRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AuthenticationFacade authenticationFacade;
        private final com.decisionhub.repository.community.CommunityMemberRepository communityMemberRepository;

    @Override
    @Transactional
    public CommentReportResponse reportComment(
            Long commentId,
            ReportCommentRequest request
    ) {
        log.info("Reporting comment ID: {}", commentId);

        Long currentUserId = authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User is not authenticated"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

        User reporter = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        CommentReport report = new CommentReport();
        report.setComment(comment);
        report.setReporter(reporter);
        report.setReason(request.reason().trim());

        CommentReport savedReport = commentReportRepository.save(report);

        log.info("Comment report created with ID: {} for comment ID: {}", savedReport.getId(), commentId);

        return new CommentReportResponse(
                savedReport.getId(),
                comment.getId(),
                reporter.getId(),
                reporter.getUsername(),
                savedReport.getReason(),
                savedReport.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.decisionhub.dto.response.discussion.CommentReportResponse> listReportsForModerator() {
        Long currentUserId = authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User is not authenticated"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        boolean isAdmin = currentUser.getRole() == com.decisionhub.enums.authentication.PlatformRole.ADMIN;

        java.util.List<CommentReport> all = commentReportRepository.findAll();

        return all.stream()
                .filter(r -> {
                    if (isAdmin) return true;
                    // Only reports for comments that belong to communities the user moderates/owns
                    com.decisionhub.entity.community.Community comm = r.getComment().getDecision().getCommunity();
                    if (comm == null) return false;
                    if (comm.getOwner() != null && comm.getOwner().getId().equals(currentUserId)) return true;
                    return communityMemberRepository.findByCommunityIdAndUserId(comm.getId(), currentUserId)
                            .map(m -> m.getStatus() == com.decisionhub.enums.community.MembershipStatus.APPROVED && m.getRole() == com.decisionhub.enums.community.CommunityMemberRole.MODERATOR)
                            .orElse(false);
                })
                .map(r -> new com.decisionhub.dto.response.discussion.CommentReportResponse(
                        r.getId(),
                        r.getComment().getId(),
                        r.getReporter().getId(),
                        r.getReporter().getUsername(),
                        r.getReason(),
                        r.getCreatedAt()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void deleteReport(Long reportId) {
        CommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        // Authorization: reuse same logic as for comment moderation
        Long currentUserId = authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User is not authenticated"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole() == com.decisionhub.enums.authentication.PlatformRole.ADMIN) {
            commentReportRepository.delete(report);
            return;
        }

        com.decisionhub.entity.community.Community comm = report.getComment().getDecision().getCommunity();
        if (comm == null) throw new UnauthorizedActionException("Not authorized to manage this report");

        if (comm.getOwner() != null && comm.getOwner().getId().equals(currentUserId)) {
            commentReportRepository.delete(report);
            return;
        }

        boolean isModerator = communityMemberRepository.findByCommunityIdAndUserId(comm.getId(), currentUserId)
                .map(m -> m.getStatus() == com.decisionhub.enums.community.MembershipStatus.APPROVED && m.getRole() == com.decisionhub.enums.community.CommunityMemberRole.MODERATOR)
                .orElse(false);

        if (!isModerator) throw new UnauthorizedActionException("Not authorized to manage this report");

        commentReportRepository.delete(report);
    }
}

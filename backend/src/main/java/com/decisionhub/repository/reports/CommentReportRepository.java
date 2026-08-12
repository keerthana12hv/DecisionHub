package com.decisionhub.repository.reports;

import com.decisionhub.entity.reports.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {
}

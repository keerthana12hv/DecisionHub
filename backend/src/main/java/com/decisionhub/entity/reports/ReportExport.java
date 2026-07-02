package com.decisionhub.entity.reports;

import com.decisionhub.entity.authentication.User;
import jakarta.persistence.*;

@Entity
public class ReportExport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reportType;
    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
package com.decisionhub.entity.decision;

import jakarta.persistence.*;

@Entity
public class ComparisonFactorTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String templateName;
    private String factorName;
}
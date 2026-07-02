package com.decisionhub.entity.decision;

import jakarta.persistence.*;

@Entity
public class ComparisonFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id")
    private Decision decision;
}
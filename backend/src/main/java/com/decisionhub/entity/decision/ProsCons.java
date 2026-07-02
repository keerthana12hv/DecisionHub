package com.decisionhub.entity.decision;

import jakarta.persistence.*;

@Entity
public class ProsCons {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private DecisionOption option;
}
package com.decisionhub.entity.decision;

import jakarta.persistence.*;

@Entity
public class OptionFactorScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private DecisionOption option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factor_id")
    private ComparisonFactor factor;
}
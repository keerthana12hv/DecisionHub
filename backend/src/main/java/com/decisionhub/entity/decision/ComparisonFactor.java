package com.decisionhub.entity.decision;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "comparison_factors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @OneToMany(mappedBy = "comparisonFactor", fetch = FetchType.LAZY)
    private List<OptionFactorScore> optionScores;
}
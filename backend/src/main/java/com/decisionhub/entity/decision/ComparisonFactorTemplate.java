package com.decisionhub.entity.decision;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comparison_factor_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonFactorTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String templateName;

    @Column(nullable = false, length = 100)
    private String factorName;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer defaultWeight;
}

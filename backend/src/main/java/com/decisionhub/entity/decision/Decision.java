package com.decisionhub.entity.decision;

import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Community;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @OneToMany(mappedBy = "decision", fetch = FetchType.LAZY)
    private List<DecisionOption> options;

    @OneToMany(mappedBy = "decision", fetch = FetchType.LAZY)
    private List<ComparisonFactor> factors;
}
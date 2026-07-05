package com.decisionhub.entity.decision;

import com.decisionhub.entity.voting.Vote;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "decision_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecisionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String optionName;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @OneToMany(mappedBy = "decisionOption", fetch = FetchType.LAZY)
    private List<OptionFactorScore> factorScores;

    @OneToMany(mappedBy = "decisionOption", fetch = FetchType.LAZY)
    private List<ProsCons> prosCons;

    @OneToMany(mappedBy = "decisionOption", fetch = FetchType.LAZY)
    private List<Vote> votes;
}
package com.decisionhub.entity.voting;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    @OneToMany(mappedBy = "poll", fetch = FetchType.LAZY)
    private List<Vote> votes;
}
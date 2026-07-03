package com.decisionhub.entity.voting;

import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.decision.DecisionOption;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "votes",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"poll_id", "user_id", "option_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private DecisionOption decisionOption;

    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt;
}
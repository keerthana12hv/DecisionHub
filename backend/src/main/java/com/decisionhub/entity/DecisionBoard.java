package com.decisionhub.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing a decision board problem space.
 */
@Entity
@Table(
    name = "decision_boards",
    indexes = {
        @Index(name = "idx_decision_boards_status_deadline", columnList = "status, deadline"),
        @Index(name = "idx_decision_boards_community_status", columnList = "community_id, status"),
        @Index(name = "idx_decision_boards_creator_created", columnList = "creator_id, created_at")
    }
)
@SQLDelete(sql = "UPDATE decision_boards SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"options", "tags", "polls", "comparisonFactors"})
public class DecisionBoard extends BaseEntity {

    @NotBlank
    @Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "voting_type", nullable = false, length = 20)
    private VotingType votingType;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "anonymity_type", nullable = false, length = 20)
    private AnonymityType anonymityType = AnonymityType.PUBLIC;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DecisionStatus status = DecisionStatus.ACTIVE;

    @Column(name = "deadline")
    private Instant deadline;

    @Builder.Default
    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DecisionOption> options = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Poll> polls = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ComparisonFactor> comparisonFactors = new ArrayList<>();

    public void addPoll(Poll poll) {
        this.polls.add(poll);
        poll.setDecision(this);
    }

    public void removePoll(Poll poll) {
        this.polls.remove(poll);
        poll.setDecision(null);
    }

    public void addComparisonFactor(ComparisonFactor factor) {
        this.comparisonFactors.add(factor);
        factor.setDecision(this);
    }

    public void removeComparisonFactor(ComparisonFactor factor) {
        this.comparisonFactors.remove(factor);
        factor.setDecision(null);
    }

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "decision_tags",
        joinColumns = @JoinColumn(name = "decision_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id")
    )
    private Set<Tag> tags = new HashSet<>();

    /**
     * Helper to add option.
     *
     * @param option Option to append.
     */
    public void addOption(DecisionOption option) {
        this.options.add(option);
        option.setDecision(this);
    }

    /**
     * Helper to remove option.
     *
     * @param option Option to detach.
     */
    public void removeOption(DecisionOption option) {
        this.options.remove(option);
        option.setDecision(null);
    }

    /**
     * Checks if voting is currently allowed on this board.
     *
     * @return true if active and deadline has not passed, false otherwise.
     */
    public boolean isVotingAllowed() {
        if (this.status != DecisionStatus.ACTIVE) {
            return false;
        }
        return this.deadline == null || Instant.now().isBefore(this.deadline);
    }
}

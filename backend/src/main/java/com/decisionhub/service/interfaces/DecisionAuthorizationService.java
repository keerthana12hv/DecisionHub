package com.decisionhub.security;

import java.util.UUID;

/**
 * Service responsible for enforcing security, permissions, and ownership checks for Decision Boards.
 */
public interface DecisionAuthorizationService {

    /**
     * Checks if a user is allowed to create a decision inside a workspace.
     */
    boolean canCreateDecision(UUID communityId, UUID userId);

    /**
     * Checks if a user is allowed to view a decision board.
     */
    boolean canViewDecision(UUID decisionId, UUID userId);

    /**
     * Checks if a user is allowed to edit a decision board.
     */
    boolean canEditDecision(UUID decisionId, UUID userId);

    /**
     * Checks if a user is allowed to delete a decision board.
     */
    boolean canDeleteDecision(UUID decisionId, UUID userId);

    /**
     * Checks if a user is allowed to transition a decision to ACTIVE state.
     */
    boolean canActivateDecision(UUID decisionId, UUID userId);

    /**
     * Checks if a user is allowed to transition a decision to CLOSED state.
     */
    boolean canCloseDecision(UUID decisionId, UUID userId);

    /**
     * Checks if a user can manage options of a decision board.
     */
    boolean canManageOptions(UUID decisionId, UUID userId);

    /**
     * Checks if a user can manage comparison factors of a decision board.
     */
    boolean canManageComparisonFactors(UUID decisionId, UUID userId);

    /**
     * Checks if a user can submit or update a comparison score on a decision board.
     */
    boolean canSubmitScore(UUID decisionId, UUID userId);
}

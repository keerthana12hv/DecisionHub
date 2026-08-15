package com.decisionhub.security.decision;

/**
 * Service responsible for enforcing security, permissions, and ownership checks for Decisions.
 */
public interface DecisionAuthorizationService {

    /**
     * Checks if a user is allowed to create a decision inside a workspace.
     */
    boolean canCreateDecision(Long communityId, Long userId);

    /**
     * Checks if a user is allowed to view a decision.
     */
    boolean canViewDecision(Long decisionId, Long userId);

    /**
     * Checks if a user is allowed to edit a decision.
     */
    boolean canEditDecision(Long decisionId, Long userId);

    /**
     * Checks if a user is allowed to delete a decision.
     */
    boolean canDeleteDecision(Long decisionId, Long userId);

    /**
     * Checks if a user is allowed to transition a decision to ACTIVE state.
     */
    boolean canActivateDecision(Long decisionId, Long userId);

    /**
     * Checks if a user is allowed to transition a decision to CLOSED state.
     */
    boolean canCloseDecision(Long decisionId, Long userId);

    /**
     * Checks if a user can manage options of a decision.
     */
    boolean canManageOptions(Long decisionId, Long userId);

    /**
     * Checks if a user can manage comparison factors of a decision.
     */
    boolean canManageComparisonFactors(Long decisionId, Long userId);

    /**
     * Checks if a user can submit or update a comparison score on a decision.
     */
    boolean canSubmitScore(Long decisionId, Long userId);

    /**
     * Checks if a user is allowed to manage the Poll lifecycle
     * associated with a Decision.
     *
     * Poll management follows Decision ownership, meaning only
     * the creator/owner of the Decision can manage its Poll.
     */
    boolean canManagePoll(Long decisionId, Long userId);

    /**
     * Checks if a user is allowed to participate in voting for a decision.
     *
     * Voting participation rules:
     * - PUBLIC: Any authenticated user can vote.
     * - COMMUNITY: Only APPROVED members of the associated community can vote.
     *
     * Community privacy is handled by the Community membership mechanism.
     * Decisions belonging to private communities are therefore indirectly
     * protected through approved community membership.
     *
     * @param decisionId ID of the decision.
     * @param userId     ID of the user attempting to vote.
     * @return true if the user is allowed to participate in voting,
     *         otherwise false.
     */
    boolean canParticipateInVoting(Long decisionId, Long userId);

    /**
     * Checks whether a user is allowed to create comments
     * on a Decision.
     *
     * Users can comment only if they are allowed to view
     * the Decision.
     */
    boolean canComment(Long decisionId, Long userId);

    /**
     * Checks whether a user is allowed to edit a comment.
     *
     * Only the comment owner may edit.
     */
    boolean canEditComment(Long commentId, Long userId);

    /**
     * Checks whether a user is allowed to delete a comment.
     *
     * A comment may be deleted by:
     * - The comment owner.
     * - The owner of the community containing the decision,
     *   when the decision belongs to a community.
     */
    boolean canDeleteComment(Long commentId, Long userId);
}

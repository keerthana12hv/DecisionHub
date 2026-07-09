package com.decisionhub.service;

import com.decisionhub.dto.ComparisonScoreRequest;
import com.decisionhub.dto.ComparisonScoreResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface managing ComparisonScore lifecycle and operations.
 */
public interface ComparisonScoreService {

    /**
     * Submits a new comparison score or updates it if it already exists.
     *
     * @param decisionId ID of the parent decision board.
     * @param request    Score submission details.
     * @param ipAddress  Client IP address.
     * @param userAgent  Client user agent.
     * @return ComparisonScoreResponse representing the created/updated score.
     */
    ComparisonScoreResponse submitScore(UUID decisionId, ComparisonScoreRequest request, String ipAddress, String userAgent);

    /**
     * Updates an existing comparison score by key.
     *
     * @param decisionId ID of the parent decision board.
     * @param scoreId    Score ID placeholder in path.
     * @param request    Updated score details.
     * @param ipAddress  Client IP address.
     * @param userAgent  Client user agent.
     * @return ComparisonScoreResponse representing the updated score.
     */
    ComparisonScoreResponse updateScore(UUID decisionId, String scoreId, ComparisonScoreRequest request, String ipAddress, String userAgent);

    /**
     * Retrieves all comparison scores for a decision board.
     *
     * @param decisionId ID of the decision board.
     * @return List of all comparison scores.
     */
    List<ComparisonScoreResponse> getScoresByDecisionId(UUID decisionId);

    /**
     * Retrieves comparison scores submitted by the current authenticated user.
     *
     * @param decisionId ID of the decision board.
     * @return List of current user's comparison scores.
     */
    List<ComparisonScoreResponse> getMyScoresByDecisionId(UUID decisionId);

    /**
     * Deletes a comparison score submitted by the current user on a decision board.
     *
     * @param decisionId ID of the parent decision board.
     * @param optionId   ID of the option.
     * @param factorId   ID of the comparison factor.
     * @param ipAddress  Client IP address.
     * @param userAgent  Client user agent.
     */
    void deleteScore(UUID decisionId, UUID optionId, UUID factorId, String ipAddress, String userAgent);
}

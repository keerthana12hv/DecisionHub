package com.decisionhub.service;

import com.decisionhub.dto.ComparisonFactorRequest;
import com.decisionhub.dto.ComparisonFactorResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface managing ComparisonFactor lifecycle and operations.
 */
public interface ComparisonFactorService {

    /**
     * Creates a new comparison factor on a draft decision board.
     *
     * @param decisionId ID of the parent decision board.
     * @param request    Factor creation details.
     * @param ipAddress  Client IP address for audit logging.
     * @param userAgent  Client user agent for audit logging.
     * @return ComparisonFactorResponse representing the created factor.
     */
    ComparisonFactorResponse createFactor(UUID decisionId, ComparisonFactorRequest request, String ipAddress, String userAgent);

    /**
     * Updates an existing factor's metadata.
     *
     * @param decisionId ID of the parent decision board.
     * @param factorId   ID of the factor to update.
     * @param request    Updated details.
     * @param ipAddress  Client IP address.
     * @param userAgent  Client user agent.
     * @return ComparisonFactorResponse representing the updated factor.
     */
    ComparisonFactorResponse updateFactor(UUID decisionId, UUID factorId, ComparisonFactorRequest request, String ipAddress, String userAgent);

    /**
     * Deletes a comparison factor from a draft decision board.
     *
     * @param decisionId ID of the parent decision board.
     * @param factorId   ID of the factor to delete.
     * @param ipAddress  Client IP address.
     * @param userAgent  Client user agent.
     */
    void deleteFactor(UUID decisionId, UUID factorId, String ipAddress, String userAgent);

    /**
     * Retrieves all comparison factors associated with a decision board.
     *
     * @param decisionId ID of the decision board.
     * @return List of ComparisonFactorResponse.
     */
    List<ComparisonFactorResponse> getFactorsByDecisionId(UUID decisionId);
}

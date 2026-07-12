package com.decisionhub.service.interfaces.decision;

import com.decisionhub.dto.request.decision.ComparisonFactorRequest;
import com.decisionhub.dto.response.decision.ComparisonFactorResponse;

import java.util.List;

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
    ComparisonFactorResponse createFactor(Long decisionId, ComparisonFactorRequest request, String ipAddress, String userAgent);

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
    ComparisonFactorResponse updateFactor(Long decisionId, Long factorId, ComparisonFactorRequest request, String ipAddress, String userAgent);

    /**
     * Deletes a comparison factor from a draft decision board.
     *
     * @param decisionId ID of the parent decision board.
     * @param factorId   ID of the factor to delete.
     * @param ipAddress  Client IP address.
     * @param userAgent  Client user agent.
     */
    void deleteFactor(Long decisionId, Long factorId, String ipAddress, String userAgent);

    /**
     * Retrieves all comparison factors associated with a decision board.
     *
     * @param decisionId ID of the decision board.
     * @return List of ComparisonFactorResponse.
     */
    List<ComparisonFactorResponse> getFactorsByDecisionId(Long decisionId);
}

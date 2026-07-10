package com.decisionhub.service.interfaces.decision;

import com.decisionhub.dto.request.decision.OptionCreateDto;
import com.decisionhub.dto.response.decision.OptionResponseDto;

/**
 * Service interface managing DecisionOption lifecycle and operations.
 */
public interface DecisionOptionService {

    /**
     * Creates a new decision option on a draft decision board.
     *
     * @param decisionId ID of the parent decision board.
     * @param dto        Option creation details.
     * @param ipAddress  Client IP address for audit logging.
     * @param userAgent  Client user agent for audit logging.
     * @return OptionResponseDto representing the created option.
     */
    OptionResponseDto createOption(Long decisionId, OptionCreateDto dto, String ipAddress, String userAgent);

    /**
     * Updates an existing option's metadata.
     *
     * @param decisionId ID of the parent decision board.
     * @param optionId   ID of the option to update.
     * @param dto        Updated details.
     * @param ipAddress  Client IP address.
     * @param userAgent  Client user agent.
     * @return OptionResponseDto representing the updated option.
     */
    OptionResponseDto updateOption(Long decisionId, Long optionId, OptionCreateDto dto, String ipAddress, String userAgent);

    /**
     * Soft deletes an option from a draft decision board.
     *
     * @param decisionId ID of the parent decision board.
     * @param optionId   ID of the option to delete.
     * @param ipAddress  Client IP address.
     * @param userAgent  Client user agent.
     */
    void deleteOption(Long decisionId, Long optionId, String ipAddress, String userAgent);
}

package com.decisionhub.service;

import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.OptionResponseDto;

import java.util.UUID;

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
    OptionResponseDto createOption(UUID decisionId, OptionCreateDto dto, String ipAddress, String userAgent);

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
    OptionResponseDto updateOption(UUID decisionId, UUID optionId, OptionCreateDto dto, String ipAddress, String userAgent);

    /**
     * Soft deletes an option from a draft decision board.
     *
     * @param decisionId ID of the parent decision board.
     * @param optionId   ID of the option to delete.
     * @param ipAddress  Client IP address.
     * @param userAgent  Client user agent.
     */
    void deleteOption(UUID decisionId, UUID optionId, String ipAddress, String userAgent);
}

package com.decisionhub.service.interfaces.support;

import java.util.List;

import com.decisionhub.dto.request.support.SupportRequest;
import com.decisionhub.dto.request.support.UpdateSupportStatusRequest;
import com.decisionhub.dto.response.support.SupportResponse;

public interface SupportService {

    SupportResponse submitTicket(
            SupportRequest request,
            String ipAddress,
            String userAgent
    );

    List<SupportResponse> getMyTickets();

    List<SupportResponse> getAllTickets();

    SupportResponse getTicketById(Long ticketId);

    SupportResponse updateTicketStatus(
            Long ticketId,
            UpdateSupportStatusRequest request,
            String ipAddress,
            String userAgent
    );

}
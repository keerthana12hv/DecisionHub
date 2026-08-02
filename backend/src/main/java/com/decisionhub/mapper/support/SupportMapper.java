package com.decisionhub.mapper.support;

import org.springframework.stereotype.Component;

import com.decisionhub.dto.request.support.SupportRequest;
import com.decisionhub.dto.response.support.SupportResponse;
import com.decisionhub.entity.support.SupportTicket;

@Component
public class SupportMapper {

    public SupportTicket toEntity(SupportRequest request) {
        if (request == null) {
            return null;
        }

        SupportTicket ticket = new SupportTicket();
        ticket.setType(request.getType());
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setRating(request.getRating());

        return ticket;
    }

    public SupportResponse toResponse(SupportTicket ticket) {
        if (ticket == null) {
            return null;
        }

        SupportResponse response = new SupportResponse();
        response.setId(ticket.getId());

        if (ticket.getUser() != null) {
            response.setUserId(ticket.getUser().getId());
            response.setUserName(ticket.getUser().getUsername()); // Change if your User entity uses fullName or username
        }

        response.setType(ticket.getType());
        response.setSubject(ticket.getSubject());
        response.setDescription(ticket.getDescription());
        response.setRating(ticket.getRating());
        response.setStatus(ticket.getStatus());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());

        return response;
    }
}
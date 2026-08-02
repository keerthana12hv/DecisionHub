package com.decisionhub.repository.support;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.support.SupportTicket;
import com.decisionhub.enums.support.SupportTicketType;

@Repository
public interface SupportRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByUserOrderByCreatedAtDesc(User user);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    List<SupportTicket> findByTypeOrderByCreatedAtDesc(SupportTicketType type);

    

}
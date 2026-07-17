package com.decisionhub.service.interfaces.decision;

import com.decisionhub.dto.request.decision.DecisionRequest;
import com.decisionhub.dto.response.decision.DecisionResponse;

import java.util.List;

public interface DecisionService {
    
    DecisionResponse createDecision(DecisionRequest request, String ipAddress, String userAgent);
    
    DecisionResponse getDecisionById(Long id);
    
    List<DecisionResponse> getAllDecisions(Long communityId, String status, Boolean mine);
    
    DecisionResponse updateDecision(Long id, DecisionRequest request, String ipAddress, String userAgent);
    
    DecisionResponse publishDecision(Long id, String ipAddress, String userAgent);
    
    void deleteDecision(Long id, String ipAddress, String userAgent);
}
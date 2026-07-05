package com.decisionhub.service.interfaces.community;

import java.util.List;

import com.decisionhub.dto.request.community.CreateCommunityRequest;
import com.decisionhub.dto.request.community.UpdateCommunityRequest;
import com.decisionhub.dto.response.community.CommunityResponse;

public interface CommunityService {

    CommunityResponse createCommunity(CreateCommunityRequest request);

    List<CommunityResponse> getAllCommunities();

    CommunityResponse getCommunityById(Long communityId);

    CommunityResponse updateCommunity(
            Long communityId,
            UpdateCommunityRequest request
    );

    void deleteCommunity(Long communityId);

    void joinCommunity(Long communityId);

    void leaveCommunity(Long communityId);
}
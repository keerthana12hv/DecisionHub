package com.decisionhub.service.interfaces.community;

import java.util.List;

import com.decisionhub.dto.request.community.CreateCommunityRequest;
import com.decisionhub.dto.request.community.UpdateCommunityRequest;
import com.decisionhub.dto.response.community.CommunityJoinRequestResponse;
import com.decisionhub.dto.response.community.CommunityMemberResponse;
import com.decisionhub.dto.response.community.CommunityResponse;
import com.decisionhub.dto.response.community.JoinCommunityResponse; // 👈 NEW IMPORT

public interface CommunityService {

    CommunityResponse createCommunity(CreateCommunityRequest request);
    
    List<CommunityResponse> getAllCommunities();

    CommunityResponse getCommunityById(Long communityId);

    List<CommunityResponse> getMyCommunities();

    // Dashboard endpoint for the moderator
    List<CommunityResponse> getModeratingCommunities();

    CommunityResponse updateCommunity(
            Long communityId,
            UpdateCommunityRequest request
    );

    void deleteCommunity(Long communityId);

    // ✅ FIXED: Now returns the professional JSON DTO
    JoinCommunityResponse joinCommunity(Long communityId);

    void leaveCommunity(Long communityId);

    // Moderator Request Workflow
    List<CommunityJoinRequestResponse> getPendingRequests(Long communityId);

    void approveJoinRequest(Long communityId, Long memberId);

    void rejectJoinRequest(Long communityId, Long memberId);

    // Final Phase 2 Features
    List<CommunityMemberResponse> getCommunityMembers(Long communityId);

    void removeMember(Long communityId, Long memberId);
}
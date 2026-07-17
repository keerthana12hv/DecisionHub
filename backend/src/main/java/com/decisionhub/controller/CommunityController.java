package com.decisionhub.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.decisionhub.dto.request.community.CreateCommunityRequest;
import com.decisionhub.dto.request.community.UpdateCommunityRequest;
import com.decisionhub.dto.response.community.CommunityJoinRequestResponse;
import com.decisionhub.dto.response.community.CommunityMemberResponse;
import com.decisionhub.dto.response.community.CommunityResponse;
import com.decisionhub.dto.response.community.JoinCommunityResponse; // 👈 NEW IMPORT
import com.decisionhub.service.interfaces.community.CommunityService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/communities")
@Validated
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @PostMapping
    public ResponseEntity<CommunityResponse> createCommunity(
            @Valid @RequestBody CreateCommunityRequest request) {

        return ResponseEntity.ok(
                communityService.createCommunity(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<CommunityResponse>> getAllCommunities() {

        return ResponseEntity.ok(
                communityService.getAllCommunities()
            );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityResponse> getCommunityById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                communityService.getCommunityById(id)
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<CommunityResponse>> getMyCommunities() {
        return ResponseEntity.ok(communityService.getMyCommunities());
    }

    // 👇 Added your missing moderating endpoint here!
    @GetMapping("/moderating")
    public ResponseEntity<List<CommunityResponse>> getModeratingCommunities() {
        return ResponseEntity.ok(
                communityService.getModeratingCommunities()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommunityResponse> updateCommunity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommunityRequest request) {

        return ResponseEntity.ok(
                communityService.updateCommunity(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCommunity(
            @PathVariable Long id) {

        communityService.deleteCommunity(id);

        return ResponseEntity.ok(
                "Community deleted successfully"
        );
    }

    // ✅ FIXED: Now returns the professional JSON DTO
    @PostMapping("/{id}/join")
    public ResponseEntity<JoinCommunityResponse> joinCommunity(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                communityService.joinCommunity(id)
        );
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<String> leaveCommunity(
            @PathVariable Long id) {

        communityService.leaveCommunity(id);

        return ResponseEntity.ok(
                "Left community successfully"
        );
    }

    // Moderator Request Endpoints

    @GetMapping("/{id}/requests")
    public ResponseEntity<List<CommunityJoinRequestResponse>> getPendingRequests(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                communityService.getPendingRequests(id)
        );
    }

    @PutMapping("/{id}/requests/{memberId}/approve")
    public ResponseEntity<String> approveJoinRequest(
            @PathVariable Long id,
            @PathVariable Long memberId) {

        communityService.approveJoinRequest(id, memberId);

        return ResponseEntity.ok("Join request approved");
    }

    @PutMapping("/{id}/requests/{memberId}/reject")
    public ResponseEntity<String> rejectJoinRequest(
            @PathVariable Long id,
            @PathVariable Long memberId) {

        communityService.rejectJoinRequest(id, memberId);

        return ResponseEntity.ok("Join request rejected");
    }

    // Final Phase 2 Features (View Members & Remove Member)

    @GetMapping("/{id}/members")
    public ResponseEntity<List<CommunityMemberResponse>> getCommunityMembers(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                communityService.getCommunityMembers(id)
        );
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<String> removeMember(
            @PathVariable Long id,
            @PathVariable Long memberId) {

        communityService.removeMember(id, memberId);

        return ResponseEntity.ok("Member removed successfully");
    }
}
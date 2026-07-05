package com.decisionhub.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.decisionhub.dto.request.community.CreateCommunityRequest;
import com.decisionhub.dto.request.community.UpdateCommunityRequest;
import com.decisionhub.dto.response.community.CommunityResponse;
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

    @PostMapping("/{id}/join")
    public ResponseEntity<String> joinCommunity(
            @PathVariable Long id) {

        communityService.joinCommunity(id);

        return ResponseEntity.ok(
                "Joined community successfully"
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
}
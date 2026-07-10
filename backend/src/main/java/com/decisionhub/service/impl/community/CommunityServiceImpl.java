package com.decisionhub.service.impl.community;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decisionhub.dto.request.community.CreateCommunityRequest;
import com.decisionhub.dto.request.community.UpdateCommunityRequest;
import com.decisionhub.dto.response.community.CommunityResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Category;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.community.CommunityMember;
import com.decisionhub.enums.community.CommunityMemberRole;
import com.decisionhub.enums.community.MembershipStatus;
import com.decisionhub.mapper.community.CommunityMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.community.CategoryRepository;
import com.decisionhub.repository.community.CommunityMemberRepository;
import com.decisionhub.repository.community.CommunityRepository;
import com.decisionhub.service.interfaces.community.CommunityService;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.ResourceAlreadyExistsException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.exception.BadRequestException;

@Service
@Transactional
public class CommunityServiceImpl implements CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CommunityServiceImpl(
            CommunityRepository communityRepository,
            CommunityMemberRepository communityMemberRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {

        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CommunityResponse createCommunity(CreateCommunityRequest request) {

        if (communityRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Community name already exists");
        }

        if (communityRepository.existsBySlug(request.getSlug())) {
            throw new ResourceAlreadyExistsException("Community slug already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        User owner = getCurrentUser();

        Community community = new Community();
        community.setName(request.getName());
        community.setSlug(request.getSlug());
        community.setDescription(request.getDescription());
        community.setCategory(category);
        community.setOwner(owner);
        community.setVisibility(request.getVisibility());
        community.setMemberCount(1);

        community = communityRepository.save(community);

        CommunityMember member = new CommunityMember();
        member.setCommunity(community);
        member.setUser(owner);
        member.setRole(CommunityMemberRole.OWNER);
        member.setStatus(MembershipStatus.APPROVED);
        member.setJoinedAt(LocalDateTime.now());

        communityMemberRepository.save(member);

        return CommunityMapper.toResponse(community);
    }

    @Override
    public List<CommunityResponse> getAllCommunities() {
        return communityRepository.findByDeletedAtIsNull()
                .stream()
                .map(CommunityMapper::toResponse)
                .toList();
    }

    @Override
    public CommunityResponse getCommunityById(Long communityId) {
        
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));
                
        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        return CommunityMapper.toResponse(community);
    }

    @Override
    public CommunityResponse updateCommunity(
            Long communityId,
            UpdateCommunityRequest request) {

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Community not found"));

        // NEW: Ignore deleted communities
        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        User currentUser = getCurrentUser();
        if (!community.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the community owner can update this community");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        if (!community.getName().equals(request.getName())
                && communityRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Community name already exists");
        }

        if (!community.getSlug().equals(request.getSlug())
                && communityRepository.existsBySlug(request.getSlug())) {
            throw new ResourceAlreadyExistsException("Community slug already exists");
        }

        community.setName(request.getName());
        community.setSlug(request.getSlug());
        community.setDescription(request.getDescription());
        community.setCategory(category);
        community.setVisibility(request.getVisibility());

        community = communityRepository.save(community);

        return CommunityMapper.toResponse(community);
    }

    @Override
    public void deleteCommunity(Long communityId) {

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Community not found"));

        // NEW: Ignore deleted communities
        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        User currentUser = getCurrentUser();
        if (!community.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the community owner can delete this community");
        }

        community.setDeletedAt(LocalDateTime.now());
        community.setMemberCount(0);

        communityRepository.save(community);
    }

    @Override
    public void joinCommunity(Long communityId) {

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Community not found"));

        // NEW: Ignore deleted communities
        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        User user = getCurrentUser();

        Optional<CommunityMember> existingMemberOpt = communityMemberRepository
                .findByCommunityAndUser(community, user);

        if (existingMemberOpt.isPresent()) {
            CommunityMember existingMember = existingMemberOpt.get();
            
            if (existingMember.getStatus() == MembershipStatus.APPROVED) {
                throw new BadRequestException("User is already a member");
            } else if (existingMember.getStatus() == MembershipStatus.LEFT) {
                existingMember.setStatus(MembershipStatus.APPROVED);
                existingMember.setJoinedAt(LocalDateTime.now());
                communityMemberRepository.save(existingMember);
            }
        } else {
            CommunityMember newMember = new CommunityMember();
            newMember.setCommunity(community);
            newMember.setUser(user);
            newMember.setRole(CommunityMemberRole.MEMBER);
            newMember.setStatus(MembershipStatus.APPROVED);
            newMember.setJoinedAt(LocalDateTime.now());
            
            communityMemberRepository.save(newMember);
        }

        community.setMemberCount(
                community.getMemberCount() + 1
        );

        communityRepository.save(community);
    }

    @Override
    public void leaveCommunity(Long communityId) {

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Community not found"));

        // NEW: Ignore deleted communities
        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        User user = getCurrentUser();

        CommunityMember member = communityMemberRepository
                .findByCommunityAndUser(community, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Membership not found"));

        if (member.getStatus() == MembershipStatus.LEFT) {
            throw new BadRequestException("User has already left the community");
        }

        if (member.getRole() == CommunityMemberRole.OWNER) {
            throw new BadRequestException(
                    "Community owner cannot leave the community"
            );
        }

        member.setStatus(MembershipStatus.LEFT);

        communityMemberRepository.save(member);

        community.setMemberCount(
                Math.max(0, community.getMemberCount() - 1)
        );

        communityRepository.save(community);
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }
}
package com.decisionhub.service.impl.community;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decisionhub.dto.request.community.CreateCommunityRequest;
import com.decisionhub.dto.request.community.UpdateCommunityRequest;
import com.decisionhub.dto.response.community.CommunityJoinRequestResponse;
import com.decisionhub.dto.response.community.CommunityMemberResponse;
import com.decisionhub.dto.response.community.CommunityResponse;
import com.decisionhub.dto.response.community.JoinCommunityResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Category;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.community.CommunityMember;
import com.decisionhub.enums.community.CommunityMemberRole;
import com.decisionhub.enums.community.CommunityVisibility;
import com.decisionhub.enums.community.MembershipStatus;
import com.decisionhub.mapper.community.CommunityMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.community.CategoryRepository;
import com.decisionhub.repository.community.CommunityMemberRepository;
import com.decisionhub.repository.community.CommunityRepository;
import com.decisionhub.service.interfaces.community.CommunityService;
import com.decisionhub.enums.authentication.PlatformRole;
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

        if (communityRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException("Community name already exists");
        }

        if (communityRepository.existsBySlug(request.slug())) {
            throw new ResourceAlreadyExistsException("Community slug already exists");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        User owner = getCurrentUser();

        // If a user creates their first active community and their PlatformRole is USER, automatically update it to MODERATOR
        boolean hasActiveCommunity = communityRepository.findByOwner(owner)
                .stream()
                .anyMatch(c -> c.getDeletedAt() == null);
        if (!hasActiveCommunity && owner.getRole() == PlatformRole.USER) {
            owner.setRole(PlatformRole.MODERATOR);
            userRepository.save(owner);
        }

        Community community = new Community();
        community.setName(request.name());
        community.setSlug(request.slug());
        community.setDescription(request.description());
        community.setCategory(category);
        community.setOwner(owner);
        community.setVisibility(request.visibility());
        community.setMemberCount(1);

        community = communityRepository.save(community);

        CommunityMember member = new CommunityMember();
        member.setCommunity(community);
        member.setUser(owner);
        member.setRole(CommunityMemberRole.MODERATOR);
        member.setStatus(MembershipStatus.APPROVED);
        member.setJoinedAt(LocalDateTime.now());

        communityMemberRepository.save(member);

        return CommunityMapper.toResponse(community, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityResponse> getAllCommunities() {
        User currentUser = getCurrentUser();

        Set<Long> joinedCommunityIds = communityMemberRepository.findByUser(currentUser)
                .stream()
                .filter(member -> member.getStatus() == MembershipStatus.APPROVED)
                .map(member -> member.getCommunity().getId())
                .collect(Collectors.toSet());

        return communityRepository.findByDeletedAtIsNull()
                .stream()
                .map(community -> {
                    boolean isMember = joinedCommunityIds.contains(community.getId());
                    return CommunityMapper.toResponse(community, isMember);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityResponse getCommunityById(Long communityId) {
        
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));
                
        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        User currentUser = getCurrentUser();
        boolean isMember = communityMemberRepository
                .findByCommunityAndUser(community, currentUser)
                .map(member -> member.getStatus() == MembershipStatus.APPROVED)
                .orElse(false);

        return CommunityMapper.toResponse(community, isMember);
    }

    @Override
    public CommunityResponse updateCommunity(
            Long communityId,
            UpdateCommunityRequest request) {

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Community not found"));

        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        User currentUser = getCurrentUser();
        if (!community.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the community moderator can update this community");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        if (!community.getName().equals(request.name())
                && communityRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException("Community name already exists");
        }

        if (!community.getSlug().equals(request.slug())
                && communityRepository.existsBySlug(request.slug())) {
            throw new ResourceAlreadyExistsException("Community slug already exists");
        }

        community.setName(request.name());
        community.setSlug(request.slug());
        community.setDescription(request.description());
        community.setCategory(category);
        community.setVisibility(request.visibility());

        community = communityRepository.save(community);

        return CommunityMapper.toResponse(community, true);
    }

    @Override
    public void deleteCommunity(Long communityId) {

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Community not found"));

        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        User currentUser = getCurrentUser();
        if (!community.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the community moderator can delete this community");
        }

        community.setDeletedAt(LocalDateTime.now());
        community.setMemberCount(0);

        communityRepository.save(community);
    }

    @Override
    public JoinCommunityResponse joinCommunity(Long communityId) {

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Community not found"));

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
            } 
            if (existingMember.getStatus() == MembershipStatus.PENDING) {
                throw new BadRequestException("Join request is already pending");
            }
            if (existingMember.getStatus() == MembershipStatus.REJECTED) {
                existingMember.setStatus(MembershipStatus.PENDING);
                existingMember.setJoinedAt(LocalDateTime.now());
                communityMemberRepository.save(existingMember);
                return new JoinCommunityResponse("Join request sent successfully", "PENDING");
            }
            if (existingMember.getStatus() == MembershipStatus.LEFT) {
                existingMember.setJoinedAt(LocalDateTime.now());
                if (community.getVisibility() == CommunityVisibility.PUBLIC) {
                    existingMember.setStatus(MembershipStatus.APPROVED);
                    
                    communityMemberRepository.save(existingMember);
                    
                    community.setMemberCount(community.getMemberCount() + 1);
                    communityRepository.save(community);
                    return new JoinCommunityResponse("Joined community successfully", "APPROVED");
                } else {
                    existingMember.setStatus(MembershipStatus.PENDING);
                    communityMemberRepository.save(existingMember);
                    return new JoinCommunityResponse("Join request sent successfully", "PENDING");
                }
            }
        } else {
            CommunityMember newMember = new CommunityMember();
            newMember.setCommunity(community);
            newMember.setUser(user);
            newMember.setRole(CommunityMemberRole.MEMBER);
            newMember.setJoinedAt(LocalDateTime.now());
            
            if (community.getVisibility() == CommunityVisibility.PUBLIC) {
                newMember.setStatus(MembershipStatus.APPROVED);
                communityMemberRepository.save(newMember);
                community.setMemberCount(community.getMemberCount() + 1);
                communityRepository.save(community);
                return new JoinCommunityResponse("Joined community successfully", "APPROVED");
            } else {
                newMember.setStatus(MembershipStatus.PENDING);
                communityMemberRepository.save(newMember);
                return new JoinCommunityResponse("Join request sent successfully", "PENDING");
            }
        }
        
        throw new IllegalStateException("Unexpected membership state.");
    }

    @Override
    public void leaveCommunity(Long communityId) {

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Community not found"));

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

        if (member.getRole() == CommunityMemberRole.MODERATOR) {
            throw new BadRequestException(
                    "Community moderator cannot leave the community"
            );
        }

        member.setStatus(MembershipStatus.LEFT);

        communityMemberRepository.save(member);

        community.setMemberCount(
                Math.max(0, community.getMemberCount() - 1)
        );

        communityRepository.save(community);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityResponse> getMyCommunities() {

        User currentUser = getCurrentUser();

        return communityMemberRepository.findByUser(currentUser)
                .stream()
                .filter(member -> member.getStatus() == MembershipStatus.APPROVED)
                .map(CommunityMember::getCommunity)
                .filter(community -> community.getDeletedAt() == null)
                .map(community -> CommunityMapper.toResponse(community, true))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityResponse> getModeratingCommunities() {
        User currentUser = getCurrentUser();

        return communityRepository.findByOwner(currentUser)
                .stream()
                .filter(community -> community.getDeletedAt() == null)
                .map(community -> CommunityMapper.toResponse(community, true))
                .toList();
    }

    @Override
    public List<CommunityJoinRequestResponse> getPendingRequests(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        User currentUser = getCurrentUser();
        if (!community.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the community moderator can view pending requests");
        }

        return communityMemberRepository.findByCommunityAndStatus(community, MembershipStatus.PENDING)
                .stream()
                .map(member -> new CommunityJoinRequestResponse(
                        member.getId(),
                        member.getUser().getId(),
                        member.getUser().getUsername(),
                        member.getUser().getEmail(),
                        member.getJoinedAt()
                ))
                .toList();
    }

    @Override
    public void approveJoinRequest(Long communityId, Long memberId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        User currentUser = getCurrentUser();
        if (!community.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the community moderator can approve requests");
        }

        CommunityMember member = communityMemberRepository.findByIdAndCommunity(memberId, community)
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found"));

        if (member.getStatus() != MembershipStatus.PENDING) {
            throw new BadRequestException("Join request is not pending");
        }

        member.setStatus(MembershipStatus.APPROVED);
        member.setJoinedAt(LocalDateTime.now());
        communityMemberRepository.save(member);

        community.setMemberCount(community.getMemberCount() + 1);
        communityRepository.save(community);
    }

    @Override
    public void rejectJoinRequest(Long communityId, Long memberId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        User currentUser = getCurrentUser();
        if (!community.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the community moderator can reject requests");
        }

        CommunityMember member = communityMemberRepository.findByIdAndCommunity(memberId, community)
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found"));

        if (member.getStatus() != MembershipStatus.PENDING) {
            throw new BadRequestException("Join request is not pending");
        }

        member.setStatus(MembershipStatus.REJECTED);
        communityMemberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityMemberResponse> getCommunityMembers(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        User currentUser = getCurrentUser();
        CommunityMember currentMember = communityMemberRepository
                .findByCommunityAndUser(community, currentUser)
                .orElseThrow(() -> new UnauthorizedActionException("You are not a member of this community"));

        if (currentMember.getStatus() != MembershipStatus.APPROVED) {
            throw new UnauthorizedActionException("You must be an approved member to view the member list");
        }

        return communityMemberRepository.findByCommunityAndStatus(community, MembershipStatus.APPROVED)
                .stream()
                .map(member -> new CommunityMemberResponse(
                        member.getId(),
                        member.getUser().getId(),
                        member.getUser().getUsername(),
                        member.getUser().getEmail(),
                        member.getRole(),
                        member.getStatus(),
                        member.getJoinedAt()
                ))
                .toList();
    }

    @Override
    public void removeMember(Long communityId, Long memberId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));

        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        User currentUser = getCurrentUser();
        if (!community.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the community moderator can remove members");
        }

        CommunityMember member = communityMemberRepository.findByIdAndCommunity(memberId, community)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));

        if (member.getRole() == CommunityMemberRole.MODERATOR) {
            throw new BadRequestException("Cannot remove the community moderator");
        }

        if (member.getStatus() == MembershipStatus.LEFT) {
            throw new BadRequestException("User has already left the community");
        }

        if (member.getStatus() != MembershipStatus.APPROVED) {
            throw new BadRequestException("Only approved members can be removed");
        }

        member.setStatus(MembershipStatus.LEFT);
        communityMemberRepository.save(member);

        community.setMemberCount(Math.max(0, community.getMemberCount() - 1));
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
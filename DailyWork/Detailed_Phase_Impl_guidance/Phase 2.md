# Phase 2 – Community Management

## 1. Objective

Implement the complete Community Management module that enables authenticated users to create, manage, and participate in communities.

This phase establishes the foundation for future modules such as Decision Management, Discussions, Voting, Notifications, Analytics, Reports, and Administration.

Authenticated users should be able to:

- Create communities
- Browse communities
- View community details
- Update communities
- Soft delete communities
- Join communities
- Leave communities

---

# 2. Modules

## Community Management

- Create Community
- View Communities
- View Community Details
- Update Community
- Soft Delete Community

## Community Membership

- Join Community
- Leave Community
- Community Owner
- Community Member
- Membership Status
- Member Count Management

## Community Categories

Communities belong to predefined categories.

Examples

- Career
- Education
- Technology
- Finance
- Travel
- Lifestyle

Categories are **read-only** in this phase.

Category creation, updates, activation, and deletion belong to the Administration Module (Phase 8).

---

# 3. Enums

## CommunityVisibility

- PUBLIC
- PRIVATE

---

## CommunityMemberRole

- OWNER
- MEMBER

---

## MembershipStatus

- APPROVED
- PENDING
- REJECTED
- LEFT

---

# 4. Entities

## Category

### Responsibilities

Stores predefined community categories.

### Database Mapping

Table

```
categories
```

Columns

- id
- name
- slug
- is_active

---

## Community

### Responsibilities

Represents a user-created community.

### Database Mapping

Table

```
communities
```

Columns

- id
- name
- slug
- description
- category_id
- owner_id
- visibility
- member_count
- deleted_at

---

## CommunityMember

### Responsibilities

Stores membership information between users and communities.

### Database Mapping

Table

```
community_members
```

Columns

- id
- community_id
- user_id
- role
- status
- joined_at

---

# 5. Java Files

## Entity

```
Category.java

Community.java

CommunityMember.java
```

---

## Repository

```
CategoryRepository.java

CommunityRepository.java

CommunityMemberRepository.java
```

---

## Request DTOs

```
CreateCommunityRequest.java

UpdateCommunityRequest.java
```

---

## Response DTOs

```
CommunityResponse.java
```

---

## Mapper

```
CommunityMapper.java
```

---

## Service

```
CommunityService.java
```

---

## Service Implementation

```
CommunityServiceImpl.java
```

---

## Controller

```
CommunityController.java
```

---

## Exceptions

Uses the global exception infrastructure introduced in Phase 1.

---

# 6. Database Tables

```
categories

communities

community_members
```

---

# 7. REST APIs

## Create Community

POST

```
/api/communities
```

Authentication

Required

Purpose

- Create a new community
- Logged-in user becomes OWNER
- Owner automatically becomes first approved member

---

## Get All Communities

GET

```
/api/communities
```

Authentication

Required

Purpose

Returns all active communities.

Soft deleted communities are excluded.

---

## Get Community By ID

GET

```
/api/communities/{id}
```

Authentication

Required

Purpose

Returns community details.

Soft deleted communities cannot be accessed.

---

## Update Community

PUT

```
/api/communities/{id}
```

Authentication

Required

Purpose

Update community information.

Only the OWNER can update.

---

## Delete Community

DELETE

```
/api/communities/{id}
```

Authentication

Required

Purpose

Soft delete community.

Only OWNER can delete.

Deletes by

- setting deleted_at
- setting member_count = 0

Community records are never physically removed.

---

## Join Community

POST

```
/api/communities/{id}/join
```

Authentication

Required

Purpose

Join a community.

Behavior

- New membership → Create membership
- LEFT member → Rejoin community
- APPROVED member → Error

---

## Leave Community

POST

```
/api/communities/{id}/leave
```

Authentication

Required

Purpose

Leave a community.

Behavior

- Membership becomes LEFT
- Member count decreases
- OWNER cannot leave

---

# 8. Business Rules

## Community Creation

When a community is created

- Owner is automatically assigned
- CommunityMember record is created
- Role = OWNER
- Status = APPROVED
- Member Count = 1

---

## Join Community

### New User

Allowed

Creates a new CommunityMember.

---

### Existing APPROVED Member

Not allowed.

Returns

```
User is already a member
```

---

### Existing LEFT Member

Allowed.

Updates

```
LEFT → APPROVED
```

Joined date is refreshed.

Member count increases.

---

## Leave Community

Allowed only when

- Membership exists
- Membership status is APPROVED

---

### OWNER

Not allowed.

Returns

```
Community owner cannot leave the community
```

---

### Already LEFT

Not allowed.

Returns

```
User has already left the community
```

---

### Normal Member

Membership

```
APPROVED → LEFT
```

Member count decreases.

---

## Update Community

Allowed only for OWNER.

Validation

- Category exists
- Community name is unique
- Community slug is unique

---

## Delete Community

Allowed only for OWNER.

Soft delete

Updates

- deleted_at
- member_count = 0

CommunityMember records remain unchanged for historical tracking.

---

# 9. Security

Protected using Spring Security and JWT.

All APIs require authentication.

Current user is obtained using

```
SecurityContextHolder
```

through

```
getCurrentUser()
```

Ownership is validated before

- Update Community
- Delete Community

---

# 10. Validation

Community

- Name required
- Slug required
- Category required
- Visibility required

Optional

- Description

Duplicate Validation

- Community Name
- Community Slug

Category Validation

- Category must exist

---

# 11. Exception Handling

Handled through

```
GlobalExceptionHandler
```

Examples

- Community Not Found
- Category Not Found
- Duplicate Community Name
- Duplicate Community Slug
- User Already Member
- Membership Not Found
- User Already Left
- Community Owner Cannot Leave
- Only Owner Can Update
- Only Owner Can Delete

---

# 12. Edge Cases Covered

- Duplicate community names
- Duplicate community slugs
- Invalid category
- Invalid community ID
- Soft deleted community access
- Owner automatically becomes first member
- Owner automatically receives OWNER role
- Owner automatically receives APPROVED membership
- Same user cannot join twice
- User can rejoin after leaving
- User cannot leave twice
- OWNER cannot leave
- Only OWNER can update
- Only OWNER can delete
- Member count remains synchronized
- Soft delete instead of physical delete
- CommunityMember history preserved

---

# 13. Dependencies

Depends on

- Phase 1 Authentication
- JWT Authentication
- User Entity

Provides foundation for

- Phase 3 Decision Management
- Phase 4 Voting
- Phase 5 Discussion
- Phase 6 Notifications
- Phase 7 Reports
- Phase 8 Administration

---

# 14. Deliverables

- Community CRUD APIs
- Community Membership APIs
- Join Workflow
- Leave Workflow
- Community Ownership
- Member Count Management
- Soft Delete Support
- JWT Protected APIs
- Validation
- Global Exception Handling
- Postman Testing
- Frontend Ready

---

# 15. Notes for Future Phases

## Phase 3

Every Decision belongs to a Community.

Community membership will be used to authorize decision creation and participation.

---

## Phase 5

Discussion and comments will be associated with Community Decisions.

---

## Phase 6

Notifications will be generated for community events such as joins, decisions, and discussions.

---

## Phase 7

Reports and analytics will use

- Total Members
- Active Communities
- Community Participation
- Popular Categories

---

## Phase 8

Administration will manage

- Categories
- Community Moderation
- Audit Logs
- Platform Management

The Community module only **reads** categories and never creates or manages them.
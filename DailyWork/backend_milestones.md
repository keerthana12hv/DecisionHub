# Backend Development Roadmap

The backend development follows a milestone-wise approach. Each phase
should be completed before moving to the next phase.

## Implementation Order (Follow for Every Phase)

1.  Create Enum Classes (if applicable)
2.  Create Entity Classes
3.  Create Repository Interfaces
4.  Create Request & Response DTOs
5.  Create Service Interface
6.  Create Service Implementation
7.  Create Controller APIs
8.  Test APIs using Postman
9.  Integrate with Frontend

------------------------------------------------------------------------

## 🚩 Milestone 1 (Week 1 -- Week 2)

**Objective:** Build the backend foundation by configuring PostgreSQL,
implementing Authentication & Authorization, and developing the core
Community and Decision Management modules.

### Phase 1 -- Authentication & Security

**Modules**

**Enums** - PlatformRole - UserStatus

**Entities** - User - OAuthAccount - PasswordResetToken

**Java Files**

-   **Entity**
    -   `User.java`
    -   `OAuthAccount.java`
    -   `PasswordResetToken.java`
-   **Repository**
    -   `UserRepository.java`
    -   `OAuthAccountRepository.java`
    -   `PasswordResetTokenRepository.java`
-   **Service**
    -   `AuthService.java`
    -   `AuthServiceImpl.java`
-   **Controller**
    -   `AuthController.java`
-   **DTO**
    -   **Request**
        -   `LoginRequest.java`
        -   `RegisterRequest.java`
    -   **Response**
        -   `LoginResponse.java`
        -   `RegisterResponse.java`
        -   `UserResponse.java`
-   **Security**
    -   `SecurityConfig.java`
    -   `JwtAuthenticationFilter.java`
    -   `JwtService.java`
    -   `CustomUserDetailsService.java`
    -   `PasswordConfig.java`

**REST APIs** - POST /auth/register - POST /auth/login - GET
/auth/profile

**Deliverables** - User Registration - User Login - JWT Authentication -
Role-Based Authorization - Password Encryption - Authentication APIs

------------------------------------------------------------------------

### Phase 2 -- Community Management

**Objective:** Manage communities, memberships and user interests.

**Modules**

**Enums** - CommunityVisibility - CommunityMemberRole - MembershipStatus

**Entities** - Category - UserInterest - Community - CommunityMember

**Java Files**

-   **Entity**
    -   `Category.java`
    -   `UserInterest.java`
    -   `Community.java`
    -   `CommunityMember.java`
-   **Repository**
    -   `CategoryRepository.java`
    -   `UserInterestRepository.java`
    -   `CommunityRepository.java`
    -   `CommunityMemberRepository.java`
-   **Service**
    -   `CommunityService.java`
    -   `CommunityServiceImpl.java`
-   **Controller**
    -   `CommunityController.java`
-   **DTO**
    -   **Request**
        -   `CreateCommunityRequest.java`
        -   `UpdateCommunityRequest.java`
    -   **Response**
        -   `CommunityResponse.java`

**REST APIs** - POST /communities - GET /communities - GET
/communities/{id} - PUT /communities/{id} - DELETE /communities/{id} -
POST /communities/{id}/join - POST /communities/{id}/leave

**Deliverables** - Community CRUD - Join Community - Leave Community -
Community Membership

------------------------------------------------------------------------

### Phase 3 -- Decision Management

**Objective:** Manage decisions, options, comparison factors and pros &
cons.

**Modules**

**Enums** - DecisionStatus - DecisionVisibility - ProsConsType

**Entities** - Decision - DecisionOption - ComparisonFactor -
ComparisonFactorTemplate - OptionFactorScore - ProsCons

**Java Files**

-   **Entity**
    -   `Decision.java`
    -   `DecisionOption.java`
    -   `ComparisonFactor.java`
    -   `ComparisonFactorTemplate.java`
    -   `OptionFactorScore.java`
    -   `ProsCons.java`
-   **Repository**
    -   `DecisionRepository.java`
    -   `DecisionOptionRepository.java`
    -   `ComparisonFactorRepository.java`
    -   `ComparisonFactorTemplateRepository.java`
    -   `OptionFactorScoreRepository.java`
    -   `ProsConsRepository.java`
-   **Service**
    -   `DecisionService.java`
    -   `DecisionServiceImpl.java`
-   **Controller**
    -   `DecisionController.java`
-   **DTO**
    -   **Request**
        -   `CreateDecisionRequest.java`
        -   `UpdateDecisionRequest.java`
    -   **Response**
        -   `DecisionResponse.java`

**REST APIs** - POST /decisions - GET /decisions - GET /decisions/{id} -
PUT /decisions/{id} - DELETE /decisions/{id} - POST
/decisions/{decisionId}/options - DELETE
/decisions/{decisionId}/options/{optionId}

**Deliverables** - Decision CRUD - Option Management - Comparison
Factors - Pros & Cons Management

### ✅ Milestone 1 Deliverables

-   PostgreSQL Configuration
-   Entity Layer
-   Repository Layer
-   DTO Layer
-   Service Layer
-   Controller Layer
-   Security Layer
-   Authentication System
-   JWT Security
-   User Management
-   Community Management
-   Decision Management

------------------------------------------------------------------------

## 🚩 Milestone 2 (Week 3 -- Week 4)

**Objective:** Implement user engagement and collaboration features.

### Phase 4 -- Voting

**Modules**

**Enums** - PollType - PollStatus

**Entities** - Poll - Vote

**Java Files**

-   **Entity**
    -   `Poll.java`
    -   `Vote.java`
-   **Repository**
    -   `PollRepository.java`
    -   `VoteRepository.java`
-   **Service**
    -   `PollService.java`
    -   `PollServiceImpl.java`
    -   `VoteService.java`
    -   `VoteServiceImpl.java`
-   **Controller**
    -   `PollController.java`
    -   `VoteController.java`
-   **DTO**
    -   **Request**
        -   `PollRequest.java`
        -   `VoteRequest.java`
    -   **Response**
        -   `PollResponse.java`
        -   `VoteResponse.java`

**REST APIs** - POST /polls - GET /polls/{id} - POST /votes - PUT
/votes/{id} - GET /polls/{id}/results

**Deliverables** - Poll Creation - Voting System - Poll Results

------------------------------------------------------------------------

### Phase 5 -- Discussion

**Modules** - Comment

**Java Files** - **Entity** - `Comment.java` - **Repository** -
`CommentRepository.java` - **Service** - `CommentService.java` -
`CommentServiceImpl.java` - **Controller** - `CommentController.java` -
**DTO** - **Request** - `CommentRequest.java` - **Response** -
`CommentResponse.java`

**REST APIs** - POST /comments - POST /comments/{id}/reply - GET
/comments - PUT /comments/{id} - DELETE /comments/{id}

**Deliverables** - Comment System - Replies - Discussion Threads

------------------------------------------------------------------------

### Phase 6 -- Notifications

**Modules**

**Enums** - NotificationType

**Entities** - Notification - NotificationPreference

**Java Files**

-   **Entity**
    -   `Notification.java`
    -   `NotificationPreference.java`
-   **Repository**
    -   `NotificationRepository.java`
    -   `NotificationPreferenceRepository.java`
-   **Service**
    -   `NotificationService.java`
    -   `NotificationServiceImpl.java`
-   **Controller**
    -   `NotificationController.java`
-   **DTO**
    -   **Request**
        -   `NotificationRequest.java`
    -   **Response**
        -   `NotificationResponse.java`

**REST APIs** - GET /notifications - PUT /notifications/{id}/read - PUT
/notification-preferences

**Deliverables** - Notification System - Notification Preferences - Read
Status Management

### ✅ Milestone 2 Deliverables

-   Poll System
-   Voting System
-   Comments
-   Discussion Threads
-   Notification System
-   Community Interaction Features

# Backend Development Roadmap (Milestone 3)

## 🚩 Milestone 3 (Week 5 -- Week 6)

**Objective:** Complete reporting, administration, testing,
optimization, and prepare the backend for deployment.

### Phase 7 -- Reports

**Modules**

**Enums** - ReportType

**Entities** - ReportExport

**Java Files**

-   **Entity**
    -   `ReportExport.java`
-   **Repository**
    -   `ReportExportRepository.java`
-   **Service**
    -   `ReportService.java`
    -   `ReportServiceImpl.java`
-   **Controller**
    -   `ReportController.java`
-   **DTO**
    -   **Request**
        -   `ReportRequest.java`
    -   **Response**
        -   `ReportResponse.java`

**REST APIs** - GET /reports - GET /reports/{id} - POST /reports/export

**Deliverables** - PDF Report Export - CSV Report Export - Report
Download

------------------------------------------------------------------------

### Phase 8 -- Administration

**Modules**

**Enums** - AuditActionType - ModerationActionType

**Entities** - AuditLog - ModerationAction

**Java Files**

-   **Entity**
    -   `AuditLog.java`
    -   `ModerationAction.java`
-   **Repository**
    -   `AuditLogRepository.java`
    -   `ModerationActionRepository.java`
-   **Service**
    -   `AdminService.java`
    -   `AdminServiceImpl.java`
-   **Controller**
    -   `AdminController.java`

**REST APIs** - GET /admin/audit-logs - DELETE /admin/decisions/{id} -
DELETE /admin/users/{id} - POST /admin/moderation-actions

**Deliverables** - Audit Logs - Moderation Features - Administration
Features

### ✅ Milestone 3 Deliverables

-   Report Generation
-   Administration Module
-   Audit Logs
-   Moderation Features
-   Backend Testing
-   Bug Fixes
-   Performance Optimization
-   Backend Ready for Deployment


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

### Phase 4 -- Voting and Polling 

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

### Phase 5 -- Discussion and Feedback system

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

# Phase 8 – Administration

## Overview

The Administration module provides platform-wide management features that are not handled by normal users or community owners.

It enables administrators to manage users, communities, categories, decisions, reports, moderation, audit logs, and overall platform operations.

This module completes the backend by providing administrative control over all major modules implemented in previous phases.

---

# Modules

## Enums

- `AuditActionType`
- `ModerationActionType`

---

## Entities

- `AuditLog`
- `ModerationAction`

---

# Java Files

## Entity

- `AuditLog.java`
- `ModerationAction.java`

---

## Repository

- `AuditLogRepository.java`
- `ModerationActionRepository.java`

---

## Service

- `AdminService.java`
- `AdminServiceImpl.java`

---

## Controller

- `AdminController.java`

---

# Features

---

# 1. Admin Dashboard

Provides a centralized dashboard for monitoring the platform.

### Dashboard Information

- Total Users
- Total Communities
- Total Decisions
- Total Polls
- Total Comments
- Total Votes
- Active Users
- Active Communities
- Recent Activities

---

# 2. User Management

Administrators manage platform users.

### Features

- View all users
- View user profile
- Search users
- Filter users
- Suspend users
- Activate users
- Soft delete users
- Restore users (Optional)
- Change user roles (USER ↔ MODERATOR)

---

# 3. Community Management

Administrators manage communities across the platform.

### Features

- View all communities
- View community details
- Search communities
- Delete inappropriate communities
- Restore deleted communities (Optional)
- View community members

> Community owners manage only their own communities.  
> Administrators can manage every community on the platform.

---

# 4. Category Management

Categories are master data used by the Community module.

Only administrators can create or manage categories.

### Features

- Create Category
- View Categories
- View Category Details
- Update Category
- Delete Category (Soft Delete if required)

> Community creators do **not** create categories.  
> While creating a community they simply select an existing category.

Example Categories:

- Technology
- Sports
- Education
- Gaming
- Business
- Entertainment

---

# 5. Decision Management

Administrators manage platform decisions.

### Features

- View all decisions
- Delete inappropriate decisions
- Restore deleted decisions (Optional)
- Moderate reported decisions

---

# 6. Comment Moderation

Administrators moderate discussions.

### Features

- View comments
- Delete offensive comments
- Moderate reported comments

---

# 7. Moderation

Perform moderation actions across the platform.

### Features

- Warn users
- Suspend users
- Ban users
- Remove inappropriate communities
- Remove inappropriate decisions
- Remove offensive comments

All moderation actions are recorded.

---

# 8. Audit Logs

Maintain complete audit history of administrative operations.

### Logged Events

- User Login
- User Registration
- Community Creation
- Community Update
- Community Deletion
- Decision Creation
- Decision Deletion
- Category Creation
- Category Update
- Category Deletion
- Moderation Actions
- Admin Activities

Audit logs help with:

- Monitoring
- Security
- Debugging
- Compliance

---

# 9. Reports

Generate reports for platform administration.

### Features

- Generate Reports
- Export Reports
- Download Reports

Supported Formats

- PDF
- CSV
- Excel

---

# 10. Platform Analytics

Provide statistics about the platform.

### Statistics

- Total Users
- Total Communities
- Total Decisions
- Total Polls
- Total Comments
- Total Votes
- Community Growth
- User Growth
- Most Active Communities

---

# 11. Admin Authorization

Only administrators can access administration APIs.

Platform Roles

```text
USER
MODERATOR
ADMIN
```

Security Rules

- USER → Normal platform access
- MODERATOR → Moderation features only (if required)
- ADMIN → Full administrative access

All Admin APIs must verify that the logged-in user has the `ADMIN` role before executing administrative operations.

---

# REST APIs

## Dashboard

```http
GET /api/admin/dashboard
```

---

## User Management

```http
GET    /api/admin/users

GET    /api/admin/users/{id}

PUT    /api/admin/users/{id}/status

PUT    /api/admin/users/{id}/role

DELETE /api/admin/users/{id}
```

---

## Community Management

```http
GET    /api/admin/communities

GET    /api/admin/communities/{id}

DELETE /api/admin/communities/{id}
```

---

## Category Management

```http
POST   /api/admin/categories

GET    /api/admin/categories

GET    /api/admin/categories/{id}

PUT    /api/admin/categories/{id}

DELETE /api/admin/categories/{id}
```

---

## Decision Management

```http
GET    /api/admin/decisions

DELETE /api/admin/decisions/{id}
```

---

## Comment Moderation

```http
GET    /api/admin/comments

DELETE /api/admin/comments/{id}
```

---

## Moderation

```http
POST   /api/admin/moderation-actions
```

---

## Audit Logs

```http
GET /api/admin/audit-logs
```

---

## Reports

```http
GET /api/admin/reports

GET /api/admin/reports/{id}
```

---

## Platform Statistics

```http
GET /api/admin/statistics
```

---

# Deliverables

- Admin Dashboard
- User Management
- Community Management
- Category Management
- Decision Management
- Comment Moderation
- Moderation Features
- Audit Logs
- Platform Analytics
- Report Management
- Admin Authorization

---

# Milestone 3 Deliverables

- Report Generation
- Administration Module
- User Management
- Community Management
- Category Management
- Decision Management
- Moderation Features
- Audit Logs
- Platform Analytics
- Backend Testing
- Bug Fixes
- Performance Optimization
- Backend Ready for Deployment

---

# Relationship with Previous Phases

| Phase | Responsibility | Managed By |
|--------|----------------|------------|
| Authentication | Register/Login Users | Users |
| Community | Create & Manage Communities | Community Owner |
| Decisions | Create Decisions | Decision Creator |
| Voting | Vote on Polls | Users |
| Discussion | Comments | Users |
| Notifications | Notifications | System |
| Reports | Generate Reports | Users/Admin |
| Administration | Manage Entire Platform | **Administrator** |

---

# Administration Responsibilities

The Administration module acts as the central management layer of the platform.

Administrators are responsible for:

- Managing Users
- Managing Communities
- Managing Categories
- Managing Decisions
- Moderating Comments
- Performing Moderation Actions
- Viewing Audit Logs
- Monitoring Platform Analytics
- Generating Reports
- Managing Overall Platform Operations

This separation follows industry-standard architecture, where normal users create and interact with platform content, while administrators oversee, moderate, and maintain the entire system.
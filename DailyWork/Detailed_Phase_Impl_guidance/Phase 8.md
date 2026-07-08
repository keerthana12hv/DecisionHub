# Phase 8 – Administration, Reports & Final Backend Integration

## 1. Objective

Implement the complete **Administration Module**, **Reporting System**, and perform the final backend integration required to make the DecisionHub platform production-ready.

This phase provides administrators with the ability to manage users, communities, categories, decisions, reports, moderation activities, audit logs, and overall platform health.

It also completes backend optimization, testing, documentation, deployment preparation, and production readiness.

---

# 2. Modules

## Administration

- Admin Dashboard
- User Management
- Category Management
- Community Management
- Decision Management
- Moderation
- Audit Logs
- Report Management

---

## Category Management

Categories are master data used throughout the application.

Only administrators can manage categories.

Features

- Create Category
- View Categories
- Update Category
- Activate Category
- Deactivate Category

---

## User Management

- View Users
- View User Details
- Suspend User
- Activate User
- Soft Delete User

---

## Community Administration

- View Communities
- Restore Soft Deleted Communities
- Moderate Communities

---

## Decision Administration

- View Decisions
- Restore Soft Deleted Decisions
- Moderate Decisions

---

## Moderation

- Moderate Users
- Moderate Communities
- Moderate Decisions
- Moderate Comments
- Record Moderation Actions

---

## Audit Logs

Track every important administrative operation.

Examples

- User Suspended
- Category Created
- Community Deleted
- Decision Restored
- Report Generated

---

## Reports

Generate platform reports using existing application data.

Supported report types

- PDF
- CSV

Generated reports are tracked using the existing **ReportExport** entity.

---

## Final Backend Integration

- API Integration Testing
- End-to-End Testing
- Performance Optimization
- Security Verification
- Production Configuration
- Documentation
- Deployment Preparation

---

# 3. Enums

## AuditActionType

Examples

- USER_CREATED
- USER_UPDATED
- USER_DELETED
- COMMUNITY_CREATED
- COMMUNITY_UPDATED
- COMMUNITY_DELETED
- CATEGORY_CREATED
- CATEGORY_UPDATED
- CATEGORY_DELETED
- DECISION_CREATED
- DECISION_UPDATED
- DECISION_DELETED
- REPORT_GENERATED
- LOGIN
- LOGOUT

---

## ModerationActionType

Examples

- DELETE_USER
- SUSPEND_USER
- ACTIVATE_USER
- DELETE_COMMUNITY
- DELETE_DECISION
- DELETE_COMMENT
- RESTORE_CONTENT
- WARNING
- BAN_USER

---

## ReportType

- PDF
- CSV

---

# 4. Entities

## AuditLog

### Responsibilities

- Administrator
- Action Type
- Target Entity
- Target Entity ID
- Description
- Timestamp

---

## ModerationAction

### Responsibilities

- Moderator/Admin
- Action Type
- Target User
- Target Community
- Target Decision
- Reason
- Timestamp

---

## ReportExport

### Responsibilities

- Report Type
- Requested By
- File URL
- Generated Timestamp

---

## Category

Administration manages the Category entity introduced in Phase 2.

### Responsibilities

- Create
- Update
- Activate
- Deactivate

---

# 5. Java Files

## Entity

```text
AuditLog.java

ModerationAction.java

ReportExport.java
```

---

## Repository

```text
AuditLogRepository.java

ModerationActionRepository.java

ReportExportRepository.java

CategoryRepository.java
```

---

## Request DTOs

```text
CreateCategoryRequest.java

UpdateCategoryRequest.java

ModerationRequest.java

ReportRequest.java
```

---

## Response DTOs

```text
AuditLogResponse.java

ModerationResponse.java

CategoryResponse.java

ReportResponse.java
```

---

## Mapper

```text
AdminMapper.java

ReportMapper.java
```

---

## Service

```text
AdminService.java

ReportService.java
```

---

## Service Implementation

```text
AdminServiceImpl.java

ReportServiceImpl.java
```

---

## Controller

```text
AdminController.java

ReportController.java
```

---

# 6. Database Tables

Administration introduces the following tables already defined in the database:

```text
audit_logs

moderation_actions

report_exports
```

Uses existing tables:

```text
users

categories

communities

community_members

decisions

polls

votes

comments

notifications
```

---

# 7. REST APIs

## Category Management

### Create Category

```http
POST /api/admin/categories
```

---

### Get All Categories

```http
GET /api/admin/categories
```

---

### Get Category By ID

```http
GET /api/admin/categories/{id}
```

---

### Update Category

```http
PUT /api/admin/categories/{id}
```

---

### Activate Category

```http
PUT /api/admin/categories/{id}/activate
```

---

### Deactivate Category

```http
PUT /api/admin/categories/{id}/deactivate
```

---

## User Management

### Get All Users

```http
GET /api/admin/users
```

---

### Get User By ID

```http
GET /api/admin/users/{id}
```

---

### Suspend User

```http
PUT /api/admin/users/{id}/suspend
```

---

### Activate User

```http
PUT /api/admin/users/{id}/activate
```

---

### Delete User

```http
DELETE /api/admin/users/{id}
```

---

## Community Administration

### Get All Communities

```http
GET /api/admin/communities
```

---

### Restore Community

```http
PUT /api/admin/communities/{id}/restore
```

---

## Decision Administration

### Get All Decisions

```http
GET /api/admin/decisions
```

---

### Restore Decision

```http
PUT /api/admin/decisions/{id}/restore
```

---

## Moderation

### Perform Moderation Action

```http
POST /api/admin/moderation-actions
```

---

### View Moderation History

```http
GET /api/admin/moderation-actions
```

---

## Audit Logs

### Get Audit Logs

```http
GET /api/admin/audit-logs
```

---

## Reports

### Generate Report

```http
POST /api/reports
```

---

### Get Report History

```http
GET /api/reports
```

---

### Download Report

```http
GET /api/reports/{id}
```

---

# 8. Business Rules

Only users with

```text
PlatformRole.ADMIN
```

can access administration APIs.

---

Categories are maintained exclusively by administrators.

Community creators can only select existing categories.

---

Every moderation action creates:

- ModerationAction
- AuditLog

---

Every administrative operation must be recorded.

Examples

- Category Created
- User Suspended
- Community Restored
- Decision Restored
- Report Generated

---

Reports are generated using aggregated platform data.

Supported export formats

- PDF
- CSV

Every generated report creates a **ReportExport** record.

---

# 9. Security

Protected using Spring Security and JWT Authentication.

All administration APIs require

- Authentication
- ADMIN Authorization

Role verification should use Spring Security role-based authorization.

---

# 10. Validation

- Administrator must exist
- Category names must be unique
- Report type must be valid
- Moderation requests require a reason
- Target entities must exist

---

# 11. Exception Handling

Handled using `GlobalExceptionHandler`.

Examples

- Unauthorized Access
- User Not Found
- Community Not Found
- Decision Not Found
- Category Already Exists
- Invalid Report Type
- Report Generation Failed
- Moderation Target Not Found

---

# 12. Edge Cases Covered

- Duplicate categories
- Invalid report requests
- Invalid moderation requests
- Unauthorized admin access
- Restoring deleted communities
- Restoring deleted decisions
- Empty audit logs
- Empty reports
- Invalid entity IDs
- Large report generation

---

# 13. Dependencies

Depends on

- Phase 1 – Authentication & User Profile
- Phase 2 – Community Management
- Phase 3 – Decision Management
- Phase 4 – Voting & Polling
- Phase 5 – Discussion & Feedback
- Phase 6 – Notification System
- Phase 7 – Decision Analytics & Dashboard

This is the final backend phase.

---

# 14. Deliverables

- Administration Module
- Admin Dashboard APIs
- Category Management
- User Management
- Community Moderation
- Decision Moderation
- Audit Logs
- Moderation System
- Report Generation APIs
- PDF Export
- CSV Export
- JWT Role-Based Authorization
- Backend Integration Testing
- Performance Optimization
- Backend Documentation
- Production Ready APIs

---

# 15. Final Milestone Deliverables

- Authentication
- User Management
- Community Management
- Decision Management
- Voting & Polling
- Discussion System
- Notification System
- Decision Analytics Dashboard
- Administration Module
- Report Generation
- Audit Logs
- Moderation
- Category Management
- Backend Testing
- API Documentation
- Production Ready Backend

---

# 16. Production Readiness Checklist

- JWT Authentication
- OAuth Login
- Password Reset
- Role-Based Authorization
- Global Exception Handling
- Validation
- Logging
- Audit Logs
- Soft Delete Support
- Optimized Queries
- API Documentation
- Postman Collection
- Environment Variables
- Security Testing
- Integration Testing
- End-to-End Testing

---

# 17. Notes

This phase completes the entire backend implementation of **DecisionHub** according to the project architecture.

After completing this phase, the backend is fully prepared for frontend integration, deployment, demonstration, and production use.
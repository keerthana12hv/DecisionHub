# Phase 6 – Notification System

## Objective

Implement the complete Notification System to keep users informed about important platform activities.

This module enables users to receive in-app notifications for community activities, decision updates, voting events, discussions, and system announcements.

Notifications are generated automatically by backend services whenever important events occur.

This phase establishes communication between all major backend modules.

---

# Modules

## Notification Management

- View Notifications
- View Notification Details
- Mark Notification as Read
- Mark All Notifications as Read

---

## Notification Preferences

Users can manage how they receive notifications.

Supported preferences:

- Email Notifications
- Push Notifications
- In-App Notifications

---

## Notification Types

The notification type determines which module generated the notification.

Supported types:

- COMMUNITY
- DECISION
- VOTE
- COMMENT
- SYSTEM

---

# Enums

## NotificationType

```text
COMMUNITY
DECISION
VOTE
COMMENT
SYSTEM
```

---

# Entities

## Notification

Represents a notification sent to a user.

### Responsibilities

- User
- Notification Type
- Message
- Read Status
- Created Timestamp

---

## NotificationPreference

Stores notification preferences for a user.

### Responsibilities

- User
- Email Notifications Enabled
- Push Notifications Enabled
- In-App Notifications Enabled

---

# Java Files

## Entity

- Notification.java
- NotificationPreference.java

---

## Repository

- NotificationRepository.java
- NotificationPreferenceRepository.java

---

## DTO

### Request DTOs

- UpdateNotificationPreferenceRequest.java

### Response DTOs

- NotificationResponse.java
- NotificationPreferenceResponse.java

---

## Mapper

- NotificationMapper.java

---

## Service

- NotificationService.java

---

## Service Implementation

- NotificationServiceImpl.java

---

## Controller

- NotificationController.java

---

# Database Tables

- notifications
- notification_preferences

---

# REST APIs

## Get My Notifications

**GET**

```http
/api/notifications
```

Authentication

Required

Purpose

Returns all notifications belonging to the logged-in user.

---

## Get Notification By ID

**GET**

```http
/api/notifications/{id}
```

Authentication

Required

Purpose

Returns details of a single notification.

Users can only access their own notifications.

---

## Mark Notification as Read

**PUT**

```http
/api/notifications/{id}/read
```

Authentication

Required

Purpose

Marks a notification as read.

---

## Mark All Notifications as Read

**PUT**

```http
/api/notifications/read-all
```

Authentication

Required

Purpose

Marks every unread notification as read.

---

## Get Notification Preferences

**GET**

```http
/api/notifications/preferences
```

Authentication

Required

Purpose

Returns notification preferences for the logged-in user.

---

## Update Notification Preferences

**PUT**

```http
/api/notifications/preferences
```

Authentication

Required

Purpose

Updates notification preferences.

---

# Notification Generation

Notifications are **never created directly through controller APIs**.

Instead, backend services generate notifications automatically.

Example flow

```text
CommunityService
        │
        ▼
NotificationService
        │
        ▼
Notification Saved
        │
        ▼
Displayed to User
```

The following services may generate notifications:

- CommunityService
- DecisionService
- VoteService
- CommentService
- Authentication Service

---

# Notification Events

Examples include:

## Community Events

- Community Created
- Community Updated
- User Joined Community
- User Left Community

---

## Decision Events

- Decision Created
- Decision Updated
- Decision Closed

---

## Voting Events

- Vote Submitted
- Poll Closed

---

## Discussion Events

- New Comment
- Reply to Comment

---

## System Events

- Welcome Notification
- Password Changed
- Account Status Updated

---

# Business Rules

## Notification Ownership

Every notification belongs to exactly one user.

Users cannot access notifications belonging to other users.

---

## Read Status

Every new notification is created with

```text
isRead = false
```

After the user opens or marks the notification,

```text
isRead = true
```

---

## Notification Preferences

Before generating a notification, the system checks the user's preferences.

Example

If

```text
emailEnabled = false
```

↓

No email notification is sent.

The same applies for:

- pushEnabled
- inAppEnabled

---

## Automatic Notification Generation

Notification creation should only occur through backend services.

Example

```text
Decision Created
        │
        ▼
DecisionService
        │
        ▼
NotificationService
        │
        ▼
Notification Saved
```

Controllers should never create notifications directly.

---

# Security

Protected using JWT Authentication.

Current user is retrieved through

```text
SecurityContextHolder
```

Only authenticated users can

- View their notifications
- Mark notifications as read
- Update notification preferences

Users cannot access notifications belonging to another user.

---

# Validation

## Notification

- User Required
- Notification Type Required
- Message Required

---

## Notification Preferences

- User Required
- Preference Record Must Exist

---

# Exception Handling

Handled using GlobalExceptionHandler.

Examples

- Notification Not Found
- Notification Preference Not Found
- Unauthorized Notification Access
- Invalid Notification Type
- User Not Found

---

# Edge Cases Covered

✓ Invalid Notification ID

✓ Access Another User's Notification

✓ Empty Notification List

✓ Already Read Notification

✓ Invalid User

✓ Notification Preference Not Found

✓ Notification Preferences Disabled

✓ Unauthorized Access

---

# Dependencies

Depends on

- Phase 1 – Authentication & User Profile
- Phase 2 – Community Management
- Phase 3 – Decision Management
- Phase 4 – Voting & Polling
- Phase 5 – Discussion & Feedback

Provides the foundation for

- Analytics
- Reports
- Administration

---

# Deliverables

- Notification APIs
- Notification Preference APIs
- Read / Unread Management
- Automatic Notification Generation
- JWT Protected APIs
- Global Exception Handling
- Notification Testing using Postman
- Backend Ready for Analytics Module

---

# Notes for Future Phases

## Phase 7 – Analytics

Analytics will include

- Total Notifications
- Read Notifications
- Unread Notifications
- Notification Read Rate
- User Engagement

---

## Phase 8 – Administration

Administrators will be able to

- Broadcast System Notifications
- Send Platform Announcements
- View Notification Statistics
- Manage Notification Templates
- Audit Notification Delivery

This phase establishes the Notification System while remaining fully aligned with the current database schema.
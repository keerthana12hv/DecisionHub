# Phase 5 – Discussion & Feedback System

## Objective

Implement the complete Discussion & Feedback module to enable collaborative communication around decisions.

This module allows authenticated community members to discuss decisions, share suggestions, provide opinions, and collaborate before making a final decision.

The implementation supports threaded conversations using parent-child comments while maintaining soft delete functionality.

This phase establishes the communication layer of the DecisionHub platform and provides the foundation for Notifications, Analytics, Reports, and Administration.

---

# Modules

## Discussion Management

- Create Comment
- View Comments
- View Comment Details
- Update Comment
- Delete Comment (Soft Delete)

---

## Threaded Discussions

- Parent Comments
- Reply to Comment
- Nested Discussions

---

## Community Feedback

Discussion comments are used to provide:

- Suggestions
- Advice
- Opinions
- Community Feedback

No separate Feedback entity is required.

---

# Soft Delete

Soft Delete is implemented for comments.

Comments are never physically removed from the database.

Instead,

```
deleted_at
```

is populated.

Deleted comments are excluded from API responses.

---

# Entities

## Comment

Represents a discussion comment created by a user for a decision.

### Responsibilities

- Decision
- User
- Parent Comment (optional)
- Comment Content
- Created Time
- Updated Time
- Soft Delete

---

# Java Files

## Entity

- Comment.java

---

## Repository

- CommentRepository.java

---

## DTO

### Request DTOs

- CreateCommentRequest.java
- UpdateCommentRequest.java

### Response DTOs

- CommentResponse.java

---

## Mapper

- CommentMapper.java

---

## Service

- CommentService.java

---

## Service Implementation

- CommentServiceImpl.java

---

## Controller

- CommentController.java

---

# Database Tables

- comments

---

# REST APIs

## Create Comment

**POST**

```http
/api/comments
```

Authentication

Required

Purpose

Create a new comment for a decision.

Supports:

- Top-level comments
- Replies

---

## Get Comments By Decision

**GET**

```http
/api/decisions/{decisionId}/comments
```

Authentication

Required

Purpose

Returns all active comments belonging to a decision.

Soft deleted comments are excluded.

---

## Get Comment By ID

**GET**

```http
/api/comments/{id}
```

Authentication

Required

Purpose

Returns a single active comment.

---

## Update Comment

**PUT**

```http
/api/comments/{id}
```

Authentication

Required

Purpose

Update comment content.

Only the comment owner can update.

---

## Delete Comment

**DELETE**

```http
/api/comments/{id}
```

Authentication

Required

Purpose

Soft delete a comment.

Only the comment owner can delete.

---

## Reply to Comment

**POST**

```http
/api/comments/{id}/reply
```

Authentication

Required

Purpose

Create a reply to an existing comment.

Replies are linked using

```
parent_comment_id
```

---

# Business Rules

## Comment Creation

Every comment must belong to:

- One Decision
- One User

Parent Comment is optional.

If

```
parent_comment_id = NULL
```

the comment becomes a top-level discussion.

Otherwise,

it becomes a reply.

---

## Community Membership

Only authenticated members of the community may create comments.

Users outside the community cannot participate in discussions.

---

## Threaded Discussion

Replies reference another comment.

Example

```
Comment
 ├── Reply
 │     ├── Reply
 │     └── Reply
 └── Reply
```

Thread depth is not restricted.

---

## Comment Update Rules

Only the comment owner can update.

Future administrator and moderator permissions will be introduced in Phase 8.

---

## Comment Delete Rules

Only the comment owner can delete.

Deletion is implemented using Soft Delete.

The database record remains intact.

---

## Decision Validation

Comments may only be created for existing active decisions.

Comments cannot be added to soft deleted decisions.

---

## Parent Comment Validation

Reply comments require an existing parent comment.

Replies cannot be created for deleted comments.

---

# Security

Protected using JWT Authentication.

Current authenticated user is retrieved through

```
SecurityContextHolder
```

Only authenticated users can

- Create comments
- Reply
- Update their own comments
- Delete their own comments

---

# Validation

## Comment

- Decision Required
- Comment Content Required
- Comment Length Validation

---

## Reply

- Parent Comment must exist
- Parent Comment must be active

---

## User

- User must exist
- User must be authenticated
- User must belong to the community

---

# Exception Handling

Handled through GlobalExceptionHandler.

Examples

- Decision Not Found
- Comment Not Found
- Parent Comment Not Found
- Community Membership Required
- User Not Authorized
- Empty Comment
- Deleted Comment
- Cannot Reply To Deleted Comment

---

# Edge Cases Covered

✓ Invalid Decision ID

✓ Invalid Comment ID

✓ Invalid Parent Comment

✓ Empty Comment

✓ Comment Length Validation

✓ Reply to Deleted Comment

✓ Comment on Deleted Decision

✓ Unauthorized Comment Update

✓ Unauthorized Comment Delete

✓ Nested Replies

✓ Soft Deleted Comments Hidden

✓ Community Membership Validation

✓ Thread Integrity

---

# Dependencies

Depends on

- Phase 1 – Authentication & User Profile
- Phase 2 – Community Management
- Phase 3 – Decision Management

Provides foundation for

- Notifications
- Analytics
- Reports
- Administration

---

# Deliverables

- Comment CRUD APIs
- Reply APIs
- Threaded Discussions
- Community Feedback
- Soft Delete Support
- JWT Protected APIs
- Ownership Validation
- Decision Validation
- Parent Comment Validation
- Global Exception Handling
- Postman API Testing
- Backend Ready for Notification Module

---

# Notes for Future Phases

## Phase 6 – Notification System

Notifications will be generated for

- New Comments
- Replies
- Community Discussions

---

## Phase 7 – Analytics

Discussion analytics will include

- Total Comments
- Active Discussions
- Community Engagement

---

## Phase 8 – Administration

Administrators and Moderators will be able to

- Moderate Discussions
- Delete Inappropriate Comments
- Review Reported Comments
- Audit Discussion Activities

This phase provides the complete Discussion System for DecisionHub while keeping the implementation aligned with the current database schema.
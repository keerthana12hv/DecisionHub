# Phase 4 – Voting & Polling System

## Objective

Implement the complete Voting & Polling module that enables authenticated users to participate in decision making through secure polling.

This phase introduces Poll Management and Voting functionality built on top of the Decision Management module implemented in Phase 3.

Users should be able to:

- Create Polls
- View Polls
- Update Polls
- Close Polls
- Cast Votes
- View Poll Results
- View Voting Statistics

> **Important**
>
> Polls do **not** have their own visibility.
>
> Poll visibility always follows the visibility of the associated Decision.
>
> Polls are **not** soft deleted in this phase.

---

# Modules

## Poll Management

- Create Poll
- View Poll
- View Poll Details
- Update Poll
- Close Poll

---

## Voting System

- Cast Vote
- Validate Vote
- View Poll Results
- View Voting Statistics

---

# Enums

## PollStatus

- OPEN
- CLOSED

---

## PollType

- SINGLE_CHOICE
- MULTIPLE_CHOICE

---

# Entities

## Poll

Represents a poll attached to a decision.

### Responsibilities

- Question
- Decision
- Created By
- Poll Type
- Poll Status
- End Time
- Created At

---

## Vote

Represents a user's vote.

### Responsibilities

- User
- Poll
- Decision Option
- Voted At

---

# Java Files

## Entity

- Poll.java
- Vote.java

---

## Repository

- PollRepository.java
- VoteRepository.java

---

## DTO

### Request DTOs

- CreatePollRequest.java
- UpdatePollRequest.java
- VoteRequest.java

### Response DTOs

- PollResponse.java
- VoteResponse.java
- PollResultResponse.java

---

## Mapper

- PollMapper.java
- VoteMapper.java

---

## Service

- PollService.java
- VoteService.java

---

## Service Implementation

- PollServiceImpl.java
- VoteServiceImpl.java

---

## Controller

- PollController.java
- VoteController.java

---

# Database Tables

- polls
- votes

---

# REST APIs

## Poll APIs

### Create Poll

```http
POST /api/polls
```

Authentication Required

Creates a new poll for an existing decision.

---

### Get All Polls

```http
GET /api/polls
```

Returns all polls.

---

### Get Poll By ID

```http
GET /api/polls/{id}
```

Returns poll details.

---

### Update Poll

```http
PUT /api/polls/{id}
```

Only the poll creator may update the poll.

---

### Close Poll

```http
POST /api/polls/{id}/close
```

Changes Poll Status from

```
OPEN
```

to

```
CLOSED
```

No further voting is allowed.

---

## Vote APIs

### Cast Vote

```http
POST /api/polls/{id}/vote
```

Creates a new vote.

---

### View Poll Results

```http
GET /api/polls/{id}/results
```

Returns

- Total Votes
- Vote Count Per Option
- Vote Percentage
- Winning Option

---

### View Poll Statistics

```http
GET /api/polls/{id}/statistics
```

Returns

- Total Votes
- Participation Statistics
- Vote Distribution

---

# Business Rules

## Poll Creation

- Poll belongs to one Decision.
- Only authenticated users can create polls.
- Only the Decision Creator may create a poll.
- Poll Status is initially

```
OPEN
```

---

## Poll Visibility

Poll visibility is inherited from the associated Decision.

- PUBLIC Decision → Public Poll
- PRIVATE Decision → Private Poll

Poll does not maintain a separate visibility field.

---

## Voting Rules

Only authenticated users may vote.

Poll must be

```
OPEN
```

Community membership should be verified before allowing a vote.

---

## Single Choice Poll

User may vote for only one option.

---

## Multiple Choice Poll

User may vote for multiple options.

---

## Duplicate Vote Prevention

The database enforces the following unique constraint:

```
(poll_id, user_id, option_id)
```

This ensures that a user cannot vote multiple times for the same option within the same poll.

---

## Poll Closing

Once Poll Status becomes

```
CLOSED
```

- No additional votes are accepted.
- Existing votes remain available for result calculation.

---

# Validation

## Poll

- Question Required
- Decision Required
- Poll Type Required
- End Time Required

---

## Vote

- Poll Exists
- Decision Option Exists
- Poll Status must be OPEN
- User must be authenticated
- Decision Option must belong to the associated Decision

---

# Security

Protected using JWT Authentication.

Only authenticated users may

- Create Polls
- Vote

Only the Poll Creator may

- Update Poll
- Close Poll

Current logged-in user should be obtained through

```java
SecurityContextHolder
```

---

# Result Calculation

Backend APIs should provide aggregated voting data.

Includes

- Total Votes
- Votes Per Option
- Vote Percentage
- Winning Option

These APIs will later support frontend charts such as:

- Pie Charts
- Bar Charts
- Progress Indicators

---

# Exception Handling

Handled using GlobalExceptionHandler.

Examples

- Poll Not Found
- Vote Not Found
- Decision Not Found
- Decision Option Not Found
- Poll Closed
- Duplicate Vote
- Unauthorized Poll Update
- Unauthorized Poll Close
- Invalid Decision Option

---

# Edge Cases Covered

✓ Invalid Poll ID

✓ Invalid Decision ID

✓ Invalid Decision Option

✓ Poll Closed

✓ Duplicate Vote

✓ Vote for Option Not Belonging to Decision

✓ Unauthorized Poll Update

✓ Unauthorized Poll Close

✓ Voting Without Authentication

✓ Empty Poll Results

✓ Decision Visibility Enforcement

✓ Multiple Choice Validation

---

# Dependencies

Depends on

- Phase 1 – Authentication & User Profile Management
- Phase 2 – Community Management
- Phase 3 – Decision Management

Provides the foundation for

- Discussion System
- Notifications
- Analytics
- Reports
- Administration

---

# Deliverables

- Poll CRUD APIs
- Vote APIs
- Single Choice Polls
- Multiple Choice Polls
- Poll Closing
- Vote Validation
- Poll Results APIs
- Vote Statistics APIs
- JWT Protected APIs
- Global Exception Handling
- Postman API Testing
- Backend Ready for Discussion Module

---

# Notes for Future Phases

## Phase 5 – Discussion

Users will discuss poll results through comments attached to Decisions and Decision Options.

---

## Phase 7 – Analytics

Voting analytics will include

- Vote Distribution
- Winning Option
- Participation Statistics
- Poll Completion Rate
- Option Popularity

---

## Phase 8 – Administration

Administrators and Moderators will be able to

- Moderate Polls
- Remove Invalid Votes
- Audit Voting Activity

The Voting module does **not** implement moderation logic.
It only provides the voting data required by the Administration module.
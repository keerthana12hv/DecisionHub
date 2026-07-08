# Phase 3 – Decision Management & Option Comparison

## Objective

Implement the complete Decision Management module that enables users to create structured decision boards, compare multiple options, evaluate alternatives, and collaboratively make informed decisions.

This phase forms the core of the DecisionHub platform by implementing Decision Board Management and the Option Comparison System.

> **Important**
>
> Decisions use **Soft Delete**, similar to Communities.
>
> Decision records are **never physically removed** from the database.
>
> Deleting a decision only populates the `deleted_at` column.
>
> Active decisions are always retrieved using:
>
> ```java
> findByDeletedAtIsNull()
> ```
>
> This ensures deleted decisions remain available for auditing while being hidden from normal users.

---

# Modules

## Decision Management

- Create Decision
- View Decisions
- View Decision Details
- Update Decision
- Delete Decision (Soft Delete)
- Public Decisions
- Private Decisions

---

## Decision Options

Each decision contains multiple decision options.

Examples

- MBA
- Job
- iPhone
- Samsung
- Goa
- Bali

Users can

- Add Options
- Update Options
- Delete Options

---

## Option Comparison

Users compare options using comparison factors.

Features

- Comparison Factors
- Factor Weight
- Score Allocation
- Pros
- Cons
- Overall Ranking

---

## Comparison Templates

Reusable templates containing predefined comparison factors.

Examples

- Career Template
- Finance Template
- Travel Template

---

# Enums

## DecisionStatus

- DRAFT
- ACTIVE
- CLOSED
- ARCHIVED

> **Note**
>
> Ensure the enum and roadmap use the same values throughout the project.
> If the project currently uses `OPEN`, either rename it to `ACTIVE` or update the roadmap accordingly.

---

## DecisionVisibility

- PUBLIC
- PRIVATE

---

## ProsConsType

- PRO
- CON

---

# Entities

## Decision

Represents a Decision Board.

### Responsibilities

- Title
- Description
- Creator
- Community
- Visibility
- Status
- Deadline
- Created At
- Updated At
- Soft Delete

---

## DecisionOption

Represents one option inside a decision.

### Responsibilities

- Option Name
- Description
- Decision

---

## ComparisonFactor

Represents comparison criteria.

### Responsibilities

- Name
- Description
- Weight
- Decision

---

## ComparisonFactorTemplate

Stores reusable comparison templates.

### Responsibilities

- Template Name
- Factor Name
- Description
- Default Weight

---

## OptionFactorScore

Stores the score assigned to an option for a comparison factor.

### Responsibilities

- Decision Option
- Comparison Factor
- Score

One option should contain only one score for a particular comparison factor.

---

## ProsCons

Stores advantages and disadvantages of an option.

### Responsibilities

- Type
- Content
- Decision Option
- User
- Created At

---

# Java Files

## Entity

- Decision.java
- DecisionOption.java
- ComparisonFactor.java
- ComparisonFactorTemplate.java
- OptionFactorScore.java
- ProsCons.java

---

## Repository

- DecisionRepository.java
- DecisionOptionRepository.java
- ComparisonFactorRepository.java
- ComparisonFactorTemplateRepository.java
- OptionFactorScoreRepository.java
- ProsConsRepository.java

---

## DTO

### Request DTOs

- CreateDecisionRequest.java
- UpdateDecisionRequest.java
- CreateDecisionOptionRequest.java
- UpdateDecisionOptionRequest.java
- ComparisonFactorRequest.java
- ScoreRequest.java
- ProsConsRequest.java

### Response DTOs

- DecisionResponse.java
- DecisionDetailResponse.java
- DecisionOptionResponse.java
- ComparisonResponse.java
- RankingResponse.java

---

## Mapper

- DecisionMapper.java

---

## Service

- DecisionService.java

---

## Service Implementation

- DecisionServiceImpl.java

---

## Controller

- DecisionController.java

---

# Database Tables

- decisions
- decision_options
- comparison_factors
- comparison_factor_templates
- option_factor_scores
- pros_cons

---

# REST APIs

## Decision APIs

### Create Decision

```http
POST /api/decisions
```

Authentication Required

---

### Get All Decisions

```http
GET /api/decisions
```

Returns all active decisions.

---

### Get Decision By ID

```http
GET /api/decisions/{id}
```

---

### Update Decision

```http
PUT /api/decisions/{id}
```

Only the decision creator may update.

---

### Delete Decision

```http
DELETE /api/decisions/{id}
```

Soft deletes the decision.

---

## Decision Option APIs

### Add Option

```http
POST /api/decisions/{id}/options
```

---

### Update Option

```http
PUT /api/options/{id}
```

---

### Delete Option

```http
DELETE /api/options/{id}
```

---

## Comparison Factor APIs

### Add Comparison Factor

```http
POST /api/decisions/{id}/comparison-factors
```

---

### Update Comparison Factor

```http
PUT /api/comparison-factors/{id}
```

---

### Delete Comparison Factor

```http
DELETE /api/comparison-factors/{id}
```

---

## Score APIs

### Add Score

```http
POST /api/options/{optionId}/scores
```

---

### Update Score

```http
PUT /api/scores/{id}
```

---

## Pros & Cons APIs

### Add Pro

```http
POST /api/options/{id}/pros
```

---

### Add Con

```http
POST /api/options/{id}/cons
```

---

### Delete Pro/Con

```http
DELETE /api/pros-cons/{id}
```

---

## Ranking API

```http
GET /api/decisions/{id}/ranking
```

Returns ranked decision options.

---

# Business Rules

## Decision Creation

- Only authenticated users can create decisions.
- Every decision belongs to a community.
- Creator becomes the decision owner.

---

## Decision Update

Only the creator may update the decision.

Checks

- Community exists
- Decision exists
- Decision is not soft deleted

---

## Decision Delete

Only the creator may delete.

Soft delete only.

Updates

- `deleted_at`

No database record is permanently removed.

---

## Decision Options

- Every decision should contain at least two options.
- Option names should be unique within the same decision.

---

## Comparison Factors

- Users may create custom comparison factors.
- Duplicate comparison factors are not allowed within the same decision.

---

## Score Allocation

- One score per option per comparison factor.
- Score must be within the allowed range.

---

## Pros & Cons

Each option may contain multiple

- Pros
- Cons

---

# Validation

## Decision

- Title Required
- Description Required
- Community Required
- Visibility Required
- Status Required

---

## Decision Option

- Option Name Required
- Decision Required

---

## Comparison Factor

- Name Required
- Weight Required

---

## Score

- Score Required
- Valid Score Range

---

# Security

Protected using JWT Authentication.

Current logged-in user should be retrieved using

```java
SecurityContextHolder
```

Only the creator may

- Update Decision
- Delete Decision

---

# Soft Delete

Decision records are never permanently removed.

Instead

```text
deleted_at
```

is populated.

Repositories should expose

```java
findByDeletedAtIsNull()
```

to return only active decisions.

---

# Exception Handling

Handled through GlobalExceptionHandler.

Examples

- Decision Not Found
- Community Not Found
- Decision Already Deleted
- Comparison Factor Not Found
- Duplicate Comparison Factor
- Duplicate Option
- Invalid Score
- Unauthorized Update
- Unauthorized Delete

---

# Edge Cases Covered

✓ Invalid community

✓ Invalid decision ID

✓ Duplicate option names

✓ Duplicate comparison factors

✓ Duplicate score entries

✓ Invalid score

✓ Less than two options

✓ Soft deleted decision cannot be viewed

✓ Soft deleted decision cannot be updated

✓ Soft deleted decision cannot receive votes

✓ Soft deleted decision cannot receive comments

✓ Unauthorized decision update

✓ Unauthorized decision delete

✓ Empty ranking

---

# Dependencies

Depends on

- Phase 1 Authentication
- Phase 2 Community Management

Provides foundation for

- Voting
- Discussion
- Notifications
- Reports
- Analytics
- Administration

---

# Deliverables

- Decision CRUD APIs
- Decision Option Management
- Comparison Factors
- Comparison Templates
- Score Allocation
- Pros & Cons Management
- Ranking Engine
- JWT Protected APIs
- Soft Delete Support
- Global Exception Handling
- Postman API Testing
- Backend Ready for Voting Module

---

# Notes for Future Phases

## Phase 4 – Voting

Voting is allowed only on **ACTIVE** decisions.

Closed decisions become read-only.

---

## Phase 5 – Discussion

Comments and discussions will be linked to decision options.

---

## Phase 7 – Analytics

Decision analytics will include

- Winning Option
- Highest Score
- Participation
- Ranking Statistics

---

## Phase 8 – Administration

Administrators will be able to

- Moderate Decisions
- Restore Soft Deleted Decisions
- Audit Decision Changes
- Manage Comparison Templates
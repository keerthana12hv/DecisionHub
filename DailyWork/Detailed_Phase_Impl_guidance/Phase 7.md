# Phase 7 – Decision Analytics & Dashboard

## Objective

Implement the **Decision Analytics & Dashboard** module to provide meaningful insights into platform activity by aggregating data from existing modules.

This phase exposes dashboard APIs that allow the frontend to display statistics, charts, and trends related to users, communities, decisions, voting, discussions, and overall platform usage.

No new database tables are introduced in this phase. Analytics is generated using optimized aggregation queries on existing data.

---

# Modules

## User Dashboard

- Profile Summary
- Active Decisions
- Decision History
- Voting History
- Community Membership
- Community Activity
- Recent Notifications
- Recent Discussions

---

## Decision Analytics

- Decision Outcomes
- Decision Trends
- Decision Status Distribution
- Option Popularity
- Winning Option Analysis

---

## Voting Analytics

- Vote Distribution
- Voting Participation
- Poll Completion Rate
- Most Popular Options

---

## Community Analytics

- Community Growth
- Community Participation
- Most Active Communities
- Popular Categories

---

## Platform Analytics

- Total Users
- Active Users
- Total Communities
- Total Decisions
- Total Polls
- Total Votes
- Total Comments
- Platform Growth

---

## Dashboard Visualization

Backend provides aggregated data for frontend charts and dashboard cards.

Examples:

- Summary Cards
- Bar Charts
- Pie Charts
- Line Charts
- Trend Graphs

---

# Enums

No new enums are introduced in this phase.

---

# Entities

No new entities are introduced.

Analytics aggregates information from existing entities using optimized database queries.

### Data Sources

- User
- Community
- CommunityMember
- Decision
- DecisionOption
- ComparisonFactor
- OptionFactorScore
- Poll
- PollOption
- Vote
- Comment
- Notification

---

# Java Files

## DTO

### Request DTOs

- DashboardFilterRequest.java
- AnalyticsFilterRequest.java

### Response DTOs

- UserDashboardResponse.java
- DecisionAnalyticsResponse.java
- VotingAnalyticsResponse.java
- CommunityAnalyticsResponse.java
- PlatformAnalyticsResponse.java

---

## Mapper

No mapper is required.

Analytics responses can be constructed directly inside the service layer since no entity-to-DTO conversion is involved.

---

## Service

- AnalyticsService.java

---

## Service Implementation

- AnalyticsServiceImpl.java

---

## Controller

- AnalyticsController.java

---

# Database Tables

No new database tables are introduced.

Analytics uses aggregation queries on the following existing tables:

- users
- communities
- community_members
- decisions
- decision_options
- comparison_factors
- option_factor_scores
- polls
- poll_options
- votes
- comments
- notifications

---

# REST APIs

## User Dashboard

### Get Dashboard

**GET**

```http
/api/dashboard
```

Authentication

Required

Purpose

Returns dashboard information for the authenticated user.

Includes

- Profile Summary
- Active Decisions
- Decision History
- Voting History
- Community Activity
- Recent Notifications
- Recent Discussions

---

## Decision Analytics

### Get Decision Analytics

**GET**

```http
/api/analytics/decisions
```

Authentication

Required

Purpose

Returns

- Decision Outcomes
- Decision Trends
- Active Decisions
- Closed Decisions
- Winning Options
- Option Popularity

---

## Voting Analytics

### Get Voting Analytics

**GET**

```http
/api/analytics/voting
```

Authentication

Required

Purpose

Returns

- Vote Distribution
- Voting Participation
- Poll Completion Rate
- Most Popular Options

---

## Community Analytics

### Get Community Analytics

**GET**

```http
/api/analytics/communities
```

Authentication

Required

Purpose

Returns

- Community Growth
- Community Participation
- Most Active Communities
- Popular Categories

---

## Platform Analytics

### Get Platform Analytics

**GET**

```http
/api/admin/analytics
```

Authentication

Required

Authorization

ADMIN

Purpose

Returns

- Total Users
- Active Users
- Total Communities
- Total Decisions
- Total Polls
- Total Votes
- Total Comments
- Platform Growth

---

# Dashboard Components

## User Dashboard

Displays

- Active Decisions
- Voting History
- Decision Outcomes
- Community Activity
- Recent Discussions
- Recent Notifications
- Community Membership

---

## Analytics Dashboard

Displays

- Decision Trends
- Vote Distribution
- Community Growth
- Popular Categories
- Participation Statistics
- Platform Summary

---

# Charts & Visualization

The backend returns aggregated values for frontend visualization.

Supported chart types include

- Pie Charts
- Bar Charts
- Line Charts

Visualization Data

- Vote Distribution
- Decision Trends
- Community Growth
- Category Popularity
- Participation Statistics
- Option Popularity

---

# Business Rules

## User Dashboard

Each user can only access their own dashboard.

---

## Platform Dashboard

Only users with the ADMIN role can access platform-wide analytics.

---

## Analytics Data

Analytics APIs must return aggregated information rather than raw database records.

Statistics should always be calculated from live database data and should never duplicate stored values.

---

## Performance

Analytics APIs should use optimized SQL/JPA aggregation queries and avoid unnecessary entity loading.

---

# Security

Protected using JWT Authentication.

User Dashboard APIs

Accessible only by the authenticated user.

Platform Analytics APIs

Accessible only by users with the ADMIN role.

---

# Validation

- User must exist.
- Dashboard filters must be valid.
- Date ranges must be valid.
- Analytics filters must be supported.

---

# Exception Handling

Handled using GlobalExceptionHandler.

Possible exceptions include

- User Not Found
- Analytics Data Not Available
- Invalid Dashboard Filter
- Invalid Date Range
- Unauthorized Analytics Access

---

# Edge Cases Covered

- User with no communities
- User with no decisions
- User with no votes
- User with no comments
- Empty dashboard
- Empty analytics results
- Invalid filters
- Future date ranges
- Large datasets
- Unauthorized analytics access

---

# Dependencies

Depends on

- Phase 1 – Authentication & User Profile
- Phase 2 – Community Management
- Phase 3 – Decision Management
- Phase 4 – Voting & Polling
- Phase 5 – Discussion & Feedback
- Phase 6 – Notification System

Provides the foundation for

- Phase 8 – Administration
- Report Generation
- Platform Monitoring

---

# Deliverables

- User Dashboard APIs
- Decision Analytics APIs
- Voting Analytics APIs
- Community Analytics APIs
- Platform Analytics APIs
- Dashboard Summary APIs
- Chart Data APIs
- JWT Protected APIs
- Optimized Aggregation Queries
- Global Exception Handling
- Analytics Testing using Postman
- Backend Ready for Administration Module

---

# Notes for Future Phases

## Phase 8 – Administration, Reports & Final Backend Integration

The Administration module will use analytics to:

- Monitor platform growth
- Identify inactive communities
- Review user engagement
- Generate PDF and CSV reports
- Track moderation statistics
- Monitor overall system performance

The `ReportExport` entity introduced in the database will be utilized during Phase 8 for exporting generated reports.
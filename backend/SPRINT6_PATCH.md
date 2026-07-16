# Sprint 6 Patch Reconciliation Documentation

This document records the alignment of the DecisionHub backend Sprint 6 (Decision CRUD) implementation with the mentor's functional expectations before starting Sprint 7 (Voting Module).

## 1. Already Complete (No Action Needed)

* **Decision CRUD**: Comprehensive CRUD operations exist for Decisions (`DecisionController`).
* **Standalone Decision Creation**: Decisions can be created without a community affiliation (`communityId == null`).
* **Community Decision Creation**: Decisions can be successfully created inside and linked to an existing community.
* **Decision-First Workflow**: Supported sequence of `Decision Creation -> Add Options -> Add Comparison Factors -> Users Submit Scores -> Generate Ranking`.
* **Community Integration**: Creation requests successfully associate with communities and validate community membership prior to creation.
* **Java Record DTOs**: DTOs for the decision module are immutable Java records.
* **CORS Configuration**: Fully configured inside `SecurityConfig.java` to support frontend cross-origin requests.
* **Authentication Integration**: Authenticated users are mapped via Spring Security and JWT.
* **Community Creation Authorization**: Any authenticated user can create a community. The creator is automatically assigned the `OWNER` role, acting as the community moderator.

## 2. Patches Applied

### PATCH 2: Decision Visibility Enum
* Added the `COMMUNITY` level to the `DecisionVisibility` enum to properly support:
  * `PUBLIC`
  * `COMMUNITY`
  * `PRIVATE`

### PATCH 3 & 4: Standalone & Community Decision Visibility + Category Inference
* **Automatic Visibility Inference**:
  * If `communityId == null`, visibility is automatically set to `PUBLIC`.
  * If `communityId != null`, visibility is automatically set to `COMMUNITY`.
* **Category Inference**: Category selection is removed from Decision Creation. Instead, when a decision is associated with a community, it automatically inherits the category of that community.
* **Backward Compatibility**: Removed `categoryId` from `DecisionRequest` primary constructor but implemented a `@Deprecated` overloaded constructor that takes 11 arguments, preventing compile-time issues with existing tests or frontend payloads.
* **MapStruct Automatic Resolution**: Updated `DecisionMapper` to map `categoryName` directly from `community.category.name`.

### PATCH 5, 6 & 7: Authorization Rules & Future Placeholders
* **View Restrictions**:
  * `PUBLIC` decisions: Accessible to all authenticated users.
  * `COMMUNITY` decisions: Accessible only to members of the corresponding community.
  * `PRIVATE` decisions: Accessible only to the creator/owner.
* **Sprint 7 / Future TODO Markers**:
  * Added TODO comments in `DecisionAuthorizationServiceImpl` for voting and commenting authorization rules on `PUBLIC`, `COMMUNITY`, and `PRIVATE` decisions.
  * Added a TODO comment placeholder for future private decision access control list / invitation validation (using a `DecisionInvitation` entity).

### PATCH 8: Anonymous Decision Placeholders
* Added TODO markers in `ComparisonScoreServiceImpl.getScoresByDecisionId` outlining the future business rules for anonymous participation:
  * **owner-only visibility**: only decision creator can view individual scores.
  * **anonymous visibility**: score submitters are anonymized (user details stripped).
  * **admin visibility**: global administrators can view all details.

### PATCH 9: API Path Consistency Choice
* To prevent breaking other teammate branches or existing frontend APIs, we have preserved the `/decisions/{decisionId}/...` path mappings on option, factor, score, and ranking controllers. This minimizes merge conflicts and maintains strict backward compatibility.

## 3. Future Sprint 7 (Voting Module) & Beyond
* **Voting Engine**: To be implemented during Sprint 7.
* **Invitation System & Private ACL**: To be implemented when private sharing features are scheduled.
* **Anonymity Processing**: To be implemented when anonymous participation behavior is fully detailed.

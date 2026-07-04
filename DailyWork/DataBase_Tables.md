# DecisionHub Database Specification (Part 1)

> This document is the official database specification for the first 10
> tables of DecisionHub. AI assistants and backend developers should
> follow the entity names, table names and column names exactly.

------------------------------------------------------------------------

# 1. User

**Entity:** `User`\
**Package:** `com.decisionhub.entity.authentication`\
**Table:** `users`

  Column          Type           Constraints
  --------------- -------------- ------------------
  id              BIGINT         PK
  username        VARCHAR(50)    UNIQUE, NOT NULL
  email           VARCHAR(100)   UNIQUE, NOT NULL
  password_hash   VARCHAR        NOT NULL
  role            PlatformRole   NOT NULL
  status          UserStatus     NOT NULL
  created_at      TIMESTAMP      NOT NULL
  updated_at      TIMESTAMP      NOT NULL
  deleted_at      TIMESTAMP      NULL

**Relationships** - One User → Many OAuthAccount - One User → Many
PasswordResetToken - One User → Many CommunityMember - One User → Many
Community (owner) - One User → Many Decision (creator)

------------------------------------------------------------------------

# 2. OAuthAccount

**Entity:** `OAuthAccount`\
**Package:** `com.decisionhub.entity.authentication`\
**Table:** `oauth_accounts`

  Column             Type          Constraints
  ------------------ ------------- ---------------
  id                 BIGINT        PK
  provider           VARCHAR(50)   NOT NULL
  provider_user_id   VARCHAR       UNIQUE
  user_id            BIGINT        FK → users.id

------------------------------------------------------------------------

# 3. PasswordResetToken

**Entity:** `PasswordResetToken`\
**Package:** `com.decisionhub.entity.authentication`\
**Table:** `password_reset_tokens`

  Column       Type        Constraints
  ------------ ----------- ---------------
  id           BIGINT      PK
  token_hash   VARCHAR     UNIQUE
  expires_at   TIMESTAMP   NOT NULL
  user_id      BIGINT      FK → users.id

------------------------------------------------------------------------

# 4. Category

**Entity:** `Category`\
**Package:** `com.decisionhub.entity.community`\
**Table:** `categories`

  Column      Type           Constraints
  ----------- -------------- -------------
  id          BIGINT         PK
  name        VARCHAR(100)   UNIQUE
  slug        VARCHAR(100)   UNIQUE
  is_active   BOOLEAN        NOT NULL

**Relationships** - One Category → Many Community - One Category → Many
UserInterest

------------------------------------------------------------------------

# 5. Community

**Entity:** `Community`\
**Package:** `com.decisionhub.entity.community`\
**Table:** `communities`

  Column         Type                  Constraints
  -------------- --------------------- --------------------
  id             BIGINT                PK
  name           VARCHAR(100)          NOT NULL
  slug           VARCHAR(100)          UNIQUE
  description    TEXT                  
  category_id    BIGINT                FK → categories.id
  owner_id       BIGINT                FK → users.id
  visibility     CommunityVisibility   
  member_count   INT                   
  deleted_at     TIMESTAMP             

**Relationships** - Many Communities → One User (owner) - Many
Communities → One Category - One Community → Many CommunityMember - One
Community → Many Decision

------------------------------------------------------------------------

# 6. CommunityMember

**Entity:** `CommunityMember`\
**Package:** `com.decisionhub.entity.community`\
**Table:** `community_members`

  Column         Type                  Constraints
  -------------- --------------------- -------------
  id             BIGINT                PK
  community_id   BIGINT                FK
  user_id        BIGINT                FK
  role           CommunityMemberRole   
  status         MembershipStatus      
  joined_at      TIMESTAMP             

------------------------------------------------------------------------

# 7. UserInterest

**Entity:** `UserInterest`\
**Package:** `com.decisionhub.entity.community`\
**Table:** `user_interests`

  Column        Type        Constraints
  ------------- ----------- -------------
  id            BIGINT      PK
  user_id       BIGINT      FK
  category_id   BIGINT      FK
  created_at    TIMESTAMP   

------------------------------------------------------------------------

# 8. Decision

**Entity:** `Decision`\
**Package:** `com.decisionhub.entity.decision`\
**Table:** `decisions`

  Column         Type                 Constraints
  -------------- -------------------- ---------------------
  id             BIGINT               PK
  title          VARCHAR(255)         NOT NULL
  description    TEXT                 
  creator_id     BIGINT               FK → users.id
  community_id   BIGINT               FK → communities.id
  visibility     DecisionVisibility   
  status         DecisionStatus       
  deadline       TIMESTAMP            
  created_at     TIMESTAMP            
  updated_at     TIMESTAMP            

**Relationships** - One Decision → Many DecisionOption - One Decision →
Many ComparisonFactor - One Decision → Many Poll - One Decision → Many
Comment

------------------------------------------------------------------------

# 9. DecisionOption

**Entity:** `DecisionOption`\
**Package:** `com.decisionhub.entity.decision`\
**Table:** `decision_options`

  Column        Type           Constraints
  ------------- -------------- -------------
  id            BIGINT         PK
  option_name   VARCHAR(255)   NOT NULL
  description   TEXT           
  decision_id   BIGINT         FK

**Relationships** - One DecisionOption → Many OptionFactorScore - One
DecisionOption → Many ProsCons - One DecisionOption → Many Vote

------------------------------------------------------------------------

# 10. ComparisonFactor

**Entity:** `ComparisonFactor`\
**Package:** `com.decisionhub.entity.decision`\
**Table:** `comparison_factors`

  Column        Type           Constraints
  ------------- -------------- -------------
  id            BIGINT         PK
  name          VARCHAR(100)   NOT NULL
  description   VARCHAR(500)   
  weight        INT            NOT NULL
  decision_id   BIGINT         FK

**Relationships** - One ComparisonFactor → Many OptionFactorScore

------------------------------------------------------------------------
# DecisionHub Database Specification (Part 2)

## 11. ComparisonFactorTemplate

-   **Entity:** ComparisonFactorTemplate
-   **Package:** com.decisionhub.entity.decision
-   **Table:** comparison_factor_templates

  Column           Type           Constraints
  ---------------- -------------- -------------
  id               BIGINT         PK
  template_name    VARCHAR(255)   NOT NULL
  factor_name      VARCHAR(100)   NOT NULL
  description      VARCHAR(500)   NULL
  default_weight   INT            NOT NULL

## 12. OptionFactorScore

-   **Entity:** OptionFactorScore
-   **Package:** com.decisionhub.entity.decision
-   **Table:** option_factor_scores

  Column      Type     Constraints
  ----------- -------- ----------------------------
  id          BIGINT   PK
  score       INT      NOT NULL
  option_id   BIGINT   FK → decision_options.id
  factor_id   BIGINT   FK → comparison_factors.id

## 13. ProsCons

-   **Entity:** ProsCons
-   **Package:** com.decisionhub.entity.decision
-   **Table:** pros_cons

  Column       Type            Constraints
  ------------ --------------- -------------
  id           BIGINT          PK
  type         ProsConsType    NOT NULL
  content      VARCHAR(1000)   NOT NULL
  option_id    BIGINT          FK
  user_id      BIGINT          FK
  created_at   TIMESTAMP       NOT NULL

## 14. Poll

-   **Entity:** Poll
-   **Package:** com.decisionhub.entity.voting
-   **Table:** polls

  Column        Type           Constraints
  ------------- -------------- -------------
  id            BIGINT         PK
  question      VARCHAR(255)   NOT NULL
  decision_id   BIGINT         FK
  created_by    BIGINT         FK
  poll_type     PollType       NOT NULL
  status        PollStatus     NOT NULL
  end_time      TIMESTAMP      NULL
  created_at    TIMESTAMP      NOT NULL

## 15. Vote

-   **Entity:** Vote
-   **Package:** com.decisionhub.entity.voting
-   **Table:** votes

  Column      Type        Constraints
  ----------- ----------- -------------
  id          BIGINT      PK
  user_id     BIGINT      FK
  poll_id     BIGINT      FK
  option_id   BIGINT      FK
  voted_at    TIMESTAMP   NOT NULL

## 16. Comment

-   **Entity:** Comment
-   **Package:** com.decisionhub.entity.discussion
-   **Table:** comments

  Column              Type            Constraints
  ------------------- --------------- -------------
  id                  BIGINT          PK
  content             VARCHAR(1000)   NOT NULL
  user_id             BIGINT          FK
  decision_id         BIGINT          FK
  parent_comment_id   BIGINT          Self FK
  created_at          TIMESTAMP       NOT NULL
  updated_at          TIMESTAMP       NULL
  deleted_at          TIMESTAMP       NULL

## 17. Notification

-   **Entity:** Notification
-   **Package:** com.decisionhub.entity.notification
-   **Table:** notifications

  Column       Type               Constraints
  ------------ ------------------ -------------
  id           BIGINT             PK
  message      VARCHAR(500)       NOT NULL
  type         NotificationType   NOT NULL
  is_read      BOOLEAN            NOT NULL
  user_id      BIGINT             FK
  created_at   TIMESTAMP          NOT NULL

## 18. NotificationPreference

-   **Entity:** NotificationPreference
-   **Package:** com.decisionhub.entity.notification
-   **Table:** notification_preferences

  Column           Type      Constraints
  ---------------- --------- -------------
  id               BIGINT    PK
  email_enabled    BOOLEAN   NOT NULL
  push_enabled     BOOLEAN   NOT NULL
  in_app_enabled   BOOLEAN   NOT NULL
  user_id          BIGINT    FK (UNIQUE)

## 19. ReportExport

-   **Entity:** ReportExport
-   **Package:** com.decisionhub.entity.reports
-   **Table:** report_exports

  Column         Type           Constraints
  -------------- -------------- -------------
  id             BIGINT         PK
  report_type    ReportType     NOT NULL
  file_url       VARCHAR(500)   NOT NULL
  generated_at   TIMESTAMP      NOT NULL
  requested_by   BIGINT         FK

## 20. AuditLog

-   **Entity:** AuditLog
-   **Package:** com.decisionhub.entity.administration
-   **Table:** audit_logs

  Column         Type              Constraints
  -------------- ----------------- -------------
  id             BIGINT            PK
  action         AuditActionType   NOT NULL
  performed_at   TIMESTAMP         NOT NULL
  performed_by   BIGINT            FK

## 21. ModerationAction

-   **Entity:** ModerationAction
-   **Package:** com.decisionhub.entity.administration
-   **Table:** moderation_actions

  Column         Type                   Constraints
  -------------- ---------------------- -------------
  id             BIGINT                 PK
  action_type    ModerationActionType   NOT NULL
  reason         VARCHAR(1000)          NOT NULL
  performed_at   TIMESTAMP              NOT NULL
  performed_by   BIGINT                 FK

------------------------------------------------------------------------

This document completes the 21-table DecisionHub database specification.

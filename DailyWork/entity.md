├── entity
│   ├── authentication
│   │   ├── User
│   │   ├── OAuthAccount
│   │   └── PasswordResetToken
│   ├── community
│   │   ├── Category
│   │   ├── Community
│   │   ├── CommunityMember
│   │   └── UserInterest
│   ├── decision
│   │   ├── Decision
│   │   ├── DecisionOption
│   │   ├── ComparisonFactor
│   │   ├── ComparisonFactorTemplate
│   │   ├── OptionFactorScore
│   │   └── ProsCons
│   ├── voting
│   │   ├── Poll
│   │   └── Vote
│   ├── discussion
│   │   └── Comment
│   ├── notification
│   │   ├── Notification
│   │   └── NotificationPreference
│   ├── reports
│   │   └── ReportExport
│   └── administration
│       ├── AuditLog
│       └── ModerationAction
│
└── enums
    ├── authentication
    │   ├── PlatformRole
    │   └── UserStatus
    ├── community
    │   ├── CommunityVisibility
    │   ├── CommunityMemberRole
    │   └── MembershipStatus
    ├── decision
    │   ├── DecisionStatus
    │   ├── DecisionVisibility
    │   └── ProsConsType
    ├── voting
    │   ├── PollStatus
    │   └── PollType
    ├── notification
    │   └── NotificationType
    ├── reports
    │   └── ReportType
    └── administration
        ├── AuditActionType
        └── ModerationActionType
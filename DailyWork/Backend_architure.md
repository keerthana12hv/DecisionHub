backend
└── src
    └── main
        ├── java
        │   └── com
        │       └── decisionhub
        │
        │           ├── config
        │           │   ├── CorsConfig.java
        │           │   ├── JwtAuthenticationFilter.java
        │           │   ├── JwtService.java
        │           │   ├── PasswordConfig.java
        │           │   ├── SecurityConfig.java
        │           │   └── OpenApiConfig.java (Optional)
        │
        │           ├── controller
        │           │   ├── AuthController.java
        │           │   ├── CommunityController.java
        │           │   ├── DecisionController.java
        │           │   ├── VoteController.java
        │           │   ├── PollController.java
        │           │   ├── CommentController.java
        │           │   ├── NotificationController.java
        │           │   ├── ReportController.java
        │           │   ├── UserController.java
        │           │   └── AdminController.java
        │
        │           ├── dto
        │           │
        │           │   ├── request
        │           │   │
        │           │   │   ├── authentication
        │           │   │   │   ├── LoginRequest.java
        │           │   │   │   └── RegisterRequest.java
        │           │   │   │
        │           │   │   ├── community
        │           │   │   │   ├── CreateCommunityRequest.java
        │           │   │   │   └── UpdateCommunityRequest.java
        │           │   │   │
        │           │   │   ├── decision
        │           │   │   │   ├── CreateDecisionRequest.java
        │           │   │   │   └── UpdateDecisionRequest.java
        │           │   │   │
        │           │   │   ├── voting
        │           │   │   │   ├── VoteRequest.java
        │           │   │   │   └── PollRequest.java
        │           │   │   │
        │           │   │   ├── discussion
        │           │   │   │   └── CommentRequest.java
        │           │   │   │
        │           │   │   └── notification
        │           │   │       └── NotificationRequest.java
        │           │
        │           │   └── response
        │           │
        │           │       ├── authentication
        │           │       │   ├── LoginResponse.java
        │           │       │   └── RegisterResponse.java
        │           │       │
        │           │       ├── community
        │           │       │   └── CommunityResponse.java
        │           │       │
        │           │       ├── decision
        │           │       │   └── DecisionResponse.java
        │           │       │
        │           │       ├── voting
        │           │       │   ├── VoteResponse.java
        │           │       │   └── PollResponse.java
        │           │       │
        │           │       ├── discussion
        │           │       │   └── CommentResponse.java
        │           │       │
        │           │       ├── user
        │           │       │   └── UserResponse.java
        │           │       │
        │           │       └── ApiResponse.java
        │
        │           ├── entity
        │           │
        │           │   ├── authentication
        │           │   │   ├── User.java
        │           │   │   ├── OAuthAccount.java
        │           │   │   └── PasswordResetToken.java
        │           │
        │           │   ├── community
        │           │   │   ├── Category.java
        │           │   │   ├── Community.java
        │           │   │   ├── CommunityMember.java
        │           │   │   └── UserInterest.java
        │           │
        │           │   ├── decision
        │           │   │   ├── Decision.java
        │           │   │   ├── DecisionOption.java
        │           │   │   ├── ComparisonFactor.java
        │           │   │   ├── ComparisonFactorTemplate.java
        │           │   │   ├── OptionFactorScore.java
        │           │   │   └── ProsCons.java
        │           │
        │           │   ├── voting
        │           │   │   ├── Poll.java
        │           │   │   └── Vote.java
        │           │
        │           │   ├── discussion
        │           │   │   └── Comment.java
        │           │
        │           │   ├── notification
        │           │   │   ├── Notification.java
        │           │   │   └── NotificationPreference.java
        │           │
        │           │   ├── reports
        │           │   │   └── ReportExport.java
        │           │
        │           │   └── administration
        │           │       ├── AuditLog.java
        │           │       └── ModerationAction.java
        │
        │           ├── enums
        │           │
        │           │   ├── authentication
        │           │   │   ├── PlatformRole.java
        │           │   │   └── UserStatus.java
        │           │
        │           │   ├── community
        │           │   │   ├── CommunityVisibility.java
        │           │   │   ├── CommunityMemberRole.java
        │           │   │   └── MembershipStatus.java
        │           │
        │           │   ├── decision
        │           │   │   ├── DecisionStatus.java
        │           │   │   ├── DecisionVisibility.java
        │           │   │   └── ProsConsType.java
        │           │
        │           │   ├── voting
        │           │   │   ├── PollStatus.java
        │           │   │   └── PollType.java
        │           │
        │           │   ├── notification
        │           │   │   └── NotificationType.java
        │           │
        │           │   ├── reports
        │           │   │   └── ReportType.java
        │           │
        │           │   └── administration
        │           │       ├── AuditActionType.java
        │           │       └── ModerationActionType.java
        │
        │           ├── exception
        │           │   ├── ApiErrorResponse.java
        │           │   ├── GlobalExceptionHandler.java
        │           │   ├── ResourceNotFoundException.java
        │           │   ├── ResourceAlreadyExistsException.java
        │           │   ├── UnauthorizedActionException.java
        │           │   └── BadRequestException.java
        │
        │           ├── mapper
        │           │
        │           │   ├── community
        │           │   │   └── CommunityMapper.java
        │           │
        │           │   ├── decision
        │           │   ├── voting
        │           │   ├── discussion
        │           │   ├── notification
        │           │   └── reports
        │
        │           ├── repository
        │           │
        │           │   ├── authentication
        │           │   │   └── UserRepository.java
        │           │
        │           │   ├── community
        │           │   │   ├── CategoryRepository.java
        │           │   │   ├── CommunityRepository.java
        │           │   │   ├── CommunityMemberRepository.java
        │           │   │   └── UserInterestRepository.java
        │           │
        │           │   ├── decision
        │           │   ├── voting
        │           │   ├── discussion
        │           │   ├── notification
        │           │   └── reports
        │
        │           ├── service
        │           │
        │           │   ├── interfaces
        │           │   │
        │           │   │   ├── authentication
        │           │   │   │   └── AuthService.java
        │           │   │   │
        │           │   │   ├── community
        │           │   │   │   └── CommunityService.java
        │           │   │   │
        │           │   │   ├── decision
        │           │   │   ├── voting
        │           │   │   ├── discussion
        │           │   │   ├── notification
        │           │   │   └── reports
        │           │
        │           │   └── impl
        │           │
        │           │       ├── authentication
        │           │       │   ├── AuthServiceImpl.java
        │           │       │   └── CustomUserDetailsService.java
        │           │       │
        │           │       ├── community
        │           │       │   └── CommunityServiceImpl.java
        │           │       │
        │           │       ├── decision
        │           │       ├── voting
        │           │       ├── discussion
        │           │       ├── notification
        │           │       └── reports
        │
        │           ├── util
        │           │   ├── Constants.java
        │           │   ├── DateUtil.java
        │           │   └── ValidationUtil.java
        │
        │           └── BackendApplication.java
        │
        └── resources
            ├── application.properties
            ├── static
            └── templates
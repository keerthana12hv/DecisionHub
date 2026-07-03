backend
└── src
    └── main
        ├── java
        │   └── com
        │       └── decisionhub
        │           ├── config
        │           │   ├── CorsConfig.java
        │           │   └── OpenApiConfig.java (Optional)
        │           ├── controller
        │           │   ├── AuthController.java
        │           │   ├── UserController.java
        │           │   ├── CommunityController.java
        │           │   ├── DecisionController.java
        │           │   ├── PollController.java
        │           │   ├── VoteController.java
        │           │   ├── CommentController.java
        │           │   ├── NotificationController.java
        │           │   ├── ReportController.java
        │           │   └── AdminController.java
        │           ├── dto
        │           │   ├── request
        │           │   │   ├── LoginRequest.java
        │           │   │   ├── RegisterRequest.java
        │           │   │   ├── CreateCommunityRequest.java
        │           │   │   ├── CreateDecisionRequest.java
        │           │   │   ├── VoteRequest.java
        │           │   │   ├── CommentRequest.java
        │           │   │   ├── PollRequest.java
        │           │   │   └── NotificationRequest.java
        │           │   └── response
        │           │       ├── LoginResponse.java
        │           │       ├── UserResponse.java
        │           │       ├── CommunityResponse.java
        │           │       ├── DecisionResponse.java
        │           │       ├── VoteResponse.java
        │           │       ├── CommentResponse.java
        │           │       ├── PollResponse.java
        │           │       └── ApiResponse.java
        │           ├── entity
        │           │   ├── authentication
        │           │   │   ├── User
        │           │   │   ├── OAuthAccount
        │           │   │   └── PasswordResetToken
        │           │   ├── community
        │           │   │   ├── Category
        │           │   │   ├── Community
        │           │   │   ├── CommunityMember
        │           │   │   └── UserInterest
        │           │   ├── decision
        │           │   │   ├── Decision
        │           │   │   ├── DecisionOption
        │           │   │   ├── ComparisonFactor
        │           │   │   ├── ComparisonFactorTemplate
        │           │   │   ├── OptionFactorScore
        │           │   │   └── ProsCons
        │           │   ├── voting
        │           │   │   ├── Poll
        │           │   │   └── Vote
        │           │   ├── discussion
        │           │   │   └── Comment
        │           │   ├── notification
        │           │   │   ├── Notification
        │           │   │   └── NotificationPreference
        │           │   ├── reports
        │           │   │   └── ReportExport
        │           │   └── administration
        │           │       ├── AuditLog
        │           │       └── ModerationAction
        │           ├── enums
        │           │   ├── authentication
        │           │   │   ├── PlatformRole
        │           │   │   └── UserStatus
        │           │   ├── community
        │           │   │   ├── CommunityVisibility
        │           │   │   ├── CommunityMemberRole
        │           │   │   └── MembershipStatus
        │           │   ├── decision
        │           │   │   ├── DecisionStatus
        │           │   │   ├── DecisionVisibility
        │           │   │   └── ProsConsType
        │           │   ├── voting
        │           │   │   ├── PollStatus
        │           │   │   └── PollType
        │           │   ├── notification
        │           │   │   └── NotificationType
        │           │   ├── reports
        │           │   │   └── ReportType
        │           │   └── administration
        │           │       ├── AuditActionType
        │           │       └── ModerationActionType
        │           ├── exception
        │           │   ├── GlobalExceptionHandler.java
        │           │   ├── ResourceNotFoundException.java
        │           │   ├── BadRequestException.java
        │           │   ├── UnauthorizedException.java
        │           │   └── ForbiddenException.java
        │           ├── repository
        │           │   ├── UserRepository.java
        │           │   ├── CommunityRepository.java
        │           │   ├── DecisionRepository.java
        │           │   ├── VoteRepository.java
        │           │   ├── CommentRepository.java
        │           │   ├── PollRepository.java
        │           │   ├── NotificationRepository.java
        │           │   └── ReportRepository.java
        │           ├── security
        │           │   ├── JwtAuthenticationFilter.java
        │           │   ├── JwtService.java
        │           │   ├── SecurityConfig.java
        │           │   ├── CustomUserDetailsService.java
        │           │   └── PasswordConfig.java
        │           ├── service
        │           │   ├── interfaces
        │           │   │   ├── AuthService.java
        │           │   │   ├── UserService.java
        │           │   │   ├── CommunityService.java
        │           │   │   ├── DecisionService.java
        │           │   │   ├── VoteService.java
        │           │   │   ├── CommentService.java
        │           │   │   ├── PollService.java
        │           │   │   ├── NotificationService.java
        │           │   │   └── ReportService.java
        │           │   └── impl
        │           │       ├── AuthServiceImpl.java
        │           │       ├── UserServiceImpl.java
        │           │       ├── CommunityServiceImpl.java
        │           │       ├── DecisionServiceImpl.java
        │           │       ├── VoteServiceImpl.java
        │           │       ├── CommentServiceImpl.java
        │           │       ├── PollServiceImpl.java
        │           │       ├── NotificationServiceImpl.java
        │           │       └── ReportServiceImpl.java
        │           ├── util
        │           │   ├── Constants.java
        │           │   ├── JwtUtil.java
        │           │   ├── DateUtil.java
        │           │   └── ValidationUtil.java
        │           └── BackendApplication.java
        │
        └── resources
            ├── application.properties
            ├── static
            └── templates
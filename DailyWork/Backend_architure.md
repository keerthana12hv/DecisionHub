# DecisionHub Backend Architecture

This document defines the backend architecture for the DecisionHub project.

Every backend developer must follow this structure while implementing features.

---

# Backend Folder Structure

backend
└── src
    └── main
        ├── java
        │   └── com
        │       └── decisionhub
        │
        │           ├── config
        │           │     ├── CorsConfig.java
        │           │     └── OpenApiConfig.java (Optional)
        │           │
        │           ├── controller
        │           │     ├── AuthController.java
        │           │     ├── UserController.java
        │           │     ├── CommunityController.java
        │           │     ├── DecisionController.java
        │           │     ├── PollController.java
        │           │     ├── VoteController.java
        │           │     ├── CommentController.java
        │           │     ├── NotificationController.java
        │           │     ├── ReportController.java
        │           │     └── AdminController.java
        │           │
        │           ├── dto
        │           │
        │           │     ├── request
        │           │     │
        │           │     ├── LoginRequest.java
        │           │     ├── RegisterRequest.java
        │           │     ├── CreateCommunityRequest.java
        │           │     ├── CreateDecisionRequest.java
        │           │     ├── VoteRequest.java
        │           │     ├── CommentRequest.java
        │           │     ├── PollRequest.java
        │           │     └── NotificationRequest.java
        │           │
        │           │
        │           │     └── response
        │           │
        │           │     ├── LoginResponse.java
        │           │     ├── UserResponse.java
        │           │     ├── CommunityResponse.java
        │           │     ├── DecisionResponse.java
        │           │     ├── VoteResponse.java
        │           │     ├── CommentResponse.java
        │           │     ├── PollResponse.java
        │           │     └── ApiResponse.java
        │           │
        │           ├── entity
        │           │
        │           │     ├── PlatformRole.java
        │           │     ├── User.java
        │           │     ├── OAuthAccount.java
        │           │     ├── PasswordResetToken.java
        │           │
        │           │     ├── Category.java
        │           │     ├── UserInterest.java
        │           │     ├── Community.java
        │           │     ├── CommunityMember.java
        │           │
        │           │     ├── Decision.java
        │           │     ├── DecisionOption.java
        │           │     ├── ComparisonFactor.java
        │           │     ├── ComparisonFactorTemplate.java
        │           │     ├── OptionFactorScore.java
        │           │     ├── ProsCons.java
        │           │
        │           │     ├── Poll.java
        │           │     ├── Vote.java
        │           │
        │           │     ├── Comment.java
        │           │
        │           │     ├── Notification.java
        │           │     ├── NotificationType.java
        │           │     ├── NotificationPreference.java
        │           │
        │           │     ├── ReportExport.java
        │           │
        │           │     ├── AuditLog.java
        │           │     └── ModerationAction.java
        │           │
        │           ├── enums
        │           │
        │           │     ├── UserRole.java
        │           │     ├── UserStatus.java
        │           │     ├── DecisionStatus.java
        │           │     ├── DecisionVisibility.java
        │           │     ├── CommunityVisibility.java
        │           │     ├── CommunityRole.java
        │           │     ├── PollType.java
        │           │     ├── VoteType.java
        │           │     ├── NotificationChannel.java
        │           │     ├── ReportType.java
        │           │     └── ModerationActionType.java
        │           │
        │           ├── exception
        │           │
        │           │     ├── GlobalExceptionHandler.java
        │           │     ├── ResourceNotFoundException.java
        │           │     ├── BadRequestException.java
        │           │     ├── UnauthorizedException.java
        │           │     └── ForbiddenException.java
        │           │
        │           ├── repository
        │           │
        │           │     ├── UserRepository.java
        │           │     ├── CommunityRepository.java
        │           │     ├── DecisionRepository.java
        │           │     ├── VoteRepository.java
        │           │     ├── CommentRepository.java
        │           │     ├── PollRepository.java
        │           │     ├── NotificationRepository.java
        │           │     └── ReportRepository.java
        │           │
        │           ├── security
        │           │
        │           │     ├── JwtAuthenticationFilter.java
        │           │     ├── JwtService.java
        │           │     ├── SecurityConfig.java
        │           │     ├── CustomUserDetailsService.java
        │           │     └── PasswordConfig.java
        │           │
        │           ├── service
        │           │
        │           │     ├── interfaces
        │           │     │
        │           │     ├── AuthService.java
        │           │     ├── UserService.java
        │           │     ├── CommunityService.java
        │           │     ├── DecisionService.java
        │           │     ├── VoteService.java
        │           │     ├── CommentService.java
        │           │     ├── PollService.java
        │           │     ├── NotificationService.java
        │           │     └── ReportService.java
        │           │
        │           │
        │           │     └── impl
        │           │
        │           │     ├── AuthServiceImpl.java
        │           │     ├── UserServiceImpl.java
        │           │     ├── CommunityServiceImpl.java
        │           │     ├── DecisionServiceImpl.java
        │           │     ├── VoteServiceImpl.java
        │           │     ├── CommentServiceImpl.java
        │           │     ├── PollServiceImpl.java
        │           │     ├── NotificationServiceImpl.java
        │           │     └── ReportServiceImpl.java
        │           │
        │           ├── util
        │           │
        │           │     ├── Constants.java
        │           │     ├── JwtUtil.java
        │           │     ├── DateUtil.java
        │           │     └── ValidationUtil.java
        │           │
        │           └── BackendApplication.java
        │
        └── resources
              ├── application.properties
              ├── static
              └── templates

---
# Backend Development Roadmap

This roadmap follows a **phase-wise backend development approach** for the DecisionHub project. Every phase is designed to be completed independently while maintaining proper dependencies between modules.

The roadmap aligns with the mentor requirements while following an industry-standard layered Spring Boot architecture.

---

# Development Methodology

Every backend phase should follow the exact implementation order below.

## Implementation Order (Mandatory)

1. Create Enum Classes (if applicable)
2. Create Entity Classes
3. Create Repository Interfaces
4. Create Request DTOs
5. Create Response DTOs
6. Create Mapper Classes (if required)
7. Create Service Interface
8. Create Service Implementation
9. Create Custom Exceptions (if required)
10. Create Global Exception Handling
11. Create Controller APIs
12. Secure APIs using Spring Security
13. Test APIs using Postman
14. Integrate with Frontend

---

# Backend Architecture

Every module follows the same layered architecture.

```
Controller
      │
      ▼
Service Interface
      │
      ▼
Service Implementation
      │
      ▼
Repository
      │
      ▼
Database
```

Supporting layers

- DTOs
- Mapper
- Exceptions
- Security
- Validation
- Enums

---

# Coding Standards

Every module should follow these practices.

- Layered Architecture
- Constructor Injection
- DTO-based communication
- Global Exception Handling
- JWT Authentication
- Role-Based Authorization
- Soft Delete wherever applicable
- Validation using Jakarta Validation
- Proper REST API naming
- Transaction Management using `@Transactional`
- Consistent API responses

---

# 🚩 Milestone 1 (Week 1 – Week 2)

# Objective

Build the backend foundation by implementing:

- Spring Boot Project Setup
- Database Configuration
- Authentication & Authorization
- User Profile Management
- Community Management
- Decision Management

This milestone establishes the core architecture that every future module depends on.

---

# Phase 1 – Authentication & User Profile Management

## Objective

Develop a secure authentication and user management system using Spring Security and JWT.

Users should be able to

- Register
- Login
- Authenticate using JWT
- Manage Profile
- Manage Interests
- View Personal History
- Reset Password
- Login using OAuth (Future Enhancement)

---

# Modules

## Authentication

- User Registration
- User Login
- JWT Authentication
- Password Encryption
- Password Reset
- OAuth Login (Google)

---

## User Profile

- View Profile
- Update Profile
- Update Interests
- Change Password
- Saved Decisions
- Decision History
- Voting History
- Account Settings

---

# Enums

```java
PlatformRole

USER
MODERATOR
ADMIN
```

```java
UserStatus

ACTIVE
INACTIVE
SUSPENDED
DELETED
```

---

# Entities

## User

Stores user information.

### Responsibilities

- Authentication
- User Profile
- Roles
- Status
- Account Information

---

## OAuthAccount

Stores OAuth provider information.

Supports

- Google Login
- Future OAuth Providers

---

## PasswordResetToken

Stores password reset tokens.

Responsibilities

- Token Generation
- Token Expiry
- Password Recovery

---

# Java Files

## Entity

```
User.java

OAuthAccount.java

PasswordResetToken.java
```

---

## Repository

```
UserRepository.java

OAuthAccountRepository.java

PasswordResetTokenRepository.java
```

---

## DTO

### Request

```
RegisterRequest.java

LoginRequest.java

ForgotPasswordRequest.java

ResetPasswordRequest.java

UpdateProfileRequest.java

UpdatePasswordRequest.java

UpdateInterestsRequest.java
```

---

### Response

```
RegisterResponse.java

LoginResponse.java

UserResponse.java

ProfileResponse.java

ApiResponse.java
```

---

## Mapper

```
UserMapper.java
```

---

## Service

```
AuthService.java

UserService.java
```

---

## Service Implementation

```
AuthServiceImpl.java

UserServiceImpl.java
```

---

## Controller

```
AuthController.java

UserController.java
```

---

## Security

```
SecurityConfig.java

JwtAuthenticationFilter.java

JwtService.java

CustomUserDetailsService.java

PasswordConfig.java
```

---

## Exception

```
ApiErrorResponse.java

GlobalExceptionHandler.java

ResourceNotFoundException.java

ResourceAlreadyExistsException.java

UnauthorizedActionException.java

BadRequestException.java
```

---

# Database Tables

```
users

oauth_accounts

password_reset_tokens
```

---

# REST APIs

## Authentication APIs

### Register User

```http
POST /api/auth/register
```

Purpose

- Register a new user

Authentication

- Not Required

---

### Login User

```http
POST /api/auth/login
```

Purpose

- Authenticate user
- Generate JWT

Authentication

- Not Required

---

### Forgot Password

```http
POST /api/auth/forgot-password
```

Purpose

- Generate password reset token

Authentication

- Not Required

---

### Reset Password

```http
POST /api/auth/reset-password
```

Purpose

- Reset user password

Authentication

- Not Required

---

### OAuth Login

```http
POST /api/auth/oauth/google
```

Purpose

- Google Login

Authentication

- Not Required

---

# User Profile APIs

### Get Logged-in User Profile

```http
GET /api/users/profile
```

Authentication

Required

---

### Update Profile

```http
PUT /api/users/profile
```

Authentication

Required

---

### Change Password

```http
PUT /api/users/password
```

Authentication

Required

---

### Get User Interests

```http
GET /api/users/interests
```

Authentication

Required

---

### Update User Interests

```http
PUT /api/users/interests
```

Authentication

Required

---

### Get Decision History

```http
GET /api/users/history/decisions
```

Authentication

Required

---

### Get Voting History

```http
GET /api/users/history/votes
```

Authentication

Required

---

### Get Saved Decisions

```http
GET /api/users/saved-decisions
```

Authentication

Required

---

# Security Features

Implemented

- Spring Security
- JWT Authentication
- Password Encryption (BCrypt)
- Stateless Authentication
- Authentication Filter
- Role-based Authorization
- Protected APIs

---

# Roles

## USER

Can

- Register
- Login
- Create Communities
- Join Communities
- Create Decisions
- Vote
- Comment

Cannot

- Access Admin APIs

---

## MODERATOR

Can

- Moderate platform content
- Manage reported content

Cannot

- Perform full platform administration

---

## ADMIN

Has full access to

- Users
- Communities
- Categories
- Decisions
- Reports
- Audit Logs
- Moderation
- Platform Management

---

# Validation Rules

## Registration

- Username required
- Email required
- Email unique
- Username unique
- Password minimum length
- Password maximum length

---

## Login

- Email required
- Password required

---

## Profile Update

- Valid email
- Username length validation
- Input sanitization

---

# Exception Handling

Examples

```
Email already exists

Username already exists

Invalid credentials

User not found

Password reset token expired

Unauthorized access

Invalid JWT

Account suspended
```

All exceptions should be handled using

```
GlobalExceptionHandler
```

---

# Edge Cases Covered

✅ Duplicate email

✅ Duplicate username

✅ Invalid credentials

✅ Invalid JWT

✅ Expired JWT

✅ Password mismatch

✅ Invalid password reset token

✅ Suspended account

✅ Deleted account

✅ Unauthorized access

---

# Dependencies

Phase 1 must be completed before

- Community Module
- Decision Module
- Voting Module
- Notifications
- Reports
- Administration

Every future module depends on authentication.

---

# Deliverables

- Spring Boot Setup
- PostgreSQL Configuration
- Spring Security Configuration
- JWT Authentication
- User Registration
- User Login
- Password Encryption
- Password Reset
- OAuth Integration (Foundation)
- User Profile APIs
- User Interests APIs
- Decision History APIs
- Voting History APIs
- Saved Decisions APIs
- Global Exception Handling
- Authentication Testing
- Postman Collection
- Frontend Authentication Ready

---

# Notes for Future Phases

This phase provides the authentication foundation used by every subsequent module.

Future modules should **never** implement their own authentication logic. Instead, they must retrieve the currently authenticated user through Spring Security's `SecurityContextHolder` (or a shared helper method such as `getCurrentUser()`), ensuring consistent authorization across the application.

Phase 2 (Community Management) will be the first consumer of this authentication infrastructure.
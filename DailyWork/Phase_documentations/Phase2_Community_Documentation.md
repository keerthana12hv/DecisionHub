# DecisionHub - Phase 2 Documentation

## Community Management Module

**Status:** ✅ Completed

------------------------------------------------------------------------

# Objective

Implement the complete Community Management module with secure JWT
authentication, ownership, membership management, soft delete, and
global exception handling.

------------------------------------------------------------------------

# APIs Implemented

  -------------------------------------------------------------------------------------------------------
  Method          Endpoint                                           Description              Auth
  --------------- -------------------------------------------------- ------------------------ -----------
  POST            http://localhost:8080/api/communities              Create Community         JWT

  GET             http://localhost:8080/api/communities              Get All Communities      JWT

  GET             http://localhost:8080/api/communities/{id}         Get Community By Id      JWT

  PUT             http://localhost:8080/api/communities/{id}         Update Community         JWT (Owner
                                                                                              Only)

  DELETE          http://localhost:8080/api/communities/{id}         Soft Delete Community    JWT (Owner
                                                                                              Only)

  POST            http://localhost:8080/api/communities/{id}/join    Join Community           JWT

  POST            http://localhost:8080/api/communities/{id}/leave   Leave Community          JWT
  -------------------------------------------------------------------------------------------------------

------------------------------------------------------------------------

# Authentication

Every endpoint requires:

    Authorization: Bearer <JWT_TOKEN>

Obtain the token by logging in:

    POST http://localhost:8080/api/auth/login

------------------------------------------------------------------------

# API Testing Guide

## Registration

Now your database is perfectly clean! Let's register Keerthi as the first user.

Open your Register tab in Postman.

Method: POST 

URL: http://localhost:8080/api/auth/register 

Authorization: Set to No Auth.

Body (raw -> JSON):

JSON
{
    "username": "keerthi",
    "email": "keerthi@gmail.com",
    "password": "Password@123"
}
Hit Send. You will get a 200 OK response.
## 1. Login

    POST http://localhost:8080/api/auth/login

Example Body

``` json
{
  "email":"user@example.com",
  "password":"password123"
}
```

Copy the returned JWT.

------------------------------------------------------------------------

## 2. Create Community

    POST http://localhost:8080/api/communities

``` json
{
  "name":"Java Developers",
  "slug":"java-developers",
  "description":"Community for Java Developers",
  "categoryId":1,
  "visibility":"PUBLIC"
}
```

Expected

-   Community created
-   Logged-in user becomes OWNER
-   memberCount = 1

------------------------------------------------------------------------

## 3. Get All Communities

    GET http://localhost:8080/api/communities

Returns all active (non-deleted) communities.

------------------------------------------------------------------------

## 4. Get Community By Id

    GET http://localhost:8080/api/communities/1

------------------------------------------------------------------------

## 5. Update Community

    PUT http://localhost:8080/api/communities/1

``` json
{
  "name":"Advanced Java Developers",
  "slug":"advanced-java",
  "description":"Updated Community",
  "categoryId":1,
  "visibility":"PRIVATE"
}
```

Only the OWNER can update.

------------------------------------------------------------------------

## 6. Join Community

    POST http://localhost:8080/api/communities/1/join

Behavior

-   First join → success
-   Already joined → error
-   LEFT member → rejoins (status becomes APPROVED)

------------------------------------------------------------------------

## 7. Leave Community

    POST http://localhost:8080/api/communities/1/leave

Behavior

-   Member leaves successfully
-   Owner cannot leave
-   Already LEFT → error

------------------------------------------------------------------------

## 8. Delete Community

    DELETE http://localhost:8080/api/communities/1

Performs soft delete:

-   deleted_at updated
-   member_count = 0

------------------------------------------------------------------------

# Business Rules

-   Community name must be unique.
-   Community slug must be unique.
-   Creator automatically becomes OWNER.
-   Owner is the first APPROVED member.
-   Only OWNER can update/delete.
-   Duplicate memberships are prevented.
-   Leaving changes membership status to LEFT.
-   Rejoining restores APPROVED status.
-   Deleted communities are hidden from listing APIs.

------------------------------------------------------------------------

# Security

-   JWT Authentication
-   Current user resolved from SecurityContextHolder
-   Owner authorization enforced
-   No userId accepted from frontend

------------------------------------------------------------------------

# Database Tables Used

-   users
-   categories
-   communities
-   community_members
-   user_interests

------------------------------------------------------------------------

# Repository Methods

## CommunityRepository

-   save()
-   findById()
-   existsByName()
-   existsBySlug()
-   findByDeletedAtIsNull()

## CommunityMemberRepository

-   save()
-   findByCommunityAndUser()
-   existsByCommunityAndUser()

## CategoryRepository

-   findById()

## UserRepository

-   findByEmail()

------------------------------------------------------------------------

# Edge Cases Covered

## Create

-   Duplicate name
-   Duplicate slug
-   Invalid category

## Read

-   Community not found
-   Deleted community hidden

## Update

-   Non-owner update
-   Duplicate name
-   Duplicate slug
-   Deleted community

## Delete

-   Non-owner delete
-   Deleted community

## Join

-   Already member
-   Rejoin after leaving
-   Deleted community
-   Community not found

## Leave

-   Owner cannot leave
-   Already left
-   Membership not found
-   Deleted community

------------------------------------------------------------------------

# Exceptions Used

-   ResourceNotFoundException
-   ResourceAlreadyExistsException
-   UnauthorizedActionException
-   BadRequestException

Handled centrally through GlobalExceptionHandler.

------------------------------------------------------------------------

# Deliverables Completed

-   ✅ Community CRUD
-   ✅ JWT Protected APIs
-   ✅ Owner Authorization
-   ✅ Community Membership
-   ✅ Join Community
-   ✅ Leave Community
-   ✅ Rejoin Community
-   ✅ Soft Delete
-   ✅ Global Exception Handling
-   ✅ Postman Testing

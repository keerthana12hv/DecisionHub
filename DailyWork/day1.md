# DecisionHub Internship - Day 01

**Date:** 01 July 2026

---

# Objective

Set up the complete development environment for the DecisionHub project as instructed by the mentor.

The primary goal of Day 1 was to establish a complete backend, frontend, database, Git, and GitHub environment so that all team members could start feature development from a common project structure.

---

# Tasks Completed

## 1. Project Requirements Review

- Read and understood the mentor's project documentation.
- Understood the overall objective of DecisionHub.
- Identified the technology stack to be used.
- Understood the project milestones and implementation workflow.
- Reviewed the modules that will be implemented during the internship.

---

# 2. Backend Setup

Generated a Spring Boot project using Spring Initializr.

### Configuration

Project:
- Maven

Language:
- Java

Spring Boot Version:
- 3.5.16

Java Version:
- 21

Group ID:
- com.decisionhub

Artifact ID:
- backend

Package Name:
- com.decisionhub

Packaging:
- Jar

---

# 3. Backend Dependencies

Added the following dependencies:

- Spring Web
- Spring Data JPA
- Spring Security
- Validation
- PostgreSQL Driver
- Lombok
- Spring Boot DevTools

---

# 4. Backend Project Structure

Created the basic backend package structure.

```
com.decisionhub

├── config
├── controller
├── dto
├── entity
├── exception
├── repository
├── security
├── service
└── util
```

Created placeholder REST controllers for future module implementation.

Controllers:

- AuthController
- UserController
- DecisionController
- VoteController
- CommentController
- CommunityController
- NotificationController
- AnalyticsController
- ReportController

---

# 5. Database Setup

Installed PostgreSQL 16 and pgAdmin 4.

Created database:

```text
decision_hub
```

Configured PostgreSQL datasource inside:

```
application.properties
```

Configured:

- JDBC URL
- Username
- Password
- Hibernate properties
- PostgreSQL Driver

Verified successful database connectivity between Spring Boot and PostgreSQL.

---

# 6. Backend Execution

Ran the backend successfully using Maven.

Command:

```bash
cd backend
.\mvnw spring-boot:run
```

Verified:

- Spring Boot started successfully.
- Embedded Tomcat started on port 8080.
- Hibernate initialized successfully.
- HikariCP connected to PostgreSQL.
- No compilation errors.
- Application started without runtime errors.

Backend URL:

```
http://localhost:8080
```

---

# 7. Frontend Setup

Created frontend using Vite.

Command:

```bash
npm create vite@latest frontend
```

Selected:

Framework:

- React

Variant:

- JavaScript

Linter:

- ESLint

Installed dependencies:

```bash
npm install
```

Started frontend:

```bash
npm run dev
```

Verified frontend running successfully.

Frontend URL:

```
http://localhost:5173
```

---

# 8. Final Project Structure

```
DecisionHub

│
├── backend
│
└── frontend
```

---

# 9. Git & GitHub Setup

Initialized Git repository.

Commands used:

```bash
git init

git remote add origin https://github.com/keerthana12hv/DecisionHub.git

git add .

git commit -m "Initial project setup"

git branch -M main

git push -u origin main
```

Successfully connected the local project with GitHub.

Repository:

https://github.com/keerthana12hv/DecisionHub

---

# 10. Team Collaboration Setup

Prepared the repository for collaborative development.

Completed:

- Created GitHub repository.
- Added project collaborators.
- Shared repository with teammates.
- Planned branch-based development workflow.
- Configured branch protection strategy for the main branch.
- Decided to use Pull Requests before merging code into the main branch.

This setup ensures that all team members can work independently without directly affecting the main project.

---

# Technologies Used

## Backend

- Java 21
- Spring Boot 3.5.16
- Spring Security
- Spring Data JPA
- Hibernate
- Maven

---

## Frontend

- React
- JavaScript
- Vite
- ESLint

---

## Database

- PostgreSQL 16

---

## Version Control

- Git
- GitHub

---

## Development Tools

- VS Code
- PostgreSQL 16
- pgAdmin 4

---

# Problems Faced

## 1. Backend Main Class Missing

### Issue

Spring Boot failed to start because the main application class was accidentally missing.

### Solution

Created:

```
BackendApplication.java
```

inside:

```
com.decisionhub
```

and verified successful startup.

---

## 2. Running Wrong Java File

### Issue

Accidentally attempted to run:

```
BackendApplicationTests.java
```

using Java directly.

JUnit and Spring Boot dependencies were not available in that context, causing compilation errors.

### Solution

Ran the backend correctly using Maven:

```bash
.\mvnw spring-boot:run
```

---

## 3. GitHub Repository Connection

Successfully:

- Initialized Git
- Connected remote repository
- Committed project
- Pushed code to GitHub
- Verified successful synchronization

---

## 4. Database Migration

### Issue

The project was initially configured with MySQL during setup.

The mentor later instructed the team to use PostgreSQL instead.

### Solution

- Removed MySQL dependency.
- Added PostgreSQL dependency.
- Updated `application.properties`.
- Created PostgreSQL database.
- Verified successful backend connection.

---

# Current Status

Completed

✅ Spring Boot backend setup

✅ React frontend setup

✅ PostgreSQL setup

✅ pgAdmin configured

✅ Backend connected to PostgreSQL

✅ Spring Boot running successfully

✅ React running successfully

✅ Git repository initialized

✅ GitHub repository connected

✅ Initial project pushed to GitHub

✅ Team collaboration workflow prepared

---

# Pending Work

- JWT Authentication
- Role-based Authentication
- User Entity
- Database Entity Classes
- Repository Layer
- Service Layer
- REST APIs
- React UI Development
- Frontend-Backend Integration

---

# Learning Summary

Today I learned:

- How to initialize a Spring Boot project.
- How to configure a React application using Vite.
- How Spring Boot connects with PostgreSQL.
- How Maven manages project dependencies.
- How to run backend and frontend independently.
- How Git and GitHub work together.
- How to connect a local project to a remote GitHub repository.
- How collaborative development works using branches and Pull Requests.
- The overall architecture and technology stack of the DecisionHub project.

---

# Notes

Day 1 focused entirely on setting up the development environment.

The backend, frontend, PostgreSQL database, and GitHub repository have been successfully configured and integrated.

The project is now ready for Milestone 1 implementation, beginning with:

- JWT Authentication
- Database Entity Creation
- Repository Layer
- Service Layer
- REST API Development
- Frontend Integration
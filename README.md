<div align="center">

# 🗳️ DecisionHub

### Collaborative Decision-Making & Polling Platform

**Decide. Together.**

<img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/java/java-original.svg" width="45" height="45"/>&nbsp;&nbsp;
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/spring/spring-original.svg" width="45" height="45"/>&nbsp;&nbsp;
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/react/react-original.svg" width="45" height="45"/>&nbsp;&nbsp;
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/vitejs/vitejs-original.svg" width="45" height="45"/>&nbsp;&nbsp;
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/javascript/javascript-original.svg" width="45" height="45"/>&nbsp;&nbsp;
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/postgresql/postgresql-original.svg" width="45" height="45"/>

<br/><br/>

DecisionHub turns *"so... what do we do?"* into a real process — lay out your options, weigh them against the factors that matter, vote as a group, argue it out in threaded discussions, and let the analytics settle the debate.

<br/>

🌐 **[Live Demo](https://decisionhub-app.netlify.app/login)** &nbsp;·&nbsp; ✨ **[Features](#-key-features)** &nbsp;·&nbsp; 🏗️ **[Architecture](#%EF%B8%8F-system-architecture)** &nbsp;·&nbsp; 🚀 **[Getting Started](#-getting-started)**

</div>

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| 🎨 **Frontend** | React.js · Vite · JavaScript · React Router · Axios · CSS · React Icons |
| ⚙️ **Backend** | Java · Spring Boot · Spring Security + JWT · RESTful APIs |
| 🗄️ **Database** | PostgreSQL (hosted on Render) |
| ☁️ **Deployment** | Netlify (frontend) · Render (backend + database) |

---

## 📖 Overview

**DecisionHub** goes beyond simple polls. It lets users build structured decision boards — complete with multiple options, comparison factors, configurable voting methods, and deadlines — then discuss, vote, and analyze the outcome together.

The platform supports three roles (**User**, **Moderator**, **Administrator**) and covers communities, decision management, voting, discussions, notifications, moderation, and analytics end to end.

**🔗 Live Application:** [decisionhub-app.netlify.app](https://decisionhub-app.netlify.app/login)

---

## ✨ Key Features

<details open>
<summary><strong>👤 User Features</strong></summary>

- Registration and login with JWT-based authentication
- Role-based access control
- Profile and account management
- Create and manage decisions
- View pinned and locked decisions where applicable
- Add multiple options and comparison criteria/factors
- Configure voting methods and participate in polls
- View decision results and analytics
- Comment, reply, and report inappropriate content in discussions
- Join and participate in public/private communities
- In-app notifications
- Submit bug reports, suggestions, and general feedback
- Light/dark theme toggle

</details>

<details>
<summary><strong>🗳️ Decision Management</strong></summary>

Each decision board includes:

| Attribute | Description |
|---|---|
| Title & Description | Core details of the decision |
| Options | Multiple choices to vote on |
| Comparison Factors | Criteria used to score/compare options |
| Visibility | Public or private |
| Voting Type | Rating-based or multiple-choice |
| Deadline | Voting end time |
| Status | Tracks lifecycle from creation → voting → closure |
| Controls | Pin and lock support (moderator/admin permitted) |

</details>

<details>
<summary><strong>📊 Voting & Comparison</strong></summary>

- Rating-based and multiple-choice voting
- Option comparison using defined comparison factors
- Comparison scoring and option ranking
- Vote statistics, distribution, and participation analysis

</details>

<details>
<summary><strong>💬 Discussions</strong></summary>

- Threaded comments and replies per decision
- Comment pinning where permitted
- Reporting and moderation of inappropriate comments
- Moderation history retained even after comment deletion, for auditability

</details>

<details>
<summary><strong>🚨 Comment Reporting & Moderation</strong></summary>

- Users can report inappropriate comments
- Reported comments are forwarded to the appropriate moderation workflow
- Administrators receive notifications for reported comments
- Administrators and moderators can review reported comments
- Reports can be dismissed
- Inappropriate comments can be deleted
- Deleted comments remain represented in moderation history where applicable
- Active reported-comment counts are updated after moderation actions

</details>

<details>
<summary><strong>👥 Community Management</strong></summary>

- Public and private communities
- Membership and community-assigned moderators
- Community-level decision listing and participation
- Community moderation, reported-comment monitoring, and analytics

</details>

<details>
<summary><strong>🛡️ Moderator Capabilities</strong></summary>

- Monitor activity across assigned communities
- Review community decisions and discussions
- Moderate, delete, or dismiss reported comments
- Pin comments / lock decisions where permitted
- View community analytics

</details>

<details>
<summary><strong>👑 Administrator Capabilities</strong></summary>

- Platform-wide dashboard and analytics (users, communities, decisions, discussions, feedback)
- Community management and moderator/member visibility
- Reported-comment monitoring and notifications
- Notification management and support/feedback review

</details>

<details>
<summary><strong>🔔 Notifications</strong></summary>

- Notification bell with unread count
- Covers moderator assignment, decision events, closures, community activity, and reported comments
- Read/unread handling, polling/refresh, and navigation to related resources

</details>

<details>
<summary><strong>📝 Help & Feedback</strong></summary>

- 💡 Bug reports and suggestions with subject/description
- 📝 General feedback with experience rating
- ⭐ Experience ratings

</details>

---

## 🧩 Core Modules

- 🔐 Authentication & Authorization
- 👤 User Profile Management
- 🗳️ Decision Management
- 📊 Voting & Poll Management
- ⚖️ Option Comparison & Scoring
- 👥 Community Management
- 💬 Discussions & Comments
- 🚨 Comment Reporting & Moderation
- 🔔 Notifications
- 📈 Analytics
- 📝 Help & Feedback
- 👑 Admin Management
- 🛡️ Moderator Management

---

## 📈 Analytics

DecisionHub provides analytics at three levels.

### 🗳️ Decision Analytics

- Overview
- Vote statistics
- Vote distribution
- Participation
- Discussion activity
- Option ranking
- Comparison data

### 👥 Community Analytics

- Community overview
- Community decisions
- Voting activity
- Discussion activity
- Community activity
- Moderation statistics

### 👑 Platform Analytics

Administrators can monitor:

- 👤 Users
- 👥 Communities
- 🗳️ Decisions
- 💬 Discussions
- 📝 Feedback
- 📊 Overall platform statistics

---

## 🔐 Security & Access Control

DecisionHub uses **JWT authentication** and **role-based access control (RBAC)**.

| Role | Responsibilities |
|---|---|
| 👤 **USER** | Create and participate in decisions, communities, voting, and discussions |
| 🛡️ **MODERATOR** | Manage and moderate assigned communities |
| 👑 **ADMIN** | Monitor and manage the platform at an administrative level |

Protected backend APIs are secured using **Spring Security** and JWT-based authorization.

---

## 🏗️ System Architecture

```text
┌──────────────────────────────────────┐
│              FRONTEND                │
│          React.js + Vite             │
│        JavaScript + CSS              │
│                                       │
│             Netlify ☁️               │
└──────────────────┬────────────────────┘
                    │
                    │ REST APIs
                    ▼
┌──────────────────────────────────────┐
│               BACKEND                │
│          Java + Spring Boot          │
│       Spring Security + JWT          │
│             REST APIs                │
│                                       │
│              Render ☁️               │
└──────────────────┬────────────────────┘
                    │
                    │ JPA / Database
                    ▼
┌──────────────────────────────────────┐
│              DATABASE                │
│              PostgreSQL              │
│                                       │
│         Render PostgreSQL ☁️         │
└──────────────────────────────────────┘
```

### Backend Architecture

DecisionHub's backend follows a layered Spring Boot architecture:

```text
Controller
    ↓
Service Interface
    ↓
Service Implementation
    ↓
Repository
    ↓
PostgreSQL Database
```

This separation keeps API handling, business logic, data access, and persistence responsibilities organized.

---

## 📂 Project Structure

```
DecisionHub/
│
├── backend/
│   ├── src/main/java/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── security/
│   │   └── ...
│   │
│   └── src/main/resources/
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── context/
│   │   └── styles/
│   │
│   └── package.json
│
├── postman/
├── DailyWork/
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven
- Node.js 18+
- npm
- PostgreSQL

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/keerthana12hv/DecisionHub.git
cd DecisionHub
```

### 2️⃣ Run the Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend runs on `http://localhost:8080`.

**Windows:**

```powershell
.\mvnw.cmd spring-boot:run
```

### 3️⃣ Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on `http://localhost:5173`.

> Configure the required database credentials and JWT configuration (e.g. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`) in the backend environment before running the application.

---

## ☁️ Deployment

DecisionHub is deployed using cloud services:

| Component | Platform |
|---|---|
| 🎨 Frontend | Netlify |
| ⚙️ Backend | Render |
| 🗄️ PostgreSQL Database | Render |

### 🌐 Live Application

**DecisionHub:** [https://decisionhub-app.netlify.app/login](https://decisionhub-app.netlify.app/login)

---

## 🧪 API Testing

The API is tested with Postman, using JWT-based auth token handling and request collections organized around distinct user roles (owner vs. member) to verify role-based access control.

---

## 👥 Team

DecisionHub was developed collaboratively as a team project.

| Team Member | GitHub |
|---|---|
| Keerthana J K | [@keerthana12hv](https://github.com/keerthana12hv) |
| Chirag B K | [@ChiragBK1012](https://github.com/ChiragBK1012) |
| Jyothi | [@jyotihanagandj00-sketch](https://github.com/jyotihanagandj00-sketch) |
| Kaviyaruba | [@Kaviyaruba110](https://github.com/Kaviyaruba110) |
| Kowsalya Nachimuthu | [@KowsalyaNachimuthu](https://github.com/KowsalyaNachimuthu) |
| Mathumitha | [@Mathumitha03](https://github.com/Mathumitha03) |
| Prasanna Sangou | [@Prasanna-Sangou](https://github.com/Prasanna-Sangou) |
| Priyanka Patil | [@PriyaPatil25](https://github.com/PriyaPatil25) |
| Sriram | [@sreeramslsd](https://github.com/sreeramslsd) |
| Mythili Sunkisala | [@Sunkisala-2379](https://github.com/Sunkisala-2379) |

### 💻 My Contribution

As a team member, I contributed primarily to:

- Backend development using Java and Spring Boot
- Database and repository implementation
- Community management functionality
- Comment reporting and moderation workflow
- Admin-side reported comment tracking
- Notification integration for reported comments
- Frontend/backend integration and testing
- Git/GitHub collaboration and feature integration

### 💻 Team Collaboration

The project was developed using a collaborative Git/GitHub workflow.

- 🌿 Feature-based Git branches
- 🔀 Pull requests and code integration
- 🧪 Backend and frontend testing
- 📮 API testing using Postman
- 👥 Collaborative development
- 🚀 Continuous deployment through the main branch

---

## 🎯 Project Highlights

DecisionHub brings together several important full-stack concepts into one application:

🔐 Authentication → 👥 Communities → 🗳️ Decisions → ⚖️ Comparison → 🗳️ Voting → 💬 Discussions → 🛡️ Moderation → 🔔 Notifications → 📊 Analytics

This makes DecisionHub more than a basic polling application — it is a complete collaborative decision-making platform.

---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add your feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## 📜 License

This project is developed as an academic/internship team project.

---

<div align="center">

### 🚀 DecisionHub

**Make decisions together. Compare ideas. Vote. Analyze. Decide.**

[🌐 Try DecisionHub Live](https://decisionhub-app.netlify.app/login)

⭐ If you find the project interesting, consider giving the repository a star!

</div>

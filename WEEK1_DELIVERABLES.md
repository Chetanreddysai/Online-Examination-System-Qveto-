# Qveto Online Examination System — Week 1 Engineering Deliverables

## 1. Overview & System Context
The **Qveto Online Examination System** (Project 3) is a robust, desktop-based assessment platform built with **Java Swing** and **MySQL**. It enables administrators to author reusable question banks and administer timed examinations, while students securely take assessments with automatic countdowns, real-time answer persistence, and automated scoring.

---

## 2. System Architecture & Layered Package Structure
Following strict separation of concerns, the application is organized into modular layers:

```
src/main/java/com/qveto/exam/
├── Main.java               # Application Bootstrap & EDT Initialization
├── model/                  # POJOs / Entities
│   ├── User.java           # Admin & Student user records
│   ├── Role.java           # ADMIN, STUDENT enum
│   ├── Exam.java           # Examination configuration entity
│   ├── Question.java       # Multiple-choice question bank entity
│   ├── ExamAttempt.java    # Candidate test attempt session
│   ├── AttemptStatus.java  # IN_PROGRESS, SUBMITTED, TIMED_OUT, ABANDONED
│   ├── SubmissionType.java # MANUAL, AUTO_TIMEOUT
│   ├── AttemptAnswer.java  # Persistent option selection
│   └── ExamResult.java     # Graded evaluation and performance metric
├── dao/                    # JDBC Data Access Layer (PreparedStatements)
│   ├── UserDAO.java        # Secure authentication & user CRUD
│   ├── ExamDAO.java        # Examination queries & creation
│   ├── QuestionDAO.java    # Reusable question bank management
│   ├── AttemptDAO.java     # Session tracking & live answer persistence
│   └── ResultDAO.java      # Score persistence and grade reports
├── service/                # Business Logic & Timing Rules
│   ├── AuthService.java    # Authentication, password checks & offline demo fallback
│   └── ExamService.java    # Exam distribution, grading rules & attempt initialization
├── validation/             # Input Validation
│   ├── InputValidator.java # Email regex, password length, boundaries
│   └── ValidationResult.java # Error collection and status object
├── util/                   # Shared Infrastructure & Helpers
│   ├── DBConnection.java   # Externalized JDBC loader with diagnostic polling
│   ├── PasswordUtil.java   # SHA-256 secure password hashing & verification
│   ├── SessionManager.java # Thread-safe active user session container
│   └── AppConstants.java   # Theme colors, typography, CardLayout constants
└── ui/                     # Java Swing Presentation Layer
    ├── MainFrame.java      # Top-level window hosting CardLayout navigation
    ├── LoginPanel.java     # Authentication screen with quick-fill demo buttons
    ├── RegisterPanel.java  # Student account self-registration
    ├── AdminDashboardPanel.java   # Week 1 Admin overview & exam management shell
    └── StudentDashboardPanel.java # Week 1 Student portal & instructions shell
```

---

## 3. Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    USERS ||--o{ EXAM_ATTEMPTS : attempts
    USERS ||--o{ EXAM_RESULTS : achieves
    EXAMS ||--|{ EXAM_QUESTIONS : contains
    QUESTIONS ||--|{ EXAM_QUESTIONS : mapped_to
    EXAMS ||--o{ EXAM_ATTEMPTS : instantiated_by
    EXAMS ||--o{ EXAM_RESULTS : evaluates
    EXAM_ATTEMPTS ||--|{ ATTEMPT_ANSWERS : records
    QUESTIONS ||--o{ ATTEMPT_ANSWERS : answered_for
    EXAM_ATTEMPTS ||--|| EXAM_RESULTS : generates

    USERS {
        int user_id PK
        string full_name
        string email UK
        string password_hash
        enum role "ADMIN, STUDENT"
        string phone
        timestamp created_at
    }

    EXAMS {
        int exam_id PK
        string title
        text description
        int duration_minutes
        int total_questions
        decimal total_marks
        decimal passing_percentage
        boolean is_active
        timestamp created_at
    }

    QUESTIONS {
        int question_id PK
        string subject
        text question_text
        text option_a
        text option_b
        text option_c
        text option_d
        enum correct_option "A, B, C, D"
        decimal marks
        timestamp created_at
    }

    EXAM_QUESTIONS {
        int exam_id PK, FK
        int question_id PK, FK
        int question_order
    }

    EXAM_ATTEMPTS {
        int attempt_id PK
        int user_id FK
        int exam_id FK
        timestamp start_time
        timestamp end_time
        enum submission_type "MANUAL, AUTO_TIMEOUT"
        enum status "IN_PROGRESS, SUBMITTED, TIMED_OUT, ABANDONED"
        decimal score
        decimal percentage
        boolean passed
    }

    ATTEMPT_ANSWERS {
        int answer_id PK
        int attempt_id FK
        int question_id FK
        enum selected_option "A, B, C, D"
        boolean is_correct
        timestamp answered_at
    }

    EXAM_RESULTS {
        int result_id PK
        int attempt_id FK, UK
        int user_id FK
        int exam_id FK
        int total_questions
        int answered_count
        int correct_count
        int incorrect_count
        decimal score
        decimal total_marks
        decimal percentage
        boolean passed
        timestamp graded_at
    }
```

---

## 4. Exam Attempt Lifecycle & Session State Diagram

```mermaid
stateDiagram-v2
    [*] --> Idle: Candidate views Available Exams
    Idle --> IN_PROGRESS: Click "Start Exam" (Attempt Created)
    
    state IN_PROGRESS {
        [*] --> DisplayQuestion
        DisplayQuestion --> SaveSelection: Candidate Selects Option A/B/C/D
        SaveSelection --> DisplayQuestion: Next / Previous Question Navigation
        DisplayQuestion --> CountdownActive: javax.swing.Timer ticks every 1000ms
    }

    IN_PROGRESS --> SUBMITTED: Confirmed Manual Submission
    IN_PROGRESS --> TIMED_OUT: Timer reaches 00:00 (Auto-Submission)
    IN_PROGRESS --> ABANDONED: Abnormal Window Closure / Interruption

    SUBMITTED --> Graded: Grade Calculation & Immediate Score Persistence
    TIMED_OUT --> Graded: Grade Calculation & Immediate Score Persistence
    Graded --> ResultDisplay: Display Score, Percentage, & Pass/Fail Status
    ResultDisplay --> [*]
```

---

## 5. Week 1 Implementation Status
- [x] **Project Skeleton:** Maven standard directory layout (`src/main/java`, `src/main/resources`, `src/test/java`, `database`).
- [x] **Database Schema:** Production-grade DDL in `database/schema.sql` covering users, exams, reusable question bank, mappings, attempts, answers, and results.
- [x] **Synthetic Seed Data:** `database/seed.sql` with default admin, student accounts, sample MCQs across subjects, and exams.
- [x] **Secure Persistence:** PreparedStatements everywhere in DAO layer; SHA-256 password hashing in `PasswordUtil`.
- [x] **CardLayout Navigation:** `MainFrame` managing seamless switching between Login, Register, Admin Dashboard, and Student Dashboard.
- [x] **Input Validation:** Centralized boundary checks for names, email regex, passwords, and questions in `InputValidator`.
- [x] **Zero-Dependency Run & Test:** `run.bat` and `test.bat` for one-click compilation, execution, and verification.
- [x] **Automated Tests:** 20 unit tests covering password security, input boundaries, entity models, and authentication workflows.

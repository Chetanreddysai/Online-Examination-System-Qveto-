# Frontend Architecture & UI Documentation
**Project 3: Online Examination System**  
**Role:** Frontend Developer (UI / UX / Swing Architecture)  
**Team Repository:** [Hack44/Qveto-Java-Team-Project-02](https://github.com/Hack44/Qveto-Java-Team-Project-02)

---

## 1. Executive Summary & Frontend Scope
In this multi-member Java project, the **Frontend Developer** owns the entire Presentation Layer (`com.qveto.exam.ui`), application routing (`CardLayout`), responsive component design, theme system (`AppConstants`), form validation feedback, visible countdown timers (`javax.swing.Timer`), and user interaction workflows.

The frontend is built natively with **Java Swing** adhering strictly to the specifications of the Qveto Java Development Internship (Project 3). It communicates cleanly with the business logic and persistence layers via `AuthService` and `ExamService`, ensuring strict separation of concerns.

---

## 2. UI Component Hierarchy & File Ownership

The frontend codebase is contained within `src/main/java/com/qveto/exam/ui/` and supported by `util/AppConstants.java`:

```
src/main/java/com/qveto/exam/
├── Main.java                        # App Entry Point (Swing EDT invokeLater)
├── util/
│   └── AppConstants.java            # UI Theme, Hex Colors, Fonts, Card Identifiers
└── ui/
    ├── MainFrame.java               # Top-level window hosting CardLayout controller
    ├── LoginPanel.java              # Sign-In view with quick demo credential buttons & DB status
    ├── RegisterPanel.java           # Student registration view with field validations
    ├── StudentDashboardPanel.java   # Student portal: Available exams, history table, profile
    ├── AdminDashboardPanel.java     # Admin portal: Exam CRUD, Question Bank, Results Analytics
    ├── CreateExamDialog.java        # Modal form: Admin exam creation with boundary checks
    ├── AddQuestionDialog.java       # Modal form: MCQ authoring with 4 options & correct key
    ├── ExamDetailsDialog.java       # Modal dialog: Pre-exam syllabus, duration & rules
    ├── UserProfileDialog.java       # Modal dialog: Display profile info & password change
    ├── TimedExamPanel.java          # Core Exam Engine: Visible countdown, 1-question view, palette
    ├── ExamResultPanel.java         # Post-submission scorecard & question review table
    └── AttemptReviewDialog.java     # Modal breakdown dialog for historical exam attempts
```

---

## 3. Screen-by-Screen Frontend Specification

### 3.1. Main Window & Navigation (`MainFrame.java`)
- **Container Pattern:** Implements `CardLayout` across a central `JPanel` (`cardsPanel`).
- **Screen Switching:** Provides centralized `showCard(String cardName)` switching between `CARD_LOGIN`, `CARD_REGISTER`, `CARD_ADMIN_DASHBOARD`, `CARD_STUDENT_DASHBOARD`, `CARD_TIMED_EXAM`, and `CARD_RESULT`.
- **Window Sizing:** Configured with preferred dimension `1024x680` and minimum dimension `860x580`, centered on the screen.

### 3.2. Authentication (`LoginPanel.java` & `RegisterPanel.java`)
- **Modern Flat Card Design:** Uses elevated white card with subtle borders over a clean background (`#F8FAFC`).
- **Input Validation:** Real-time checking for required fields, email syntax (`user@domain.ext`), and password minimums.
- **Connection Diagnostics:** Dynamic status badge displaying whether the local MySQL instance is connected or operating in graceful Standalone/Demo mode.
- **Quick-Fill Buttons:** One-click fill buttons for `Admin` and `Student` credentials for rapid review and grading.

### 3.3. Student Candidate Dashboard (`StudentDashboardPanel.java`)
- **Header:** Displays logged-in student name, email chip, and "Profile / Password" and "Logout" controls.
- **Available Exams Catalog:** `JTable` rendering active exams with Title, Subject, Question Count, Total Marks, and Duration.
- **Action Controls:** "View Instructions" (opens modal dialog) and "Start Exam" (launches timed session).
- **Assessment History:** Shows student's past attempts, score obtained, percentage, pass/fail badge, and a "Review" button.

### 3.4. Timed Examination Engine (`TimedExamPanel.java`)
- **One-Question-at-a-Time View:** Large formatted question prompt with marks badge.
- **Radio Option Selection:** 4 stylized `JRadioButton` choices within a unified `ButtonGroup`.
- **Live Countdown Timer:** Driven by `javax.swing.Timer` (1-second tick), dynamically changing colors:
  - **Normal (> 2 min):** Soft green badge (`#10B981`)
  - **Warning (1 - 2 min):** Warning amber badge (`#F59E0B`)
  - **Critical (< 1 min):** Urgent red badge (`#EF4444`) with countdown warning.
- **Interactive Question Palette:** Grid of numbered buttons representing every question:
  - **Gray:** Unvisited question
  - **Blue:** Current active question
  - **Green:** Answered / Option selected
- **Answer Persistence:** Switching between Next/Previous or jumping via Palette saves the selected answer immediately into memory/session.
- **Dual Submission Controls:**
  - **Manual Submission:** Button with confirmation dialog ("Are you sure you want to submit?").
  - **Auto-Submission:** Automatically triggers and grades the paper when the timer expires to 00:00.

### 3.5. Result & Review Panels (`ExamResultPanel.java` & `AttemptReviewDialog.java`)
- **Scorecard Header:** Displays Total Score, Maximum Marks, Percentage (formatted to 1 decimal place), and a prominent Pass/Fail status banner.
- **Question Review Table:** Shows Question Number, Candidate Answer, and Result status without leaking the exam answer key prematurely.
- **Navigation Action:** "Return to Dashboard" button resetting the candidate session.

### 3.6. Administrator Dashboard & Dialogs (`AdminDashboardPanel.java`, etc.)
- **Tabbed Interface:**
  1. *Exams Tab:* Table of all configured exams, "Create New Exam", and "Toggle Active/Inactive" actions.
  2. *Question Bank Tab:* Master bank of MCQs categorized by subject with "Add Question" modal.
  3. *Results & Analytics Tab:* Filterable table of all candidate submissions, search by student name, filter by exam, completion statistics, and "Export CSV" feature.

---

## 4. UI Theme & Design Tokens (`AppConstants.java`)

| Token | Hex Value | Purpose |
| :--- | :--- | :--- |
| `COLOR_PRIMARY` | `#1E3A8A` | Deep Navy - Primary brand headers & action buttons |
| `COLOR_SECONDARY` | `#0284C7` | Sky Blue - Navigation bars, chips, and accents |
| `COLOR_SUCCESS` | `#10B981` | Emerald Green - Pass badges, timer normal, answered palette |
| `COLOR_DANGER` | `#EF4444` | Crimson Red - Fail badges, timer alert, destructive actions |
| `COLOR_WARNING` | `#F59E0B` | Amber - Warning alerts, timer urgent stage |
| `COLOR_BACKGROUND` | `#F1F5F9` | Slate Gray - Main window backdrop |
| `COLOR_CARD_BG` | `#FFFFFF` | Pure White - Elevated card panels |
| `COLOR_BORDER` | `#E2E8F0` | Subtle Border Gray - Form field and table borders |

---

## 5. How to Run & Verify the Frontend

### Quick Launch (Batch Script):
```cmd
run.bat
```

### Manual Compilation & Execution via Terminal:
```cmd
javac -encoding UTF-8 -d bin src/main/java/com/qveto/exam/model/*.java src/main/java/com/qveto/exam/util/*.java src/main/java/com/qveto/exam/validation/*.java src/main/java/com/qveto/exam/dao/*.java src/main/java/com/qveto/exam/service/*.java src/main/java/com/qveto/exam/ui/*.java src/main/java/com/qveto/exam/Main.java

java -cp "bin;src/main/resources" com.qveto.exam.Main
```

### Running Automated Frontend & Workflow Tests:
```cmd
test.bat
```
*(Executes all 37 automated test cases verifying authentication, exam navigation, timer mechanics, and scoring).*

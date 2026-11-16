# Qveto Online Examination System — Week 2 Engineering Deliverables

## 1. Overview & Objectives (Week 2 Milestone)
In accordance with the **Project 3: Online Examination System** roadmap, Week 2 focuses on:
- **Admin Exam CRUD & Configuration**: Authoring new examination papers with custom titles, duration, question count, maximum marks, and pass percentages.
- **Reusable Question Bank Management**: Authoring multiple-choice questions with 4 options, 1 verified correct answer, marks allocation, and cross-exam reuse.
- **Candidate Profile & Account Security**: Self-service profile updates (display name, contact phone) and secure password change workflows with old-password verification.
- **Syllabus & Guidelines Pre-Exam Review**: Dedicated candidate preview before entering timed test environments.

---

## 2. Implemented Components & Screens

```
src/main/java/com/qveto/exam/
├── ui/
│   ├── CreateExamDialog.java     # Admin modal to create and publish exams
│   ├── AddQuestionDialog.java     # Admin modal to author MCQs for reusable bank
│   ├── UserProfileDialog.java     # Candidate & Admin profile & password update modal
│   ├── ExamDetailsDialog.java     # Modal previewing exam syllabus, rules & criteria
│   ├── AdminDashboardPanel.java   # Enhanced with action toolbars for exams & question bank
│   └── StudentDashboardPanel.java # Enhanced with profile access & syllabus preview
├── service/
│   ├── ExamService.java           # Added createExam, addQuestionToBank, toggleExamStatus, getAllQuestionBank
│   └── AuthService.java           # Added updateProfile, updatePassword with verification
└── dao/
    ├── UserDAO.java               # Added updateProfile(userId, fullName, phone)
    ├── ExamDAO.java               # Added toggleActive(examId, isActive)
    └── QuestionDAO.java           # Added deleteQuestion(questionId)
```

---

## 3. Workflow & Verification Details

### A. Admin Exam Authoring Workflow
1. Navigate to **Admin Dashboard -> Examination Papers**.
2. Click **`+ Create New Exam`**.
3. Fill in:
   - Exam Title
   - Description / Instructions
   - Duration (5 - 300 minutes)
   - Total Questions
   - Total Marks & Passing Percentage
4. Input validation enforces positive duration, non-empty title, and valid percentage range ($0 \le \text{passPct} \le 100$).
5. Instantly saves to MySQL (or synchronized in-memory fallback) and updates dashboard metrics.

### B. Question Bank Authoring & Reuse Workflow
1. Navigate to **Admin Dashboard -> Question Bank**.
2. Click **`+ Add Question to Bank`**.
3. Select or enter Subject category (Java Programming, Database Systems, Computer Networks, etc.).
4. Enter question statement and 4 distinct options (Option A, B, C, D).
5. Designate the correct option (`A`, `B`, `C`, or `D`) and allocate marks.
6. Optionally associate with an active exam paper, or save to the central reusable repository for cross-exam assignment.

### C. Student Profile & Password Workflow
1. Candidate logs in and clicks **`👤 My Profile & Security`**.
2. **Profile Tab**: Edit display name or mobile contact number.
3. **Change Password Tab**: Enter current password, new password (min. 6 characters), and confirmation. Verifies existing hash before updating.

---

## 4. Test Verification
All 27 automated unit tests across 5 test suites executed successfully:
- `PasswordUtilTest`: 5 tests passed
- `InputValidatorTest`: 6 tests passed
- `ModelTest`: 4 tests passed
- `AuthServiceTest`: 5 tests passed
- `ExamServiceTest`: 7 tests passed
- **Total: 27 passed, 0 failed.**

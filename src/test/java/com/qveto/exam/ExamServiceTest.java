package com.qveto.exam;

import com.qveto.exam.model.Exam;
import com.qveto.exam.model.Question;
import com.qveto.exam.model.Role;
import com.qveto.exam.model.User;
import com.qveto.exam.service.AuthService;
import com.qveto.exam.service.ExamService;
import com.qveto.exam.util.SessionManager;
import com.qveto.exam.validation.ValidationResult;

import java.util.List;

import static com.qveto.exam.Assert.*;

public class ExamServiceTest {

    private ExamService examService;
    private AuthService authService;

    public void setUp() {
        examService = new ExamService();
        authService = new AuthService();
        SessionManager.logout();
    }

    public void testCreateExamSuccess() {
        setUp();
        Exam exam = new Exam(0, "Data Structures Assessment", "Stacks, Queues, and Binary Trees", 45, 15, 100.0, 45.0, true);
        ValidationResult result = examService.createExam(exam);
        assertTrue(result.isValid(), "Exam creation with valid data should succeed");

        List<Exam> exams = examService.getAvailableExams();
        boolean found = false;
        for (Exam e : exams) {
            if ("Data Structures Assessment".equals(e.getTitle())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Newly created exam must appear in available exams");
    }

    public void testCreateExamValidationFailure() {
        setUp();
        Exam invalidExam = new Exam(0, "", "No title", -10, 0, -50.0, 120.0, true);
        ValidationResult result = examService.createExam(invalidExam);
        assertFalse(result.isValid(), "Exam creation with invalid fields should fail");
        assertTrue(result.getErrors().size() >= 3, "Multiple validation errors should be collected");
    }

    public void testAddQuestionToBank() {
        setUp();
        Question q = new Question(0, "Algorithms", "What is the time complexity of binary search on a sorted array?",
                "O(1)", "O(log n)", "O(n)", "O(n^2)", "B", 10.0);
        ValidationResult result = examService.addQuestionToBank(q, null);
        assertTrue(result.isValid(), "Adding valid question to bank should succeed");

        List<Question> bank = examService.getAllQuestionBank();
        boolean found = false;
        for (Question item : bank) {
            if (item.getQuestionText().contains("binary search")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Question must be present in the central question bank");
    }

    public void testAddQuestionValidationFailure() {
        setUp();
        Question invalidQ = new Question(0, "", "", "", "", "", "", "Z", -5.0);
        ValidationResult result = examService.addQuestionToBank(invalidQ, null);
        assertFalse(result.isValid(), "Invalid question must fail validation");
    }

    public void testToggleExamStatus() {
        setUp();
        List<Exam> exams = examService.getAvailableExams();
        assertFalse(exams.isEmpty(), "Available exams must not be empty");

        Exam first = exams.get(0);
        boolean initialStatus = first.isActive();

        boolean toggled = examService.toggleExamStatus(first.getExamId());
        assertTrue(toggled, "Toggle status must return true for valid exam id");
        assertEquals(!initialStatus, first.isActive(), "Exam active status should be inverted");

        // Toggle back to preserve state
        examService.toggleExamStatus(first.getExamId());
        assertEquals(initialStatus, first.isActive());
    }

    public void testUpdateProfileWorkflow() {
        setUp();
        User student = authService.login("student1@qveto.com", "Student@123");
        assertNotNull(student);

        ValidationResult res = authService.updateProfile(student.getUserId(), "Aarav P. Updated", "+91 99999 11111");
        assertTrue(res.isValid(), "Profile update should succeed");
        assertEquals("Aarav P. Updated", SessionManager.getCurrentUser().getFullName());
    }

    public void testUpdatePasswordWorkflow() {
        setUp();
        User student = authService.login("student2@qveto.com", "Student@123");
        assertNotNull(student);

        // Fail wrong old password
        ValidationResult failOld = authService.updatePassword(student.getUserId(), "WrongOldPass", "NewSecret@123", "NewSecret@123");
        assertFalse(failOld.isValid(), "Incorrect old password must fail");

        // Fail password mismatch
        ValidationResult failMismatch = authService.updatePassword(student.getUserId(), "Student@123", "NewSecret@123", "Mismatch@123");
        assertFalse(failMismatch.isValid(), "Password mismatch must fail");

        // Success password change
        ValidationResult success = authService.updatePassword(student.getUserId(), "Student@123", "NewSecret@123", "NewSecret@123");
        assertTrue(success.isValid(), "Valid password change must succeed");
    }
}

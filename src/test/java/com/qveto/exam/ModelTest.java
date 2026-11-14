package com.qveto.exam;

import com.qveto.exam.model.*;
import static com.qveto.exam.Assert.*;

public class ModelTest {

    public void testUserModel() {
        User user = new User(10, "Test Student", "test@qveto.com", Role.STUDENT, "+91 99999 88888");
        assertEquals(10, user.getUserId());
        assertEquals("Test Student", user.getFullName());
        assertEquals("test@qveto.com", user.getEmail());
        assertEquals(Role.STUDENT, user.getRole());
        assertTrue(user.isStudent());
        assertFalse(user.isAdmin());
    }

    public void testExamModel() {
        Exam exam = new Exam(1, "Java Test", "A quick quiz", 30, 10, 100.0, 50.0, true);
        assertEquals("Java Test", exam.getTitle());
        assertEquals(30, exam.getDurationMinutes());
        assertEquals(10, exam.getTotalQuestions());
        assertEquals(100.0, exam.getTotalMarks());
        assertTrue(exam.isActive());
    }

    public void testQuestionModel() {
        Question q = new Question(1, "Java", "What is an interface?", "A", "B", "C", "D", "A", 10.0);
        assertEquals("Java", q.getSubject());
        assertEquals("A", q.getCorrectOption());
        assertEquals("A", q.getOptionText("A"));
    }

    public void testExamAttemptModel() {
        ExamAttempt attempt = new ExamAttempt(1, 2, 1, new java.sql.Timestamp(System.currentTimeMillis()), AttemptStatus.IN_PROGRESS);
        assertEquals(AttemptStatus.IN_PROGRESS, attempt.getStatus());
        assertFalse(attempt.isCompleted());

        attempt.setStatus(AttemptStatus.SUBMITTED);
        assertTrue(attempt.isCompleted());
    }
}

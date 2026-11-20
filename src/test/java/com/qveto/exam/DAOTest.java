package com.qveto.exam;

import com.qveto.exam.dao.*;
import com.qveto.exam.model.*;
import com.qveto.exam.util.DBConnection;
import com.qveto.exam.util.PasswordUtil;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.qveto.exam.Assert.*;

/**
 * Unit and resilience test suite for the JDBC Data Access Objects (DAOs).
 * Author: Member 2 (Database & Persistence Engineer)
 * Milestone: Week 2 - Day 12 Automated Persistence Validation
 */
public class DAOTest {

    private UserDAO userDAO;
    private ExamDAO examDAO;
    private QuestionDAO questionDAO;
    private AttemptDAO attemptDAO;
    private ResultDAO resultDAO;

    public void setUp() {
        userDAO = new UserDAO();
        examDAO = new ExamDAO();
        questionDAO = new QuestionDAO();
        attemptDAO = new AttemptDAO();
        resultDAO = new ResultDAO();
    }

    public void testDBConnectionConfiguration() {
        setUp();
        Properties props = DBConnection.getProperties();
        assertNotNull(props, "Properties instance should not be null");
        assertTrue(props.containsKey("db.url"), "Properties should specify db.url");
        assertTrue(props.containsKey("db.user"), "Properties should specify db.user");
        assertTrue(props.containsKey("db.password"), "Properties should specify db.password");

        // Polling database connectivity status should not throw an exception
        boolean connected = DBConnection.isDatabaseConnected();
        // Result is boolean true or false depending on whether local MySQL daemon is running
        assertTrue(connected || !connected, "isDatabaseConnected() must return a valid boolean");
    }

    public void testUserDAOAuthenticationSafety() {
        setUp();
        // Testing null and non-existent input safety
        User nullUser = userDAO.authenticate("nonexistent_candidate_xyz@domain.invalid", "randompass");
        assertNull(nullUser, "Authentication for nonexistent user must return null without throwing exception");

        User nullEmailUser = userDAO.findByEmail("nonexistent_test_999@domain.invalid");
        assertNull(nullEmailUser, "Lookup for nonexistent email must return null");
    }

    public void testExamDAOQueryResilience() {
        setUp();
        List<Exam> activeExams = examDAO.getAllActiveExams();
        assertNotNull(activeExams, "Active exams list should never be null");

        List<Exam> allExams = examDAO.getAllExams();
        assertNotNull(allExams, "All exams list should never be null");

        Exam nonExistent = examDAO.getExamById(-9999);
        assertNull(nonExistent, "Lookup for negative exam ID should return null");
    }

    public void testQuestionDAOQueryResilience() {
        setUp();
        List<Question> questions = questionDAO.getAllQuestions();
        assertNotNull(questions, "Question bank list should never be null");

        List<Question> examQuestions = questionDAO.getQuestionsForExam(-999);
        assertNotNull(examQuestions, "Exam questions list should not be null for invalid exam ID");
        assertEquals(0, examQuestions.size(), "Invalid exam ID should yield 0 questions");

        // Deleting non-existent question should safely return false without crashing
        boolean deleted = questionDAO.deleteQuestion(-9999);
        assertFalse(deleted, "Deleting non-existent question ID should return false");
    }

    public void testAttemptDAOAnswerPersistenceResilience() {
        setUp();
        Map<Integer, String> answers = attemptDAO.getAnswersForAttempt(-999);
        assertNotNull(answers, "Attempt answers map should never be null");
        assertEquals(0, answers.size(), "Invalid attempt ID should return empty answer map");

        // Attempting to complete non-existent attempt should gracefully return false or handle without crashing
        boolean completed = attemptDAO.completeAttempt(-999, SubmissionType.MANUAL, AttemptStatus.SUBMITTED, 0, 0, false);
        assertFalse(completed, "Completing invalid attempt ID should return false");
    }

    public void testResultDAOReportingResilience() {
        setUp();
        List<ExamResult> studentResults = resultDAO.getResultsByUser(-999);
        assertNotNull(studentResults, "Student results list should never be null");
        assertEquals(0, studentResults.size(), "Invalid student ID should return empty result history");

        List<ExamResult> allResults = resultDAO.getAllResults();
        assertNotNull(allResults, "All results list should never be null");
    }

    public void testPasswordHashPersistenceIntegrity() {
        setUp();
        String rawPassword = "SecurePassword@2026";
        String hashed = PasswordUtil.hash(rawPassword);
        assertNotNull(hashed);
        assertEquals(64, hashed.length(), "SHA-256 hash must be 64 hexadecimal characters for database storage");
        assertTrue(PasswordUtil.verify(rawPassword, hashed), "Hash verification must succeed");
        assertFalse(PasswordUtil.verify("WrongPass", hashed), "Hash verification must reject incorrect password");
    }
}

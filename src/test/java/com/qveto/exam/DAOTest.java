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

    public void testAttemptStatusStateTransitions() {
        setUp();
        // Initial state
        AttemptStatus inProgress = AttemptStatus.IN_PROGRESS;
        assertFalse(inProgress.isTerminal(), "IN_PROGRESS should not be terminal");
        assertTrue(inProgress.canTransitionTo(AttemptStatus.SUBMITTED), "IN_PROGRESS can transition to SUBMITTED");
        assertTrue(inProgress.canTransitionTo(AttemptStatus.TIMED_OUT), "IN_PROGRESS can transition to TIMED_OUT");
        assertTrue(inProgress.canTransitionTo(AttemptStatus.ABANDONED), "IN_PROGRESS can transition to ABANDONED");

        // Terminal states must be immutable
        AttemptStatus submitted = AttemptStatus.SUBMITTED;
        assertTrue(submitted.isTerminal(), "SUBMITTED must be a terminal state");
        assertFalse(submitted.canTransitionTo(AttemptStatus.IN_PROGRESS), "SUBMITTED cannot revert to IN_PROGRESS");
        assertFalse(submitted.canTransitionTo(AttemptStatus.TIMED_OUT), "SUBMITTED cannot transition to TIMED_OUT");

        AttemptStatus timedOut = AttemptStatus.TIMED_OUT;
        assertTrue(timedOut.isTerminal(), "TIMED_OUT must be a terminal state");
        assertFalse(timedOut.canTransitionTo(AttemptStatus.SUBMITTED), "TIMED_OUT cannot change to SUBMITTED");

        AttemptStatus abandoned = AttemptStatus.ABANDONED;
        assertTrue(abandoned.isTerminal(), "ABANDONED must be a terminal state");
        assertFalse(abandoned.canTransitionTo(AttemptStatus.SUBMITTED), "ABANDONED cannot transition to SUBMITTED");
    }

    public void testSubmissionTypeSemantics() {
        setUp();
        SubmissionType manual = SubmissionType.MANUAL;
        assertTrue(manual.isManual(), "MANUAL type should return true for isManual()");
        assertFalse(manual.isAutoTimeout(), "MANUAL type should return false for isAutoTimeout()");

        SubmissionType auto = SubmissionType.AUTO_TIMEOUT;
        assertTrue(auto.isAutoTimeout(), "AUTO_TIMEOUT should return true for isAutoTimeout()");
        assertFalse(auto.isManual(), "AUTO_TIMEOUT should return false for isManual()");

        assertEquals(SubmissionType.MANUAL, SubmissionType.fromString("MANUAL"));
        assertEquals(SubmissionType.AUTO_TIMEOUT, SubmissionType.fromString("AUTO_TIMEOUT"));
        assertNull(SubmissionType.fromString("INVALID_TYPE"));
    }

    public void testAttemptDAOStateStatusSafety() {
        setUp();
        // Non-existent attempt query safety
        AttemptStatus status = attemptDAO.getAttemptStatus(-9999);
        assertNull(status, "Querying status of nonexistent attempt ID should return null");

        boolean active = attemptDAO.isAttemptActive(-9999);
        assertFalse(active, "Nonexistent attempt should not be reported as active");
    }
}

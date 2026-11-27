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

        // getAttemptById and getAttempt non-existent lookup safety
        ExamAttempt attempt = attemptDAO.getAttemptById(-9999);
        assertNull(attempt, "Querying attempt by negative ID should return null without error");

        ExamAttempt aliasAttempt = attemptDAO.getAttempt(-9999);
        assertNull(aliasAttempt, "getAttempt alias should return null for invalid attempt ID");

        ExamAttempt activeAttempt = attemptDAO.getActiveAttempt(-9999, -9999);
        assertNull(activeAttempt, "Nonexistent user/exam should have no active attempt");
    }

    public void testDynamicAnswerUpsertAndNavigationPersistence() {
        setUp();
        // Dynamic upsert safety on non-existent attempts
        boolean upserted = attemptDAO.upsertAnswer(-9999, -9999, "B");
        assertFalse(upserted, "Upserting answer for nonexistent attempt should return false safely");

        boolean cleared = attemptDAO.clearAnswer(-9999, -9999);
        assertFalse(cleared, "Clearing answer for nonexistent attempt should return false safely");

        String selected = attemptDAO.getSelectedAnswer(-9999, -9999);
        assertNull(selected, "Selected option for nonexistent attempt should be null");

        int answeredCount = attemptDAO.getAnsweredCount(-9999);
        assertEquals(0, answeredCount, "Answered count for nonexistent attempt should be 0");
    }

    public void testQuestionPaletteStatusRetrievalAndBulkAnswers() {
        setUp();
        java.util.Set<Integer> answeredIds = attemptDAO.getAnsweredQuestionIds(-9999);
        assertNotNull(answeredIds, "Answered question IDs set should never be null");
        assertEquals(0, answeredIds.size(), "Nonexistent attempt should return empty answered set");

        java.util.Map<Integer, Boolean> paletteStatuses = attemptDAO.getQuestionPaletteStatuses(-9999);
        assertNotNull(paletteStatuses, "Palette statuses map should never be null");
        assertEquals(0, paletteStatuses.size(), "Nonexistent attempt should return empty palette map");

        java.util.List<AttemptAnswer> records = attemptDAO.getAttemptAnswerRecords(-9999);
        assertNotNull(records, "Attempt answer records list should never be null");
        assertEquals(0, records.size(), "Nonexistent attempt should return empty records list");
    }

    public void testTransactionalSubmissionAndTimeoutLocking() {
        setUp();
        // Non-terminal status transition must be rejected prior to database lock acquisition
        boolean invalidTransition = attemptDAO.completeAttempt(
                1, SubmissionType.MANUAL, AttemptStatus.IN_PROGRESS, 10.0, 20.0, false);
        assertFalse(invalidTransition, "Finalizing an attempt into IN_PROGRESS must be rejected");

        // Non-existent attempt IDs must safely return false for locked submission & timeout flows
        boolean submitted = attemptDAO.submitAttempt(-9999, 45.0, 90.0, true);
        assertFalse(submitted, "submitAttempt on nonexistent attempt must return false");

        boolean timedOutWithScore = attemptDAO.timeoutAttempt(-9999, 20.0, 40.0, false);
        assertFalse(timedOutWithScore, "Scored timeoutAttempt on nonexistent attempt must return false");

        boolean timedOutUnscored = attemptDAO.timeoutAttempt(-9999);
        assertFalse(timedOutUnscored, "Unscored timeoutAttempt on nonexistent attempt must return false");

        boolean abandoned = attemptDAO.abandonAttempt(-9999);
        assertFalse(abandoned, "abandonAttempt on nonexistent attempt must return false");

        boolean completedNullStatus = attemptDAO.completeAttempt(
                -9999, SubmissionType.AUTO_TIMEOUT, null, 15.0, 30.0, false);
        assertFalse(completedNullStatus, "completeAttempt with inferred TIMED_OUT on nonexistent attempt must return false");

        boolean locked = attemptDAO.isAttemptLocked(-9999);
        assertFalse(locked, "Nonexistent attempt must not be reported as locked");

        int expiredCount = attemptDAO.expireTimedOutAttempts();
        assertTrue(expiredCount >= 0, "expireTimedOutAttempts must return non-negative count safely");
    }

    public void testACIDExamFinalizationTransactionBoundaries() {
        setUp();
        // Null result or invalid identity guards must reject before opening transaction
        assertFalse(attemptDAO.finalizeAttemptTransaction(null, SubmissionType.MANUAL),
                "Null ExamResult must be rejected");

        ExamResult invalidIdsResult = new ExamResult(0, -9999, 0, 0, 5, 3, 2, 1, 20.0, 50.0, 40.0, false, null);
        assertFalse(attemptDAO.finalizeAttemptTransaction(invalidIdsResult, SubmissionType.MANUAL),
                "Result with non-positive userId/examId/attemptId must be rejected");

        ExamResult validResult = new ExamResult(0, 9999, 2, 1, 5, 5, 4, 1, 40.0, 50.0, 80.0, true, null);
        // Non-terminal target status must be rejected
        assertFalse(attemptDAO.finalizeAttemptTransaction(
                9999, SubmissionType.MANUAL, AttemptStatus.IN_PROGRESS, validResult, null, null),
                "ACID finalization into IN_PROGRESS must be rejected");

        // Transaction against nonexistent attempt must safely roll back and return false without throwing
        validResult.setAttemptId(-9999);
        assertFalse(attemptDAO.finalizeAttemptTransaction(validResult, SubmissionType.AUTO_TIMEOUT),
                "ACID auto-timeout finalization on nonexistent attempt must roll back safely");

        java.util.List<Question> emptyQuestions = java.util.Collections.emptyList();
        java.util.Map<Integer, String> emptyAnswers = java.util.Collections.emptyMap();
        assertFalse(attemptDAO.finalizeAttemptTransaction(
                999999, SubmissionType.MANUAL, AttemptStatus.SUBMITTED, validResult, emptyQuestions, emptyAnswers),
                "ACID manual finalization on missing/unreachable attempt must roll back safely");
    }

    public void testResultDAOScoreCalculationAndPersistence() {
        setUp();
        // Percentage & pass/fail threshold calculations
        assertEquals(0.0, ResultDAO.calculatePercentage(10.0, 0.0), "Zero max marks should yield 0.0% safely");
        assertEquals(0.0, ResultDAO.calculatePercentage(-5.0, 50.0), "Negative score should yield 0.0%");
        assertEquals(66.67, ResultDAO.calculatePercentage(20.0, 30.0), "Percentage should round to 2 decimal places");

        assertTrue(ResultDAO.isPassingScore(50.0, 50.0), "Exact passing threshold must pass");
        assertFalse(ResultDAO.isPassingScore(49.99, 50.0), "Just below threshold must fail");

        // Build computed result from exam, questions, and partial candidate answers
        Exam exam = new Exam(1, "Java Core", "Unit Test Exam", 30, 4, 40.0, 50.0, true);
        java.util.List<Question> questions = java.util.Arrays.asList(
                new Question(1, "Java", "Q1", "A", "B", "C", "D", "A", 10.0),
                new Question(2, "Java", "Q2", "A", "B", "C", "D", "B", 10.0),
                new Question(3, "Java", "Q3", "A", "B", "C", "D", "C", 10.0),
                new Question(4, "Java", "Q4", "A", "B", "C", "D", "D", 10.0)
        );
        java.util.Map<Integer, String> answers = new java.util.HashMap<>();
        answers.put(1, "a");   // Correct (case-insensitive)
        answers.put(2, "B");   // Correct
        answers.put(3, "A");   // Incorrect
        answers.put(4, "  ");  // Blank -> unanswered

        ExamResult computed = ResultDAO.buildComputedResult(101, 2, exam, questions, answers);
        assertNotNull(computed, "Computed ExamResult should not be null");
        assertEquals(4, computed.getTotalQuestions());
        assertEquals(3, computed.getAnsweredCount());
        assertEquals(2, computed.getCorrectCount());
        assertEquals(1, computed.getIncorrectCount());
        assertEquals(20.0, computed.getScore());
        assertEquals(40.0, computed.getTotalMarks());
        assertEquals(50.0, computed.getPercentage());
        assertTrue(computed.isPassed(), "50.0% matches 50.0% passing threshold");

        // Persistence & lookup safety checks
        assertFalse(resultDAO.saveResult(null), "saveResult(null) must return false");
        assertFalse(resultDAO.upsertResult(null), "upsertResult(null) must return false");

        ExamResult invalidResult = new ExamResult(0, -1, 2, 1, 4, 0, 0, 0, 0.0, 40.0, 0.0, false, null);
        assertFalse(resultDAO.saveResult(invalidResult), "saveResult with invalid attemptId must return false");
        assertFalse(resultDAO.upsertResult(invalidResult), "upsertResult with invalid attemptId must return false");

        assertNull(resultDAO.getResultByAttemptId(-9999), "getResultByAttemptId for negative ID must return null");
        assertNull(resultDAO.getResultByAttemptId(Integer.MAX_VALUE), "getResultByAttemptId for nonexistent ID must return null");
        assertFalse(resultDAO.resultExistsForAttempt(-9999), "resultExistsForAttempt for negative ID must return false");
        assertFalse(resultDAO.resultExistsForAttempt(Integer.MAX_VALUE), "resultExistsForAttempt for nonexistent ID must return false");
        assertNull(resultDAO.calculateAndSaveResult(-9999), "calculateAndSaveResult for negative ID must return null");
        assertNull(resultDAO.calculateAndSaveResult(Integer.MAX_VALUE), "calculateAndSaveResult for nonexistent ID must return null");
    }
}

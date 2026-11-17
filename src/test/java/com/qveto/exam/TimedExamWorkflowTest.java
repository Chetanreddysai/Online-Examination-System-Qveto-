package com.qveto.exam;

import com.qveto.exam.model.*;
import com.qveto.exam.service.ExamService;
import com.qveto.exam.util.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.qveto.exam.Assert.*;

public class TimedExamWorkflowTest {

    private ExamService examService;

    public void setUp() {
        examService = new ExamService();
        SessionManager.logout();
    }

    public void testStartAttemptSession() {
        setUp();
        ExamAttempt attempt = examService.startAttempt(2, 1);
        assertNotNull(attempt);
        assertEquals(AttemptStatus.IN_PROGRESS, attempt.getStatus());
        assertNotNull(attempt.getStartTime());
    }

    public void testAnswerPersistence() {
        setUp();
        ExamAttempt attempt = examService.startAttempt(2, 1);
        boolean saved = examService.recordAnswer(attempt.getAttemptId(), 1, "C");
        assertTrue(saved, "Recording answer should succeed");
    }

    public void testManualSubmissionAndScoring() {
        setUp();
        List<Exam> exams = examService.getAvailableExams();
        Exam javaExam = exams.get(0); // 5 questions, 50 marks, 50% pass
        List<Question> questions = examService.getQuestionsForExam(javaExam.getExamId());
        assertEquals(5, questions.size());

        // Answers: 4 correct ('C', 'B', 'B', 'A'), 1 incorrect ('D' instead of 'B')
        Map<Integer, String> studentAnswers = new HashMap<>();
        studentAnswers.put(questions.get(0).getQuestionId(), "C"); // Correct (+10)
        studentAnswers.put(questions.get(1).getQuestionId(), "B"); // Correct (+10)
        studentAnswers.put(questions.get(2).getQuestionId(), "B"); // Correct (+10)
        studentAnswers.put(questions.get(3).getQuestionId(), "A"); // Correct (+10)
        studentAnswers.put(questions.get(4).getQuestionId(), "D"); // Incorrect (0)

        ExamAttempt attempt = examService.startAttempt(2, javaExam.getExamId());
        ExamResult result = examService.gradeAttempt(
                attempt.getAttemptId(), 2, javaExam, questions, studentAnswers, SubmissionType.MANUAL
        );

        assertNotNull(result);
        assertEquals(5, result.getTotalQuestions());
        assertEquals(5, result.getAnsweredCount());
        assertEquals(4, result.getCorrectCount());
        assertEquals(1, result.getIncorrectCount());
        assertEquals(40.0, result.getScore());
        assertEquals(80.0, result.getPercentage());
        assertTrue(result.isPassed(), "80% score must exceed 50% passing threshold");
    }

    public void testAutoTimeoutSubmissionScoring() {
        setUp();
        List<Exam> exams = examService.getAvailableExams();
        Exam javaExam = exams.get(0);
        List<Question> questions = examService.getQuestionsForExam(javaExam.getExamId());

        // Partial answers due to timer timeout: answered only 2 questions
        Map<Integer, String> studentAnswers = new HashMap<>();
        studentAnswers.put(questions.get(0).getQuestionId(), "C"); // Correct (+10)
        studentAnswers.put(questions.get(1).getQuestionId(), "C"); // Incorrect (0)

        ExamAttempt attempt = examService.startAttempt(2, javaExam.getExamId());
        ExamResult result = examService.gradeAttempt(
                attempt.getAttemptId(), 2, javaExam, questions, studentAnswers, SubmissionType.AUTO_TIMEOUT
        );

        assertNotNull(result);
        assertEquals(5, result.getTotalQuestions());
        assertEquals(2, result.getAnsweredCount());
        assertEquals(1, result.getCorrectCount());
        assertEquals(1, result.getIncorrectCount());
        assertEquals(10.0, result.getScore());
        assertEquals(20.0, result.getPercentage());
        assertFalse(result.isPassed(), "20% score should fail a 50% passing threshold");
    }

    public void testPassingThresholdBoundary() {
        setUp();
        List<Exam> exams = examService.getAvailableExams();
        Exam javaExam = exams.get(0);
        List<Question> questions = examService.getQuestionsForExam(javaExam.getExamId());

        // Test exact pass threshold: 3 correct = 30 / 50 = 60% (Pass >= 50%)
        Map<Integer, String> passAnswers = new HashMap<>();
        passAnswers.put(questions.get(0).getQuestionId(), "C");
        passAnswers.put(questions.get(1).getQuestionId(), "B");
        passAnswers.put(questions.get(2).getQuestionId(), "B");

        ExamResult passResult = examService.gradeAttempt(10, 2, javaExam, questions, passAnswers, SubmissionType.MANUAL);
        assertTrue(passResult.isPassed());

        // Test fail threshold: 2 correct = 20 / 50 = 40% (Fail < 50%)
        Map<Integer, String> failAnswers = new HashMap<>();
        failAnswers.put(questions.get(0).getQuestionId(), "C");
        failAnswers.put(questions.get(1).getQuestionId(), "B");

        ExamResult failResult = examService.gradeAttempt(11, 2, javaExam, questions, failAnswers, SubmissionType.MANUAL);
        assertFalse(failResult.isPassed());
    }
}

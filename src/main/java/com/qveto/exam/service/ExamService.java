package com.qveto.exam.service;

import com.qveto.exam.dao.AttemptDAO;
import com.qveto.exam.dao.ExamDAO;
import com.qveto.exam.dao.QuestionDAO;
import com.qveto.exam.dao.ResultDAO;
import com.qveto.exam.model.*;
import com.qveto.exam.util.DBConnection;
import com.qveto.exam.validation.InputValidator;
import com.qveto.exam.validation.ValidationResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Service managing examination workflows, question banks, scoring, and history.
 */
public class ExamService {

    private final ExamDAO examDAO;
    private final QuestionDAO questionDAO;
    private final AttemptDAO attemptDAO;
    private final ResultDAO resultDAO;

    // Fallback data for offline mode
    private static final List<Exam> mockExams = new ArrayList<>();
    private static final Map<Integer, List<Question>> mockExamQuestions = new HashMap<>();
    private static final List<Question> mockStandaloneQuestions = new ArrayList<>();
    private static final List<ExamResult> mockResults = new ArrayList<>();
    private static final Map<Integer, Map<Integer, String>> mockAttemptAnswers = new HashMap<>();

    static {
        Exam ex1 = new Exam(1, "Java Core & Swing Fundamentals",
                "Timed assessment evaluating core Java concepts, OOP, multithreading, and Swing GUI architecture.",
                15, 5, 50.00, 50.00, true);
        Exam ex2 = new Exam(2, "Database Management Systems & SQL",
                "Comprehensive assessment covering relational databases, normalization, SQL queries, and JDBC transactions.",
                20, 5, 50.00, 50.00, true);
        mockExams.add(ex1);
        mockExams.add(ex2);

        List<Question> javaQuestions = Arrays.asList(
                new Question(1, "Java", "Which of the following is NOT a Java primitive data type?", "int", "boolean", "String", "char", "C", 10.0),
                new Question(2, "Java", "Which keyword is used to prevent method overriding in Java?", "static", "final", "abstract", "protected", "B", 10.0),
                new Question(3, "Java", "What is the default layout manager for a Swing JFrame content pane?", "FlowLayout", "BorderLayout", "GridLayout", "CardLayout", "B", 10.0),
                new Question(4, "Java", "Which interface must a class implement to be executed as a background thread?", "Runnable", "Comparable", "Serializable", "Cloneable", "A", 10.0),
                new Question(5, "Java", "Which package contains the Java Swing components like JFrame and JButton?", "java.awt", "javax.swing", "java.util", "java.lang", "B", 10.0)
        );
        mockExamQuestions.put(1, javaQuestions);

        List<Question> dbQuestions = Arrays.asList(
                new Question(6, "DBMS", "Which SQL clause is used to filter records resulting from a GROUP BY statement?", "WHERE", "ORDER BY", "HAVING", "JOIN", "C", 10.0),
                new Question(7, "DBMS", "Which JDBC interface is used to execute parameterized SQL queries securely?", "Statement", "PreparedStatement", "CallableStatement", "ResultSet", "B", 10.0),
                new Question(8, "DBMS", "What type of relationship exists between an Exam and Question when a question can belong to multiple exams?", "One-to-One", "One-to-Many", "Many-to-Many", "Self-Referencing", "C", 10.0),
                new Question(9, "DBMS", "Which constraint ensures that all values in a database column are unique?", "PRIMARY KEY", "NOT NULL", "UNIQUE", "FOREIGN KEY", "C", 10.0),
                new Question(10, "DBMS", "What does ACID stand for in database transaction management?", "Atomicity, Consistency, Isolation, Durability", "Accuracy, Concurrency, Integrity, Durability", "Atomicity, Compatibility, Isolation, Dependency", "Access, Consistency, Indexing, Data", "A", 10.0)
        );
        mockExamQuestions.put(2, dbQuestions);

        // Seed initial mock exam results
        ExamResult r1 = new ExamResult(1, 1, 2, 1, 5, 5, 4, 1, 40.0, 50.0, 80.0, true, new java.sql.Timestamp(System.currentTimeMillis() - 7200000));
        r1.setExamTitle("Java Core & Swing Fundamentals");
        r1.setStudentName("Aarav Patel");
        mockResults.add(r1);
        Map<Integer, String> r1Answers = new HashMap<>();
        r1Answers.put(1, "C"); r1Answers.put(2, "B"); r1Answers.put(3, "B"); r1Answers.put(4, "A"); r1Answers.put(5, "A");
        mockAttemptAnswers.put(1, r1Answers);

        ExamResult r2 = new ExamResult(2, 2, 3, 2, 5, 5, 5, 0, 50.0, 50.0, 100.0, true, new java.sql.Timestamp(System.currentTimeMillis() - 3600000));
        r2.setExamTitle("Database Management Systems & SQL");
        r2.setStudentName("Diya Sharma");
        mockResults.add(r2);
        Map<Integer, String> r2Answers = new HashMap<>();
        r2Answers.put(6, "C"); r2Answers.put(7, "B"); r2Answers.put(8, "C"); r2Answers.put(9, "C"); r2Answers.put(10, "A");
        mockAttemptAnswers.put(2, r2Answers);
    }

    public ExamService() {
        this.examDAO = new ExamDAO();
        this.questionDAO = new QuestionDAO();
        this.attemptDAO = new AttemptDAO();
        this.resultDAO = new ResultDAO();
    }

    public List<Exam> getAvailableExams() {
        if (DBConnection.isDatabaseConnected()) {
            List<Exam> exams = examDAO.getAllActiveExams();
            if (!exams.isEmpty()) {
                return exams;
            }
        }
        return new ArrayList<>(mockExams);
    }

    public List<Question> getQuestionsForExam(int examId) {
        if (DBConnection.isDatabaseConnected()) {
            List<Question> questions = questionDAO.getQuestionsForExam(examId);
            if (!questions.isEmpty()) {
                return questions;
            }
        }
        return mockExamQuestions.getOrDefault(examId, Collections.emptyList());
    }

    /**
     * Retrieves all questions in the central question bank.
     */
    public List<Question> getAllQuestionBank() {
        if (DBConnection.isDatabaseConnected()) {
            List<Question> questions = questionDAO.getAllQuestions();
            if (!questions.isEmpty()) {
                return questions;
            }
        }
        List<Question> all = new ArrayList<>(mockStandaloneQuestions);
        for (List<Question> list : mockExamQuestions.values()) {
            for (Question q : list) {
                if (!all.contains(q)) {
                    all.add(q);
                }
            }
        }
        return all;
    }

    /**
     * Creates a new examination paper with input validation and persistence.
     */
    public ValidationResult createExam(Exam exam) {
        if (exam == null) {
            return ValidationResult.failure("Exam payload cannot be null.");
        }

        ValidationResult val = InputValidator.validateExam(
                exam.getTitle(),
                exam.getDurationMinutes(),
                exam.getTotalQuestions(),
                exam.getTotalMarks(),
                exam.getPassingPercentage()
        );
        if (!val.isValid()) {
            return val;
        }

        if (DBConnection.isDatabaseConnected()) {
            boolean created = examDAO.createExam(exam);
            if (!created) {
                return ValidationResult.failure("Database error while creating exam.");
            }
        }

        // Keep mock store synchronized
        if (exam.getExamId() <= 0) {
            exam.setExamId(mockExams.size() + 10);
        }
        mockExams.add(exam);
        return ValidationResult.success();
    }

    /**
     * Adds a new multiple-choice question to the question bank and optionally maps it to an exam.
     */
    public ValidationResult addQuestionToBank(Question question, Integer targetExamId) {
        if (question == null) {
            return ValidationResult.failure("Question cannot be null.");
        }

        ValidationResult val = InputValidator.validateQuestion(
                question.getSubject(),
                question.getQuestionText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getCorrectOption()
        );
        if (!val.isValid()) {
            return val;
        }

        if (DBConnection.isDatabaseConnected()) {
            boolean added = questionDAO.addQuestion(question);
            if (!added) {
                return ValidationResult.failure("Database error while adding question.");
            }
            if (targetExamId != null && targetExamId > 0) {
                int order = questionDAO.getQuestionsForExam(targetExamId).size() + 1;
                questionDAO.linkQuestionToExam(targetExamId, question.getQuestionId(), order);
            }
        }

        // Keep mock store synchronized
        if (question.getQuestionId() <= 0) {
            question.setQuestionId(200 + (int)(System.currentTimeMillis() % 1000));
        }
        if (targetExamId != null && targetExamId > 0) {
            mockExamQuestions.computeIfAbsent(targetExamId, k -> new ArrayList<>()).add(question);
        } else {
            mockStandaloneQuestions.add(question);
        }

        return ValidationResult.success();
    }

    /**
     * Toggles an exam's active status between active and inactive.
     */
    public boolean toggleExamStatus(int examId) {
        for (Exam ex : mockExams) {
            if (ex.getExamId() == examId) {
                boolean newStatus = !ex.isActive();
                ex.setActive(newStatus);
                if (DBConnection.isDatabaseConnected()) {
                    examDAO.toggleActive(examId, newStatus);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Initializes a new student exam attempt session.
     */
    public ExamAttempt startAttempt(int userId, int examId) {
        if (DBConnection.isDatabaseConnected()) {
            ExamAttempt attempt = attemptDAO.createAttempt(userId, examId);
            if (attempt != null) {
                return attempt;
            }
        }
        ExamAttempt mockAttempt = new ExamAttempt();
        mockAttempt.setAttemptId(1000 + (int)(System.currentTimeMillis() % 10000));
        mockAttempt.setUserId(userId);
        mockAttempt.setExamId(examId);
        mockAttempt.setStatus(AttemptStatus.IN_PROGRESS);
        mockAttempt.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
        return mockAttempt;
    }

    /**
     * Persists candidate's answer for a question during navigation.
     */
    public boolean recordAnswer(int attemptId, int questionId, String selectedOption) {
        if (DBConnection.isDatabaseConnected()) {
            return attemptDAO.saveAnswer(attemptId, questionId, selectedOption);
        }
        return true;
    }

    /**
     * Calculates score, marks, percentage, and passed state given student's answers,
     * and finalizes the attempt record.
     */
    public ExamResult gradeAttempt(int attemptId, int userId, Exam exam, List<Question> questions,
                                   Map<Integer, String> selectedAnswers, SubmissionType submissionType) {
        int totalQuestions = questions.size();
        int answeredCount = 0;
        int correctCount = 0;
        int incorrectCount = 0;
        double totalScore = 0.0;
        double maxMarks = exam.getTotalMarks();

        for (Question q : questions) {
            String studentChoice = selectedAnswers.get(q.getQuestionId());
            if (studentChoice != null && !studentChoice.trim().isEmpty()) {
                answeredCount++;
                if (studentChoice.equalsIgnoreCase(q.getCorrectOption())) {
                    correctCount++;
                    totalScore += q.getMarks();
                } else {
                    incorrectCount++;
                }
            }
        }

        double percentage = maxMarks > 0 ? (totalScore / maxMarks) * 100.0 : 0.0;
        boolean passed = percentage >= exam.getPassingPercentage();

        ExamResult result = new ExamResult(
                0, attemptId, userId, exam.getExamId(),
                totalQuestions, answeredCount, correctCount, incorrectCount,
                totalScore, maxMarks, percentage, passed,
                new java.sql.Timestamp(System.currentTimeMillis())
        );
        result.setExamTitle(exam.getTitle());

        if (DBConnection.isDatabaseConnected()) {
            resultDAO.saveResult(result);
            AttemptStatus status = (submissionType == SubmissionType.AUTO_TIMEOUT)
                    ? AttemptStatus.TIMED_OUT : AttemptStatus.SUBMITTED;
            attemptDAO.completeAttempt(attemptId, submissionType, status, totalScore, percentage, passed);
        }

        // Keep mock/offline history synchronized
        if (result.getResultId() <= 0) {
            result.setResultId(mockResults.size() + 10);
        }
        mockResults.add(0, result);
        mockAttemptAnswers.put(attemptId, new HashMap<>(selectedAnswers));

        return result;
    }

    /**
     * Overload for backward compatibility defaulting to manual submission.
     */
    public ExamResult gradeAttempt(int attemptId, int userId, Exam exam, List<Question> questions, Map<Integer, String> selectedAnswers) {
        return gradeAttempt(attemptId, userId, exam, questions, selectedAnswers, SubmissionType.MANUAL);
    }

    /**
     * Retrieves examination history for a specific candidate.
     */
    public List<ExamResult> getStudentHistory(int userId) {
        if (DBConnection.isDatabaseConnected()) {
            List<ExamResult> results = resultDAO.getResultsByUser(userId);
            if (!results.isEmpty()) {
                return results;
            }
        }
        List<ExamResult> userHistory = new ArrayList<>();
        for (ExamResult r : mockResults) {
            if (r.getUserId() == userId) {
                userHistory.add(r);
            }
        }
        return userHistory;
    }

    /**
     * Retrieves all candidate examination results across all exams and students.
     */
    public List<ExamResult> getAllCandidateResults() {
        if (DBConnection.isDatabaseConnected()) {
            List<ExamResult> results = resultDAO.getAllResults();
            if (!results.isEmpty()) {
                return results;
            }
        }
        return new ArrayList<>(mockResults);
    }

    /**
     * Retrieves answers selected for a specific attempt for detailed review.
     */
    public Map<Integer, String> getAnswersForAttempt(int attemptId) {
        if (DBConnection.isDatabaseConnected()) {
            Map<Integer, String> dbAnswers = attemptDAO.getAnswersForAttempt(attemptId);
            if (!dbAnswers.isEmpty()) {
                return dbAnswers;
            }
        }
        return mockAttemptAnswers.getOrDefault(attemptId, Collections.emptyMap());
    }

    /**
     * Exports candidate assessment results to a CSV spreadsheet file.
     */
    public boolean exportResultsToCSV(File file, List<ExamResult> results) {
        if (file == null || results == null) {
            return false;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write CSV Header
            writer.println("Result ID,Candidate Name,Exam Title,Total Questions,Answered,Correct,Incorrect,Score,Total Marks,Percentage,Outcome,Graded At");

            for (ExamResult r : results) {
                writer.printf("%d,\"%s\",\"%s\",%d,%d,%d,%d,%.2f,%.2f,%.2f%%,%s,\"%s\"%n",
                        r.getResultId(),
                        r.getStudentName() != null ? r.getStudentName().replace("\"", "\"\"") : "Candidate",
                        r.getExamTitle() != null ? r.getExamTitle().replace("\"", "\"\"") : "Exam",
                        r.getTotalQuestions(),
                        r.getAnsweredCount(),
                        r.getCorrectCount(),
                        r.getIncorrectCount(),
                        r.getScore(),
                        r.getTotalMarks(),
                        r.getPercentage(),
                        r.isPassed() ? "PASSED" : "FAILED",
                        r.getGradedAt() != null ? r.getGradedAt().toString() : ""
                );
            }
            return true;
        } catch (IOException e) {
            System.err.println("[ExamService] exportResultsToCSV error: " + e.getMessage());
            return false;
        }
    }
}

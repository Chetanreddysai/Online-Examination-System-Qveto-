package com.qveto.exam.service;

import com.qveto.exam.dao.AttemptDAO;
import com.qveto.exam.dao.ExamDAO;
import com.qveto.exam.dao.QuestionDAO;
import com.qveto.exam.dao.ResultDAO;
import com.qveto.exam.model.*;
import com.qveto.exam.util.DBConnection;

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
     * Calculates score, marks, percentage, and passed state given student's answers.
     */
    public ExamResult gradeAttempt(int attemptId, int userId, Exam exam, List<Question> questions, Map<Integer, String> selectedAnswers) {
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
            attemptDAO.completeAttempt(attemptId, SubmissionType.MANUAL, AttemptStatus.SUBMITTED, totalScore, percentage, passed);
        }

        return result;
    }
}

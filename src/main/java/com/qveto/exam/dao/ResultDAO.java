package com.qveto.exam.dao;

import com.qveto.exam.model.Exam;
import com.qveto.exam.model.ExamResult;
import com.qveto.exam.model.Question;
import com.qveto.exam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for computing, persisting, and querying final examination scores and outcomes.
 */
public class ResultDAO {

    private static final String RESULT_SELECT_BASE =
            "SELECT r.result_id, r.attempt_id, r.user_id, r.exam_id, e.title AS exam_title, " +
            "u.full_name AS student_name, r.total_questions, r.answered_count, r.correct_count, " +
            "r.incorrect_count, r.score, r.total_marks, r.percentage, r.passed, r.graded_at " +
            "FROM exam_results r " +
            "JOIN exams e ON r.exam_id = e.exam_id " +
            "JOIN users u ON r.user_id = u.user_id ";

    private static final String INSERT_RESULT_SQL =
            "INSERT INTO exam_results (attempt_id, user_id, exam_id, total_questions, " +
            "answered_count, correct_count, incorrect_count, score, total_marks, percentage, passed) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPSERT_RESULT_SQL =
            INSERT_RESULT_SQL + " ON DUPLICATE KEY UPDATE " +
            "total_questions = VALUES(total_questions), " +
            "answered_count = VALUES(answered_count), " +
            "correct_count = VALUES(correct_count), " +
            "incorrect_count = VALUES(incorrect_count), " +
            "score = VALUES(score), " +
            "total_marks = VALUES(total_marks), " +
            "percentage = VALUES(percentage), " +
            "passed = VALUES(passed), " +
            "graded_at = CURRENT_TIMESTAMP";

    /**
     * Persists a newly graded examination result record.
     */
    public boolean saveResult(ExamResult result) {
        if (!isValidResultPayload(result)) {
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            return saveResult(conn, result);
        } catch (SQLException e) {
            System.err.println("[ResultDAO] saveResult error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Persists a graded examination result within an existing caller-managed JDBC connection/transaction.
     */
    public boolean saveResult(Connection conn, ExamResult result) throws SQLException {
        if (conn == null || !isValidResultPayload(result)) {
            return false;
        }
        return persistResultInternal(conn, result, INSERT_RESULT_SQL);
    }

    /**
     * Idempotently saves or updates the graded outcome for an attempt (keyed by unique attempt_id).
     */
    public boolean upsertResult(ExamResult result) {
        if (!isValidResultPayload(result)) {
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            return persistResultInternal(conn, result, UPSERT_RESULT_SQL);
        } catch (SQLException e) {
            System.err.println("[ResultDAO] upsertResult error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Computes score, percentage, and pass/fail status directly from persisted attempt answers
     * and question bank definitions, then persists the result atomically.
     */
    public ExamResult calculateAndSaveResult(int attemptId) {
        if (attemptId <= 0) {
            return null;
        }

        String attemptMetaSql =
                "SELECT a.user_id, a.exam_id, e.title, e.total_questions, e.total_marks, e.passing_percentage " +
                "FROM exam_attempts a " +
                "INNER JOIN exams e ON a.exam_id = e.exam_id " +
                "WHERE a.attempt_id = ?";

        String gradingSql =
                "SELECT q.question_id, q.correct_option, q.marks, aa.selected_option " +
                "FROM exam_questions eq " +
                "INNER JOIN questions q ON eq.question_id = q.question_id " +
                "LEFT JOIN attempt_answers aa ON aa.question_id = q.question_id AND aa.attempt_id = ? " +
                "WHERE eq.exam_id = ? " +
                "ORDER BY eq.question_order ASC";

        Connection conn = null;
        boolean previousAutoCommit = true;

        try {
            conn = DBConnection.getConnection();
            previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            int userId;
            int examId;
            String examTitle;
            int configuredQuestions;
            double totalMarks;
            double passingPercentage;

            try (PreparedStatement metaStmt = conn.prepareStatement(attemptMetaSql)) {
                metaStmt.setInt(1, attemptId);
                try (ResultSet rs = metaStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return null;
                    }
                    userId = rs.getInt("user_id");
                    examId = rs.getInt("exam_id");
                    examTitle = rs.getString("title");
                    configuredQuestions = rs.getInt("total_questions");
                    totalMarks = rs.getDouble("total_marks");
                    passingPercentage = rs.getDouble("passing_percentage");
                }
            }

            int mappedQuestions = 0;
            int answeredCount = 0;
            int correctCount = 0;
            int incorrectCount = 0;
            double score = 0.0;
            double sumQuestionMarks = 0.0;

            try (PreparedStatement gradeStmt = conn.prepareStatement(gradingSql)) {
                gradeStmt.setInt(1, attemptId);
                gradeStmt.setInt(2, examId);
                try (ResultSet rs = gradeStmt.executeQuery()) {
                    while (rs.next()) {
                        mappedQuestions++;
                        double qMarks = rs.getDouble("marks");
                        sumQuestionMarks += qMarks;
                        String correctOpt = rs.getString("correct_option");
                        String selectedOpt = rs.getString("selected_option");

                        if (selectedOpt != null && !selectedOpt.trim().isEmpty()) {
                            answeredCount++;
                            if (correctOpt != null && selectedOpt.trim().equalsIgnoreCase(correctOpt.trim())) {
                                correctCount++;
                                score += qMarks;
                            } else {
                                incorrectCount++;
                            }
                        }
                    }
                }
            }

            int totalQuestions = mappedQuestions > 0 ? mappedQuestions : configuredQuestions;
            double effectiveMaxMarks = totalMarks > 0 ? totalMarks : sumQuestionMarks;
            double percentage = calculatePercentage(score, effectiveMaxMarks);
            boolean passed = isPassingScore(percentage, passingPercentage);

            ExamResult result = new ExamResult(
                    0, attemptId, userId, examId,
                    totalQuestions, answeredCount, correctCount, incorrectCount,
                    score, effectiveMaxMarks, percentage, passed,
                    new Timestamp(System.currentTimeMillis())
            );
            result.setExamTitle(examTitle);

            if (!persistResultInternal(conn, result, UPSERT_RESULT_SQL)) {
                conn.rollback();
                return null;
            }

            conn.commit();
            return result;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[ResultDAO] calculateAndSaveResult rollback error: " + rollbackEx.getMessage());
                }
            }
            System.err.println("[ResultDAO] calculateAndSaveResult error: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(previousAutoCommit);
                } catch (SQLException ignored) {
                }
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
        return null;
    }

    /**
     * Computes an ExamResult summary from exam metadata, questions, and candidate answer selections.
     */
    public static ExamResult buildComputedResult(int attemptId, int userId, Exam exam, List<Question> questions,
                                                   Map<Integer, String> selectedAnswers) {
        if (exam == null || questions == null) {
            return null;
        }

        int totalQuestions = questions.size();
        int answeredCount = 0;
        int correctCount = 0;
        int incorrectCount = 0;
        double score = 0.0;
        double sumQuestionMarks = 0.0;

        for (Question q : questions) {
            sumQuestionMarks += q.getMarks();
            String choice = selectedAnswers != null ? selectedAnswers.get(q.getQuestionId()) : null;
            if (choice != null && !choice.trim().isEmpty()) {
                answeredCount++;
                if (q.getCorrectOption() != null && choice.trim().equalsIgnoreCase(q.getCorrectOption().trim())) {
                    correctCount++;
                    score += q.getMarks();
                } else {
                    incorrectCount++;
                }
            }
        }

        double maxMarks = exam.getTotalMarks() > 0 ? exam.getTotalMarks() : sumQuestionMarks;
        double percentage = calculatePercentage(score, maxMarks);
        boolean passed = isPassingScore(percentage, exam.getPassingPercentage());

        ExamResult result = new ExamResult(
                0, attemptId, userId, exam.getExamId(),
                totalQuestions, answeredCount, correctCount, incorrectCount,
                score, maxMarks, percentage, passed,
                new Timestamp(System.currentTimeMillis())
        );
        result.setExamTitle(exam.getTitle());
        return result;
    }

    /**
     * Calculates percentage rounded to 2 decimal places, guarding against zero or negative total marks.
     */
    public static double calculatePercentage(double score, double totalMarks) {
        if (totalMarks <= 0 || score <= 0) {
            return 0.0;
        }
        return Math.round(((score / totalMarks) * 100.0) * 100.0) / 100.0;
    }

    /**
     * Evaluates pass/fail threshold status against exam passing criteria.
     */
    public static boolean isPassingScore(double percentage, double passingPercentage) {
        return percentage >= passingPercentage;
    }

    /**
     * Retrieves the graded result for a specific examination attempt ID.
     */
    public ExamResult getResultByAttemptId(int attemptId) {
        if (attemptId <= 0) {
            return null;
        }
        String sql = RESULT_SELECT_BASE + "WHERE r.attempt_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToResult(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ResultDAO] getResultByAttemptId error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks whether a graded result record already exists for an attempt.
     */
    public boolean resultExistsForAttempt(int attemptId) {
        if (attemptId <= 0) {
            return false;
        }
        String sql = "SELECT 1 FROM exam_results WHERE attempt_id = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[ResultDAO] resultExistsForAttempt error: " + e.getMessage());
        }
        return false;
    }

    public List<ExamResult> getResultsByUser(int userId) {
        List<ExamResult> results = new ArrayList<>();
        String sql = RESULT_SELECT_BASE + "WHERE r.user_id = ? ORDER BY r.graded_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToResult(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ResultDAO] getResultsByUser error: " + e.getMessage());
        }
        return results;
    }

    public List<ExamResult> getAllResults() {
        List<ExamResult> results = new ArrayList<>();
        String sql = RESULT_SELECT_BASE + "ORDER BY r.graded_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(mapResultSetToResult(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ResultDAO] getAllResults error: " + e.getMessage());
        }
        return results;
    }

    private boolean isValidResultPayload(ExamResult result) {
        return result != null
                && result.getAttemptId() > 0
                && result.getUserId() > 0
                && result.getExamId() > 0
                && result.getTotalQuestions() >= 0
                && result.getAnsweredCount() >= 0
                && result.getCorrectCount() >= 0
                && result.getIncorrectCount() >= 0
                && result.getScore() >= 0.0
                && result.getTotalMarks() >= 0.0;
    }

    private boolean persistResultInternal(Connection conn, ExamResult result, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, result.getAttemptId());
            stmt.setInt(2, result.getUserId());
            stmt.setInt(3, result.getExamId());
            stmt.setInt(4, result.getTotalQuestions());
            stmt.setInt(5, result.getAnsweredCount());
            stmt.setInt(6, result.getCorrectCount());
            stmt.setInt(7, result.getIncorrectCount());
            stmt.setDouble(8, result.getScore());
            stmt.setDouble(9, result.getTotalMarks());
            stmt.setDouble(10, result.getPercentage());
            stmt.setBoolean(11, result.isPassed());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        result.setResultId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    private ExamResult mapResultSetToResult(ResultSet rs) throws SQLException {
        ExamResult res = new ExamResult();
        res.setResultId(rs.getInt("result_id"));
        res.setAttemptId(rs.getInt("attempt_id"));
        res.setUserId(rs.getInt("user_id"));
        res.setExamId(rs.getInt("exam_id"));
        res.setExamTitle(rs.getString("exam_title"));
        res.setStudentName(rs.getString("student_name"));
        res.setTotalQuestions(rs.getInt("total_questions"));
        res.setAnsweredCount(rs.getInt("answered_count"));
        res.setCorrectCount(rs.getInt("correct_count"));
        res.setIncorrectCount(rs.getInt("incorrect_count"));
        res.setScore(rs.getDouble("score"));
        res.setTotalMarks(rs.getDouble("total_marks"));
        res.setPercentage(rs.getDouble("percentage"));
        res.setPassed(rs.getBoolean("passed"));
        res.setGradedAt(rs.getTimestamp("graded_at"));
        return res;
    }
}

package com.qveto.exam.dao;

import com.qveto.exam.model.AttemptStatus;
import com.qveto.exam.model.ExamAttempt;
import com.qveto.exam.model.SubmissionType;
import com.qveto.exam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for exam attempts and candidate answer persistence.
 */
public class AttemptDAO {

    public ExamAttempt createAttempt(int userId, int examId) {
        String sql = "INSERT INTO exam_attempts (user_id, exam_id, status) VALUES (?, ?, 'IN_PROGRESS')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, examId);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        ExamAttempt attempt = new ExamAttempt();
                        attempt.setAttemptId(rs.getInt(1));
                        attempt.setUserId(userId);
                        attempt.setExamId(examId);
                        attempt.setStatus(AttemptStatus.IN_PROGRESS);
                        attempt.setStartTime(new Timestamp(System.currentTimeMillis()));
                        return attempt;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] createAttempt error: " + e.getMessage());
        }
        return null;
    }

    public boolean saveAnswer(int attemptId, int questionId, String selectedOption) {
        String sql = "INSERT INTO attempt_answers (attempt_id, question_id, selected_option) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE selected_option = VALUES(selected_option)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            stmt.setInt(2, questionId);
            stmt.setString(3, selectedOption);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] saveAnswer error: " + e.getMessage());
        }
        return false;
    }

    public boolean completeAttempt(int attemptId, SubmissionType submissionType, AttemptStatus status,
                                   double score, double percentage, boolean passed) {
        String sql = "UPDATE exam_attempts SET end_time = CURRENT_TIMESTAMP, submission_type = ?, " +
                     "status = ?, score = ?, percentage = ?, passed = ? WHERE attempt_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, submissionType != null ? submissionType.name() : null);
            stmt.setString(2, status != null ? status.name() : AttemptStatus.SUBMITTED.name());
            stmt.setDouble(3, score);
            stmt.setDouble(4, percentage);
            stmt.setBoolean(5, passed);
            stmt.setInt(6, attemptId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] completeAttempt error: " + e.getMessage());
        }
        return false;
    }

    public List<ExamAttempt> getAttemptsByUser(int userId) {
        List<ExamAttempt> attempts = new ArrayList<>();
        String sql = "SELECT attempt_id, user_id, exam_id, start_time, end_time, submission_type, " +
                     "status, score, percentage, passed " +
                     "FROM exam_attempts WHERE user_id = ? ORDER BY start_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapResultSetToAttempt(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] getAttemptsByUser error: " + e.getMessage());
        }
        return attempts;
    }

    /**
     * Retrieves an attempt session by its unique attempt ID.
     */
    public ExamAttempt getAttemptById(int attemptId) {
        String sql = "SELECT attempt_id, user_id, exam_id, start_time, end_time, submission_type, " +
                     "status, score, percentage, passed " +
                     "FROM exam_attempts WHERE attempt_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttempt(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] getAttemptById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Alias for getAttemptById to support getAttempt(attemptId).
     */
    public ExamAttempt getAttempt(int attemptId) {
        return getAttemptById(attemptId);
    }

    /**
     * Retrieves the active in-progress attempt for a user and exam, if one exists.
     */
    public ExamAttempt getActiveAttempt(int userId, int examId) {
        String sql = "SELECT attempt_id, user_id, exam_id, start_time, end_time, submission_type, " +
                     "status, score, percentage, passed " +
                     "FROM exam_attempts WHERE user_id = ? AND exam_id = ? AND status = 'IN_PROGRESS' " +
                     "ORDER BY start_time DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttempt(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] getActiveAttempt error: " + e.getMessage());
        }
        return null;
    }

    private ExamAttempt mapResultSetToAttempt(ResultSet rs) throws SQLException {
        ExamAttempt a = new ExamAttempt();
        a.setAttemptId(rs.getInt("attempt_id"));
        a.setUserId(rs.getInt("user_id"));
        a.setExamId(rs.getInt("exam_id"));
        a.setStartTime(rs.getTimestamp("start_time"));
        a.setEndTime(rs.getTimestamp("end_time"));
        a.setSubmissionType(SubmissionType.fromString(rs.getString("submission_type")));
        a.setStatus(AttemptStatus.fromString(rs.getString("status")));
        double sc = rs.getDouble("score");
        if (!rs.wasNull()) a.setScore(sc);
        double pct = rs.getDouble("percentage");
        if (!rs.wasNull()) a.setPercentage(pct);
        boolean pass = rs.getBoolean("passed");
        if (!rs.wasNull()) a.setPassed(pass);
        return a;
    }

    /**
     * Retrieves saved answer selections for a specific attempt session.
     */
    public Map<Integer, String> getAnswersForAttempt(int attemptId) {
        Map<Integer, String> answers = new HashMap<>();
        String sql = "SELECT question_id, selected_option FROM attempt_answers WHERE attempt_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    answers.put(rs.getInt("question_id"), rs.getString("selected_option"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] getAnswersForAttempt error: " + e.getMessage());
        }
        return answers;
    }

    /**
     * Retrieves the current lifecycle status of an attempt.
     */
    public AttemptStatus getAttemptStatus(int attemptId) {
        String sql = "SELECT status FROM exam_attempts WHERE attempt_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attemptId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return AttemptStatus.fromString(rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] getAttemptStatus error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks if the attempt is actively in progress and permits answer changes.
     */
    public boolean isAttemptActive(int attemptId) {
        AttemptStatus status = getAttemptStatus(attemptId);
        return status == AttemptStatus.IN_PROGRESS;
    }
}

package com.qveto.exam.dao;

import com.qveto.exam.model.AttemptStatus;
import com.qveto.exam.model.ExamAttempt;
import com.qveto.exam.model.SubmissionType;
import com.qveto.exam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

                    attempts.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] getAttemptsByUser error: " + e.getMessage());
        }
        return attempts;
    }
}

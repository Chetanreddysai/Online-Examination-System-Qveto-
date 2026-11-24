package com.qveto.exam.dao;

import com.qveto.exam.model.AttemptAnswer;
import com.qveto.exam.model.AttemptStatus;
import com.qveto.exam.model.ExamAttempt;
import com.qveto.exam.model.SubmissionType;
import com.qveto.exam.util.DBConnection;

import java.sql.*;
import java.util.*;

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

    /**
     * Persists or updates candidate's answer for a question in real-time.
     * Uses ON DUPLICATE KEY UPDATE to guarantee zero data loss during navigation.
     */
    public boolean saveAnswer(int attemptId, int questionId, String selectedOption) {
        return upsertAnswer(attemptId, questionId, selectedOption);
    }

    /**
     * Dynamic upsert query for live answer persistence during question navigation.
     * Records selection or un-selection while updating timestamp.
     */
    public boolean upsertAnswer(int attemptId, int questionId, String selectedOption) {
        // Enforce state immutability: do not allow writes if attempt is already completed/timed out
        AttemptStatus currentStatus = getAttemptStatus(attemptId);
        if (currentStatus != null && currentStatus.isTerminal()) {
            System.err.println("[AttemptDAO] Rejecting answer modification for finalized attempt: " + attemptId);
            return false;
        }

        String sql = "INSERT INTO attempt_answers (attempt_id, question_id, selected_option, answered_at) " +
                     "VALUES (?, ?, ?, CURRENT_TIMESTAMP) " +
                     "ON DUPLICATE KEY UPDATE selected_option = VALUES(selected_option), answered_at = CURRENT_TIMESTAMP";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            stmt.setInt(2, questionId);
            stmt.setString(3, selectedOption != null && !selectedOption.trim().isEmpty() ? selectedOption.trim() : null);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] upsertAnswer error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Clears candidate's selected option for a specific question (e.g. candidate clicks 'Clear Answer').
     */
    public boolean clearAnswer(int attemptId, int questionId) {
        return upsertAnswer(attemptId, questionId, null);
    }

    /**
     * Retrieves the candidate's selected choice for a specific question in an attempt.
     */
    public String getSelectedAnswer(int attemptId, int questionId) {
        String sql = "SELECT selected_option FROM attempt_answers WHERE attempt_id = ? AND question_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            stmt.setInt(2, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("selected_option");
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] getSelectedAnswer error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Counts the total number of questions answered with non-null selections in an attempt.
     */
    public int getAnsweredCount(int attemptId) {
        String sql = "SELECT COUNT(*) FROM attempt_answers WHERE attempt_id = ? AND selected_option IS NOT NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] getAnsweredCount error: " + e.getMessage());
        }
        return 0;
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

    /**
     * Retrieves the set of question IDs that have been answered (non-null and non-empty selection)
     * for immediate rendering of green indicators on the Question Navigator / Palette.
     */
    public Set<Integer> getAnsweredQuestionIds(int attemptId) {
        Set<Integer> answered = new HashSet<>();
        String sql = "SELECT question_id FROM attempt_answers WHERE attempt_id = ? " +
                     "AND selected_option IS NOT NULL AND TRIM(selected_option) != ''";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    answered.add(rs.getInt("question_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] getAnsweredQuestionIds error: " + e.getMessage());
        }
        return answered;
    }

    /**
     * Retrieves a map of question IDs to boolean answered status for question palette fast-lookup.
     */
    public Map<Integer, Boolean> getQuestionPaletteStatuses(int attemptId) {
        Map<Integer, Boolean> statuses = new HashMap<>();
        String sql = "SELECT question_id, (selected_option IS NOT NULL AND TRIM(selected_option) != '') AS is_answered " +
                     "FROM attempt_answers WHERE attempt_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    statuses.put(rs.getInt("question_id"), rs.getBoolean("is_answered"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] getQuestionPaletteStatuses error: " + e.getMessage());
        }
        return statuses;
    }

    /**
     * Retrieves detailed AttemptAnswer entity records for an attempt (used in post-exam breakdown review).
     */
    public List<AttemptAnswer> getAttemptAnswerRecords(int attemptId) {
        List<AttemptAnswer> records = new ArrayList<>();
        String sql = "SELECT answer_id, attempt_id, question_id, selected_option, is_correct, answered_at " +
                     "FROM attempt_answers WHERE attempt_id = ? ORDER BY question_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AttemptAnswer record = new AttemptAnswer();
                    record.setAnswerId(rs.getInt("answer_id"));
                    record.setAttemptId(rs.getInt("attempt_id"));
                    record.setQuestionId(rs.getInt("question_id"));
                    record.setSelectedOption(rs.getString("selected_option"));
                    boolean isCorr = rs.getBoolean("is_correct");
                    if (!rs.wasNull()) record.setIsCorrect(isCorr);
                    record.setAnsweredAt(rs.getTimestamp("answered_at"));
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] getAttemptAnswerRecords error: " + e.getMessage());
        }
        return records;
    }
}

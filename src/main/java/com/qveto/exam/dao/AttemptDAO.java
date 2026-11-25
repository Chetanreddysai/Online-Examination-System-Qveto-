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

    /**
     * Submits an active in-progress attempt manually with pessimistic row-level locking.
     */
    public boolean submitAttempt(int attemptId, double score, double percentage, boolean passed) {
        return finalizeAttemptWithLock(attemptId, SubmissionType.MANUAL, AttemptStatus.SUBMITTED,
                score, percentage, passed);
    }

    /**
     * Auto-submits an active attempt upon countdown timer expiry with pessimistic row-level locking.
     */
    public boolean timeoutAttempt(int attemptId, double score, double percentage, boolean passed) {
        return finalizeAttemptWithLock(attemptId, SubmissionType.AUTO_TIMEOUT, AttemptStatus.TIMED_OUT,
                score, percentage, passed);
    }

    /**
     * Locks and marks an active attempt as TIMED_OUT with AUTO_TIMEOUT before or independent of grading.
     */
    public boolean timeoutAttempt(int attemptId) {
        return finalizeAttemptWithLock(attemptId, SubmissionType.AUTO_TIMEOUT, AttemptStatus.TIMED_OUT,
                null, null, null);
    }

    /**
     * Locks and marks an interrupted attempt as ABANDONED without a submission type.
     */
    public boolean abandonAttempt(int attemptId) {
        return finalizeAttemptWithLock(attemptId, null, AttemptStatus.ABANDONED,
                null, null, null);
    }

    /**
     * Finalizes an attempt using explicit submission type and terminal status under row-level locking.
     */
    public boolean completeAttempt(int attemptId, SubmissionType submissionType, AttemptStatus status,
                                   double score, double percentage, boolean passed) {
        AttemptStatus targetStatus = status;
        if (targetStatus == null) {
            targetStatus = (submissionType == SubmissionType.AUTO_TIMEOUT)
                    ? AttemptStatus.TIMED_OUT
                    : AttemptStatus.SUBMITTED;
        }

        SubmissionType effectiveSubmissionType = submissionType;
        if (targetStatus == AttemptStatus.ABANDONED) {
            effectiveSubmissionType = null;
        } else if (effectiveSubmissionType == null) {
            effectiveSubmissionType = (targetStatus == AttemptStatus.TIMED_OUT)
                    ? SubmissionType.AUTO_TIMEOUT
                    : SubmissionType.MANUAL;
        }

        return finalizeAttemptWithLock(attemptId, effectiveSubmissionType, targetStatus,
                score, percentage, passed);
    }

    /**
     * Performs a row-locked state transition from IN_PROGRESS to a terminal state.
     * Prevents race conditions between simultaneous manual submission and timer auto-timeout.
     */
    private boolean finalizeAttemptWithLock(int attemptId, SubmissionType submissionType, AttemptStatus targetStatus,
                                            Double score, Double percentage, Boolean passed) {
        if (targetStatus == null || !targetStatus.isTerminal()) {
            System.err.println("[AttemptDAO] Invalid non-terminal target status for finalization: " + targetStatus);
            return false;
        }

        String lockSql = "SELECT status FROM exam_attempts WHERE attempt_id = ? FOR UPDATE";
        String updateSql = "UPDATE exam_attempts SET end_time = CURRENT_TIMESTAMP, submission_type = ?, " +
                           "status = ?, score = ?, percentage = ?, passed = ? " +
                           "WHERE attempt_id = ? AND status = 'IN_PROGRESS'";

        Connection conn = null;
        boolean previousAutoCommit = true;

        try {
            conn = DBConnection.getConnection();
            previousAutoCommit = conn.getAutoCommit();
            // Hold InnoDB row lock across SELECT FOR UPDATE and UPDATE within one transaction
            conn.setAutoCommit(false);

            AttemptStatus currentStatus = null;
            try (PreparedStatement lockStmt = conn.prepareStatement(lockSql)) {
                lockStmt.setInt(1, attemptId);
                try (ResultSet rs = lockStmt.executeQuery()) {
                    if (rs.next()) {
                        currentStatus = AttemptStatus.fromString(rs.getString("status"));
                    }
                }
            }

            if (currentStatus == null) {
                conn.rollback();
                return false;
            }
            if (currentStatus.isTerminal() || !currentStatus.canTransitionTo(targetStatus)) {
                System.err.println("[AttemptDAO] Attempt " + attemptId + " is already locked in terminal state: " + currentStatus);
                conn.rollback();
                return false;
            }

            int updatedRows;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                if (submissionType != null) {
                    updateStmt.setString(1, submissionType.name());
                } else {
                    updateStmt.setNull(1, Types.VARCHAR);
                }
                updateStmt.setString(2, targetStatus.name());

                if (score != null) {
                    updateStmt.setDouble(3, score);
                } else {
                    updateStmt.setNull(3, Types.DECIMAL);
                }
                if (percentage != null) {
                    updateStmt.setDouble(4, percentage);
                } else {
                    updateStmt.setNull(4, Types.DECIMAL);
                }
                if (passed != null) {
                    updateStmt.setBoolean(5, passed);
                } else {
                    updateStmt.setNull(5, Types.BOOLEAN);
                }

                updateStmt.setInt(6, attemptId);
                updatedRows = updateStmt.executeUpdate();
            }

            if (updatedRows > 0) {
                conn.commit();
                return true;
            }
            conn.rollback();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[AttemptDAO] finalizeAttemptWithLock rollback error: " + rollbackEx.getMessage());
                }
            }
            System.err.println("[AttemptDAO] finalizeAttemptWithLock error: " + e.getMessage());
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
        return false;
    }

    /**
     * Bulk auto-timeout update for stale IN_PROGRESS attempts whose exam duration has elapsed.
     *
     * @return number of overdue attempts transitioned to TIMED_OUT
     */
    public int expireTimedOutAttempts() {
        String sql = "UPDATE exam_attempts a " +
                     "INNER JOIN exams e ON a.exam_id = e.exam_id " +
                     "SET a.end_time = CURRENT_TIMESTAMP, " +
                     "    a.submission_type = 'AUTO_TIMEOUT', " +
                     "    a.status = 'TIMED_OUT' " +
                     "WHERE a.status = 'IN_PROGRESS' " +
                     "  AND TIMESTAMPDIFF(SECOND, a.start_time, CURRENT_TIMESTAMP) >= (e.duration_minutes * 60)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AttemptDAO] expireTimedOutAttempts error: " + e.getMessage());
        }
        return 0;
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
     * Checks if the attempt is locked in a terminal state (SUBMITTED, TIMED_OUT, or ABANDONED).
     */
    public boolean isAttemptLocked(int attemptId) {
        AttemptStatus status = getAttemptStatus(attemptId);
        return status != null && status.isTerminal();
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

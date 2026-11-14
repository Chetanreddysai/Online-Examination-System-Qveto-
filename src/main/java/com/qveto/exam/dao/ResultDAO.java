package com.qveto.exam.dao;

import com.qveto.exam.model.ExamResult;
import com.qveto.exam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for final examination scores and reports.
 */
public class ResultDAO {

    public boolean saveResult(ExamResult result) {
        String sql = "INSERT INTO exam_results (attempt_id, user_id, exam_id, total_questions, " +
                     "answered_count, correct_count, incorrect_count, score, total_marks, percentage, passed) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
        } catch (SQLException e) {
            System.err.println("[ResultDAO] saveResult error: " + e.getMessage());
        }
        return false;
    }

    public List<ExamResult> getResultsByUser(int userId) {
        List<ExamResult> results = new ArrayList<>();
        String sql = "SELECT r.result_id, r.attempt_id, r.user_id, r.exam_id, e.title as exam_title, " +
                     "u.full_name as student_name, r.total_questions, r.answered_count, r.correct_count, " +
                     "r.incorrect_count, r.score, r.total_marks, r.percentage, r.passed, r.graded_at " +
                     "FROM exam_results r " +
                     "JOIN exams e ON r.exam_id = e.exam_id " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.user_id = ? ORDER BY r.graded_at DESC";

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
        String sql = "SELECT r.result_id, r.attempt_id, r.user_id, r.exam_id, e.title as exam_title, " +
                     "u.full_name as student_name, r.total_questions, r.answered_count, r.correct_count, " +
                     "r.incorrect_count, r.score, r.total_marks, r.percentage, r.passed, r.graded_at " +
                     "FROM exam_results r " +
                     "JOIN exams e ON r.exam_id = e.exam_id " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "ORDER BY r.graded_at DESC";

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

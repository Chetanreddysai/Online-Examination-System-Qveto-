package com.qveto.exam.dao;

import com.qveto.exam.model.Exam;
import com.qveto.exam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Examination metadata and records.
 */
public class ExamDAO {

    public List<Exam> getAllActiveExams() {
        List<Exam> exams = new ArrayList<>();
        String sql = "SELECT exam_id, title, description, duration_minutes, total_questions, " +
                     "total_marks, passing_percentage, is_active, created_at " +
                     "FROM exams WHERE is_active = TRUE ORDER BY exam_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                exams.add(mapResultSetToExam(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ExamDAO] getAllActiveExams error: " + e.getMessage());
        }
        return exams;
    }

    public List<Exam> getAllExams() {
        List<Exam> exams = new ArrayList<>();
        String sql = "SELECT exam_id, title, description, duration_minutes, total_questions, " +
                     "total_marks, passing_percentage, is_active, created_at " +
                     "FROM exams ORDER BY exam_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                exams.add(mapResultSetToExam(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ExamDAO] getAllExams error: " + e.getMessage());
        }
        return exams;
    }

    public Exam getExamById(int examId) {
        String sql = "SELECT exam_id, title, description, duration_minutes, total_questions, " +
                     "total_marks, passing_percentage, is_active, created_at " +
                     "FROM exams WHERE exam_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExam(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ExamDAO] getExamById error: " + e.getMessage());
        }
        return null;
    }

    public boolean createExam(Exam exam) {
        String sql = "INSERT INTO exams (title, description, duration_minutes, total_questions, " +
                     "total_marks, passing_percentage, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, exam.getTitle().trim());
            stmt.setString(2, exam.getDescription());
            stmt.setInt(3, exam.getDurationMinutes());
            stmt.setInt(4, exam.getTotalQuestions());
            stmt.setDouble(5, exam.getTotalMarks());
            stmt.setDouble(6, exam.getPassingPercentage());
            stmt.setBoolean(7, exam.isActive());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        exam.setExamId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ExamDAO] createExam error: " + e.getMessage());
        }
        return false;
    }

    private Exam mapResultSetToExam(ResultSet rs) throws SQLException {
        Exam exam = new Exam();
        exam.setExamId(rs.getInt("exam_id"));
        exam.setTitle(rs.getString("title"));
        exam.setDescription(rs.getString("description"));
        exam.setDurationMinutes(rs.getInt("duration_minutes"));
        exam.setTotalQuestions(rs.getInt("total_questions"));
        exam.setTotalMarks(rs.getDouble("total_marks"));
        exam.setPassingPercentage(rs.getDouble("passing_percentage"));
        exam.setActive(rs.getBoolean("is_active"));
        exam.setCreatedAt(rs.getTimestamp("created_at"));
        return exam;
    }
}

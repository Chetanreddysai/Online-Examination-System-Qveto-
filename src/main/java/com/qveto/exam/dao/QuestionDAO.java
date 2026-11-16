package com.qveto.exam.dao;

import com.qveto.exam.model.Question;
import com.qveto.exam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the reusable question bank and exam question mappings.
 */
public class QuestionDAO {

    /**
     * Retrieves all questions mapped to a specific exam, ordered by question_order.
     */
    public List<Question> getQuestionsForExam(int examId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.question_id, q.subject, q.question_text, q.option_a, q.option_b, " +
                     "q.option_c, q.option_d, q.correct_option, q.marks, q.created_at " +
                     "FROM questions q " +
                     "INNER JOIN exam_questions eq ON q.question_id = eq.question_id " +
                     "WHERE eq.exam_id = ? " +
                     "ORDER BY eq.question_order ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapResultSetToQuestion(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] getQuestionsForExam error: " + e.getMessage());
        }
        return questions;
    }

    /**
     * Retrieves all questions in the entire question bank.
     */
    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT question_id, subject, question_text, option_a, option_b, " +
                     "option_c, option_d, correct_option, marks, created_at " +
                     "FROM questions ORDER BY question_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] getAllQuestions error: " + e.getMessage());
        }
        return questions;
    }

    /**
     * Adds a new multiple-choice question to the question bank.
     */
    public boolean addQuestion(Question question) {
        String sql = "INSERT INTO questions (subject, question_text, option_a, option_b, " +
                     "option_c, option_d, correct_option, marks) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, question.getSubject().trim());
            stmt.setString(2, question.getQuestionText().trim());
            stmt.setString(3, question.getOptionA().trim());
            stmt.setString(4, question.getOptionB().trim());
            stmt.setString(5, question.getOptionC().trim());
            stmt.setString(6, question.getOptionD().trim());
            stmt.setString(7, question.getCorrectOption().toUpperCase());
            stmt.setDouble(8, question.getMarks());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        question.setQuestionId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] addQuestion error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Maps a question to an exam.
     */
    public boolean linkQuestionToExam(int examId, int questionId, int order) {
        String sql = "INSERT INTO exam_questions (exam_id, question_id, question_order) " +
                     "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE question_order = VALUES(question_order)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
            stmt.setInt(2, questionId);
            stmt.setInt(3, order);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] linkQuestionToExam error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a question from the question bank.
     */
    public boolean deleteQuestion(int questionId) {
        String sql = "DELETE FROM questions WHERE question_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, questionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] deleteQuestion error: " + e.getMessage());
        }
        return false;
    }

    private Question mapResultSetToQuestion(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setQuestionId(rs.getInt("question_id"));
        q.setSubject(rs.getString("subject"));
        q.setQuestionText(rs.getString("question_text"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setCorrectOption(rs.getString("correct_option"));
        q.setMarks(rs.getDouble("marks"));
        q.setCreatedAt(rs.getTimestamp("created_at"));
        return q;
    }
}

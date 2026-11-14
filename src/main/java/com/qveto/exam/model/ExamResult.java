package com.qveto.exam.model;

import java.sql.Timestamp;

/**
 * ExamResult entity representing computed score, accuracy, and performance summary.
 */
public class ExamResult {
    private int resultId;
    private int attemptId;
    private int userId;
    private int examId;
    private String examTitle;
    private String studentName;
    private int totalQuestions;
    private int answeredCount;
    private int correctCount;
    private int incorrectCount;
    private double score;
    private double totalMarks;
    private double percentage;
    private boolean passed;
    private Timestamp gradedAt;

    public ExamResult() {
    }

    public ExamResult(int resultId, int attemptId, int userId, int examId, int totalQuestions,
                      int answeredCount, int correctCount, int incorrectCount,
                      double score, double totalMarks, double percentage, boolean passed, Timestamp gradedAt) {
        this.resultId = resultId;
        this.attemptId = attemptId;
        this.userId = userId;
        this.examId = examId;
        this.totalQuestions = totalQuestions;
        this.answeredCount = answeredCount;
        this.correctCount = correctCount;
        this.incorrectCount = incorrectCount;
        this.score = score;
        this.totalMarks = totalMarks;
        this.percentage = percentage;
        this.passed = passed;
        this.gradedAt = gradedAt;
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getAnsweredCount() {
        return answeredCount;
    }

    public void setAnsweredCount(int answeredCount) {
        this.answeredCount = answeredCount;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getIncorrectCount() {
        return incorrectCount;
    }

    public void setIncorrectCount(int incorrectCount) {
        this.incorrectCount = incorrectCount;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public Timestamp getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(Timestamp gradedAt) {
        this.gradedAt = gradedAt;
    }
}

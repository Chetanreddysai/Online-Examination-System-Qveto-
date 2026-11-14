package com.qveto.exam.model;

import java.sql.Timestamp;

/**
 * Exam entity defining an examination paper.
 */
public class Exam {
    private int examId;
    private String title;
    private String description;
    private int durationMinutes;
    private int totalQuestions;
    private double totalMarks;
    private double passingPercentage;
    private boolean isActive;
    private Timestamp createdAt;

    public Exam() {
        this.isActive = true;
    }

    public Exam(int examId, String title, String description, int durationMinutes, int totalQuestions, double totalMarks, double passingPercentage, boolean isActive) {
        this.examId = examId;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.totalQuestions = totalQuestions;
        this.totalMarks = totalMarks;
        this.passingPercentage = passingPercentage;
        this.isActive = isActive;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public double getPassingPercentage() {
        return passingPercentage;
    }

    public void setPassingPercentage(double passingPercentage) {
        this.passingPercentage = passingPercentage;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return title + " (" + durationMinutes + " mins, " + totalMarks + " marks)";
    }
}

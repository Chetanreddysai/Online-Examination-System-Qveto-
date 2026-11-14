package com.qveto.exam.model;

import java.sql.Timestamp;

/**
 * ExamAttempt entity representing an individual student test-taking session.
 */
public class ExamAttempt {
    private int attemptId;
    private int userId;
    private int examId;
    private Timestamp startTime;
    private Timestamp endTime;
    private SubmissionType submissionType;
    private AttemptStatus status;
    private Double score;
    private Double percentage;
    private Boolean passed;

    public ExamAttempt() {
        this.status = AttemptStatus.IN_PROGRESS;
    }

    public ExamAttempt(int attemptId, int userId, int examId, Timestamp startTime, AttemptStatus status) {
        this.attemptId = attemptId;
        this.userId = userId;
        this.examId = examId;
        this.startTime = startTime;
        this.status = status;
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

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public SubmissionType getSubmissionType() {
        return submissionType;
    }

    public void setSubmissionType(SubmissionType submissionType) {
        this.submissionType = submissionType;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public boolean isCompleted() {
        return status == AttemptStatus.SUBMITTED || status == AttemptStatus.TIMED_OUT;
    }
}

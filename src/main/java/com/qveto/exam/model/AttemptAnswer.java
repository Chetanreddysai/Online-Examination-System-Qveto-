package com.qveto.exam.model;

import java.sql.Timestamp;

/**
 * AttemptAnswer entity representing an answer selected by a student for a question during an exam.
 */
public class AttemptAnswer {
    private int answerId;
    private int attemptId;
    private int questionId;
    private String selectedOption; // 'A', 'B', 'C', 'D' or null if skipped
    private Boolean isCorrect;
    private Timestamp answeredAt;

    public AttemptAnswer() {
    }

    public AttemptAnswer(int attemptId, int questionId, String selectedOption) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedOption = selectedOption;
    }

    public AttemptAnswer(int answerId, int attemptId, int questionId, String selectedOption, Boolean isCorrect, Timestamp answeredAt) {
        this.answerId = answerId;
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedOption = selectedOption;
        this.isCorrect = isCorrect;
        this.answeredAt = answeredAt;
    }

    public int getAnswerId() {
        return answerId;
    }

    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }

    public Boolean isCorrect() {
        return isCorrect;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public void setIsCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public Timestamp getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(Timestamp answeredAt) {
        this.answeredAt = answeredAt;
    }
}

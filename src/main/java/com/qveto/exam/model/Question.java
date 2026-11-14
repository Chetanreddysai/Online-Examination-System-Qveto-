package com.qveto.exam.model;

import java.sql.Timestamp;

/**
 * Question entity representing a multiple-choice question in the reusable question bank.
 */
public class Question {
    private int questionId;
    private String subject;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption; // 'A', 'B', 'C', 'D'
    private double marks;
    private Timestamp createdAt;

    public Question() {
        this.marks = 1.0;
    }

    public Question(int questionId, String subject, String questionText, String optionA, String optionB, String optionC, String optionD, String correctOption, double marks) {
        this.questionId = questionId;
        this.subject = subject;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.marks = marks;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getOptionText(String optionKey) {
        if ("A".equalsIgnoreCase(optionKey)) return optionA;
        if ("B".equalsIgnoreCase(optionKey)) return optionB;
        if ("C".equalsIgnoreCase(optionKey)) return optionC;
        if ("D".equalsIgnoreCase(optionKey)) return optionD;
        return "";
    }

    @Override
    public String toString() {
        return "[" + subject + "] " + questionText;
    }
}

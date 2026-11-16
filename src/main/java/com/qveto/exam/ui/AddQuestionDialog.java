package com.qveto.exam.ui;

import com.qveto.exam.model.Exam;
import com.qveto.exam.model.Question;
import com.qveto.exam.service.ExamService;
import com.qveto.exam.util.AppConstants;
import com.qveto.exam.validation.ValidationResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog for authoring multiple-choice questions with 4 options,
 * correct answer selection, marks, and optional exam association.
 */
public class AddQuestionDialog extends JDialog {

    private final ExamService examService;
    private final Runnable onSuccessCallback;

    private JComboBox<String> subjectCombo;
    private JTextArea questionTextArea;
    private JTextField optionAField;
    private JTextField optionBField;
    private JTextField optionCField;
    private JTextField optionDField;
    private JComboBox<String> correctOptionCombo;
    private JTextField marksField;
    private JComboBox<ExamItem> examTargetCombo;
    private JLabel errorLabel;

    private static class ExamItem {
        final int id;
        final String title;

        ExamItem(int id, String title) {
            this.id = id;
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public AddQuestionDialog(Frame owner, ExamService examService, Runnable onSuccessCallback) {
        super(owner, "Author Question for Question Bank", true);
        this.examService = examService;
        this.onSuccessCallback = onSuccessCallback;

        setSize(580, 680);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppConstants.COLOR_CARD_BG);
        mainPanel.setBorder(new EmptyBorder(18, 24, 18, 24));

        // Header
        JLabel header = new JLabel("Author Multiple-Choice Question (MCQ)");
        header.setFont(AppConstants.FONT_HEADER);
        header.setForeground(AppConstants.COLOR_SECONDARY);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subheader = new JLabel("Questions authored here are reusable across multiple exams");
        subheader.setFont(AppConstants.FONT_SMALL);
        subheader.setForeground(AppConstants.COLOR_TEXT_MUTED);
        subheader.setAlignmentX(Component.LEFT_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppConstants.FONT_BODY);
        errorLabel.setForeground(AppConstants.COLOR_DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Subject selector
        String[] subjects = {"Java Programming", "Database Systems", "Computer Networks", "Data Structures", "Operating Systems", "Web Development"};
        subjectCombo = new JComboBox<>(subjects);
        subjectCombo.setEditable(true);
        subjectCombo.setFont(AppConstants.FONT_BODY);
        subjectCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        subjectCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Question text
        questionTextArea = new JTextArea(3, 20);
        questionTextArea.setFont(AppConstants.FONT_BODY);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        JScrollPane qScroll = new JScrollPane(questionTextArea);
        qScroll.setBorder(new LineBorder(AppConstants.COLOR_BORDER, 1));
        qScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        qScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // Option fields
        optionAField = createTextField();
        optionBField = createTextField();
        optionCField = createTextField();
        optionDField = createTextField();

        // Correct Option & Marks Row
        String[] options = {"A", "B", "C", "D"};
        correctOptionCombo = new JComboBox<>(options);
        correctOptionCombo.setFont(AppConstants.FONT_BODY_BOLD);
        correctOptionCombo.setMaximumSize(new Dimension(100, 32));

        marksField = createTextField();
        marksField.setText("10.0");
        marksField.setMaximumSize(new Dimension(100, 32));

        // Exam Target Combo
        examTargetCombo = new JComboBox<>();
        examTargetCombo.addItem(new ExamItem(0, "-- Reusable Question Bank Only (No Direct Assignment) --"));
        List<Exam> exams = examService.getAvailableExams();
        for (Exam ex : exams) {
            examTargetCombo.addItem(new ExamItem(ex.getExamId(), ex.getTitle() + " (ID: " + ex.getExamId() + ")"));
        }
        examTargetCombo.setFont(AppConstants.FONT_BODY);
        examTargetCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        examTargetCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Bottom Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(AppConstants.FONT_BODY);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("Save Question");
        saveBtn.setFont(AppConstants.FONT_BODY_BOLD);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(AppConstants.COLOR_SECONDARY);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> saveQuestion());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        // Assemble Layout
        mainPanel.add(header);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        mainPanel.add(subheader);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        mainPanel.add(errorLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        mainPanel.add(createFieldLabel("Subject Category:"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        mainPanel.add(subjectCombo);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        mainPanel.add(createFieldLabel("Question Statement:"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        mainPanel.add(qScroll);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        mainPanel.add(createFieldLabel("Option A:"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        mainPanel.add(optionAField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        mainPanel.add(createFieldLabel("Option B:"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        mainPanel.add(optionBField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        mainPanel.add(createFieldLabel("Option C:"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        mainPanel.add(optionCField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        mainPanel.add(createFieldLabel("Option D:"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        mainPanel.add(optionDField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel bottomOptions = new JPanel(new GridLayout(1, 2, 12, 0));
        bottomOptions.setOpaque(false);
        bottomOptions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        bottomOptions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel corPan = new JPanel(new BorderLayout(0, 2)); corPan.setOpaque(false);
        corPan.add(createFieldLabel("Correct Answer:"), BorderLayout.NORTH);
        corPan.add(correctOptionCombo, BorderLayout.CENTER);

        JPanel markPan = new JPanel(new BorderLayout(0, 2)); markPan.setOpaque(false);
        markPan.add(createFieldLabel("Marks / Points:"), BorderLayout.NORTH);
        markPan.add(marksField, BorderLayout.CENTER);

        bottomOptions.add(corPan);
        bottomOptions.add(markPan);
        mainPanel.add(bottomOptions);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        mainPanel.add(createFieldLabel("Assign to Examination Paper:"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        mainPanel.add(examTargetCombo);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        mainPanel.add(buttonPanel);

        getContentPane().add(mainPanel);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.FONT_BODY_BOLD);
        lbl.setForeground(AppConstants.COLOR_TEXT_PRIMARY);
        return lbl;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(AppConstants.FONT_BODY);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private void saveQuestion() {
        Object subjObj = subjectCombo.getSelectedItem();
        String subject = subjObj != null ? subjObj.toString().trim() : "";
        String text = questionTextArea.getText().trim();
        String optA = optionAField.getText().trim();
        String optB = optionBField.getText().trim();
        String optC = optionCField.getText().trim();
        String optD = optionDField.getText().trim();
        String correct = (String) correctOptionCombo.getSelectedItem();
        double marks = 10.0;

        try {
            marks = Double.parseDouble(marksField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Marks must be a valid numeric value.");
            return;
        }

        Question question = new Question(0, subject, text, optA, optB, optC, optD, correct, marks);
        ExamItem selectedExam = (ExamItem) examTargetCombo.getSelectedItem();
        Integer examId = (selectedExam != null && selectedExam.id > 0) ? selectedExam.id : null;

        ValidationResult res = examService.addQuestionToBank(question, examId);
        if (!res.isValid()) {
            errorLabel.setText(res.getFirstError());
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Question added successfully to Question Bank" +
                (examId != null ? " and linked to " + selectedExam.title : "") + "!",
                "Question Saved",
                JOptionPane.INFORMATION_MESSAGE);

        if (onSuccessCallback != null) {
            onSuccessCallback.run();
        }
        dispose();
    }
}

package com.qveto.exam.ui;

import com.qveto.exam.model.Exam;
import com.qveto.exam.service.ExamService;
import com.qveto.exam.util.AppConstants;
import com.qveto.exam.validation.ValidationResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modal dialog enabling Administrators to create and configure new examination papers.
 */
public class CreateExamDialog extends JDialog {

    private final ExamService examService;
    private final Runnable onSuccessCallback;

    private JTextField titleField;
    private JTextArea descArea;
    private JSpinner durationSpinner;
    private JSpinner questionsSpinner;
    private JTextField marksField;
    private JTextField passPctField;
    private JLabel errorLabel;

    public CreateExamDialog(Frame owner, ExamService examService, Runnable onSuccessCallback) {
        super(owner, "Create New Examination Paper", true);
        this.examService = examService;
        this.onSuccessCallback = onSuccessCallback;

        setSize(520, 560);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppConstants.COLOR_CARD_BG);
        mainPanel.setBorder(new EmptyBorder(20, 28, 20, 28));

        // Header
        JLabel header = new JLabel("New Examination Configuration");
        header.setFont(AppConstants.FONT_HEADER);
        header.setForeground(AppConstants.COLOR_PRIMARY);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subheader = new JLabel("Define duration, question count, and scoring criteria");
        subheader.setFont(AppConstants.FONT_SMALL);
        subheader.setForeground(AppConstants.COLOR_TEXT_MUTED);
        subheader.setAlignmentX(Component.LEFT_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppConstants.FONT_BODY);
        errorLabel.setForeground(AppConstants.COLOR_DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fields
        titleField = createTextField();
        descArea = new JTextArea(3, 20);
        descArea.setFont(AppConstants.FONT_BODY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(new LineBorder(AppConstants.COLOR_BORDER, 1));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        durationSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 300, 5));
        durationSpinner.setFont(AppConstants.FONT_BODY);
        durationSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        durationSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        questionsSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        questionsSpinner.setFont(AppConstants.FONT_BODY);
        questionsSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        questionsSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        marksField = createTextField();
        marksField.setText("100.0");

        passPctField = createTextField();
        passPctField.setText("50.0");

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(AppConstants.FONT_BODY);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("Save & Publish Exam");
        saveBtn.setFont(AppConstants.FONT_BODY_BOLD);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(AppConstants.COLOR_PRIMARY);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> saveExam());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        // Grid for Numbers
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 8));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel p1 = new JPanel(new BorderLayout(0, 4)); p1.setOpaque(false);
        p1.add(createFieldLabel("Duration (Minutes):"), BorderLayout.NORTH);
        p1.add(durationSpinner, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout(0, 4)); p2.setOpaque(false);
        p2.add(createFieldLabel("Total Questions:"), BorderLayout.NORTH);
        p2.add(questionsSpinner, BorderLayout.CENTER);

        JPanel p3 = new JPanel(new BorderLayout(0, 4)); p3.setOpaque(false);
        p3.add(createFieldLabel("Total Marks:"), BorderLayout.NORTH);
        p3.add(marksField, BorderLayout.CENTER);

        JPanel p4 = new JPanel(new BorderLayout(0, 4)); p4.setOpaque(false);
        p4.add(createFieldLabel("Passing Percentage (%):"), BorderLayout.NORTH);
        p4.add(passPctField, BorderLayout.CENTER);

        grid.add(p1);
        grid.add(p2);
        grid.add(p3);
        grid.add(p4);

        // Assemble Form
        mainPanel.add(header);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        mainPanel.add(subheader);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        mainPanel.add(errorLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        mainPanel.add(createFieldLabel("Exam Title:"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        mainPanel.add(titleField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        mainPanel.add(createFieldLabel("Description / Instructions:"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        mainPanel.add(descScroll);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        mainPanel.add(grid);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

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
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private void saveExam() {
        String title = titleField.getText().trim();
        String desc = descArea.getText().trim();
        int duration = (Integer) durationSpinner.getValue();
        int totalQ = (Integer) questionsSpinner.getValue();
        double totalMarks;
        double passPct;

        try {
            totalMarks = Double.parseDouble(marksField.getText().trim());
            passPct = Double.parseDouble(passPctField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Total marks and passing percentage must be valid numbers.");
            return;
        }

        Exam exam = new Exam(0, title, desc, duration, totalQ, totalMarks, passPct, true);
        ValidationResult res = examService.createExam(exam);

        if (!res.isValid()) {
            errorLabel.setText(res.getFirstError());
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Examination '" + title + "' has been successfully created and published!",
                "Exam Created",
                JOptionPane.INFORMATION_MESSAGE);

        if (onSuccessCallback != null) {
            onSuccessCallback.run();
        }
        dispose();
    }
}

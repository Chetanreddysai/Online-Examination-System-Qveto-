package com.qveto.exam.ui;

import com.qveto.exam.model.Exam;
import com.qveto.exam.util.AppConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modal dialog for reviewing examination parameters, rules, and syllabus details.
 */
public class ExamDetailsDialog extends JDialog {

    public ExamDetailsDialog(Frame owner, Exam exam) {
        super(owner, "Examination Details & Syllabus Overview", true);
        setSize(480, 460);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI(exam);
    }

    private void initUI(Exam exam) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppConstants.COLOR_CARD_BG);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel(exam != null ? exam.getTitle() : "Examination");
        title.setFont(AppConstants.FONT_HEADER);
        title.setForeground(AppConstants.COLOR_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Official Assessment Instructions & Candidate Guidelines");
        sub.setFont(AppConstants.FONT_SMALL);
        sub.setForeground(AppConstants.COLOR_TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Details grid
        JPanel grid = new JPanel(new GridLayout(4, 2, 8, 8));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        grid.add(createLabel("Paper Duration:"));
        grid.add(createValue((exam != null ? exam.getDurationMinutes() : 0) + " Minutes"));

        grid.add(createLabel("Total Questions:"));
        grid.add(createValue((exam != null ? exam.getTotalQuestions() : 0) + " Multiple Choice"));

        grid.add(createLabel("Maximum Marks:"));
        grid.add(createValue((exam != null ? exam.getTotalMarks() : 0.0) + " Points"));

        grid.add(createLabel("Passing Threshold:"));
        grid.add(createValue((exam != null ? exam.getPassingPercentage() : 0.0) + "%"));

        // Description / Syllabus
        JLabel descHeader = new JLabel("Syllabus & Coverage:");
        descHeader.setFont(AppConstants.FONT_BODY_BOLD);
        descHeader.setForeground(AppConstants.COLOR_TEXT_PRIMARY);
        descHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea descArea = new JTextArea(exam != null ? exam.getDescription() : "Standard Multiple Choice Examination");
        descArea.setFont(AppConstants.FONT_BODY);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(new Color(248, 250, 252));
        descArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(descArea);
        scroll.setBorder(new LineBorder(AppConstants.COLOR_BORDER, 1));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JButton closeBtn = new JButton("Close Preview");
        closeBtn.setFont(AppConstants.FONT_BODY_BOLD);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(AppConstants.COLOR_PRIMARY);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        closeBtn.addActionListener(e -> dispose());

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(sub);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));
        panel.add(grid);
        panel.add(Box.createRigidArea(new Dimension(0, 14)));
        panel.add(descHeader);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(scroll);
        panel.add(Box.createRigidArea(new Dimension(0, 18)));
        panel.add(closeBtn);

        getContentPane().add(panel);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConstants.FONT_BODY_BOLD);
        l.setForeground(AppConstants.COLOR_TEXT_PRIMARY);
        return l;
    }

    private JLabel createValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConstants.FONT_BODY);
        l.setForeground(AppConstants.COLOR_TEXT_MUTED);
        return l;
    }
}

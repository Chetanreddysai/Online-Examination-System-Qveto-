package com.qveto.exam.ui;

import com.qveto.exam.model.Exam;
import com.qveto.exam.model.ExamResult;
import com.qveto.exam.model.Question;
import com.qveto.exam.service.ExamService;
import com.qveto.exam.util.AppConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Modal dialog displaying detailed question-by-question breakdown
 * for any past examination attempt.
 */
public class AttemptReviewDialog extends JDialog {

    public AttemptReviewDialog(Frame owner, ExamResult result, ExamService examService) {
        super(owner, "Assessment Performance Report", true);
        setSize(780, 560);
        setLocationRelativeTo(owner);
        initUI(result, examService);
    }

    private void initUI(ExamResult result, ExamService examService) {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(AppConstants.COLOR_CARD_BG);
        mainPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        // 1. Header with Result Summary
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);

        JLabel titleLbl = new JLabel(result.getExamTitle() != null ? result.getExamTitle() : "Assessment Performance");
        titleLbl.setFont(AppConstants.FONT_HEADER);
        titleLbl.setForeground(AppConstants.COLOR_PRIMARY);

        String candidateInfo = (result.getStudentName() != null ? "Candidate: " + result.getStudentName() + " | " : "") +
                "Date: " + (result.getGradedAt() != null ? result.getGradedAt() : "Completed");
        JLabel subLbl = new JLabel(candidateInfo);
        subLbl.setFont(AppConstants.FONT_SMALL);
        subLbl.setForeground(AppConstants.COLOR_TEXT_MUTED);

        titles.add(titleLbl);
        titles.add(subLbl);

        // Status Badge
        JLabel badge = new JLabel(result.isPassed() ? "  PASSED  " : "  FAILED  ", JLabel.CENTER);
        badge.setFont(AppConstants.FONT_BODY_BOLD);
        badge.setOpaque(true);
        if (result.isPassed()) {
            badge.setBackground(new Color(236, 253, 245));
            badge.setForeground(new Color(6, 95, 70));
            badge.setBorder(new LineBorder(new Color(167, 243, 208), 1, true));
        } else {
            badge.setBackground(new Color(254, 242, 242));
            badge.setForeground(new Color(153, 27, 27));
            badge.setBorder(new LineBorder(new Color(254, 202, 202), 1, true));
        }

        headerPanel.add(titles, BorderLayout.WEST);
        headerPanel.add(badge, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Metrics Bar
        JPanel metricsBar = new JPanel(new GridLayout(1, 4, 10, 0));
        metricsBar.setOpaque(false);
        metricsBar.setBorder(new EmptyBorder(6, 0, 8, 0));

        metricsBar.add(createMiniCard("Score", String.format("%.1f / %.1f", result.getScore(), result.getTotalMarks())));
        metricsBar.add(createMiniCard("Percentage", String.format("%.1f%%", result.getPercentage())));
        metricsBar.add(createMiniCard("Correct", String.valueOf(result.getCorrectCount())));
        metricsBar.add(createMiniCard("Incorrect", String.valueOf(result.getIncorrectCount())));

        // 3. Question Review Table
        String[] cols = {"Q#", "Question Statement", "Candidate Answer", "Correct Answer", "Outcome", "Points"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(AppConstants.FONT_BODY);
        table.getTableHeader().setFont(AppConstants.FONT_BODY_BOLD);

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(360);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);
        table.getColumnModel().getColumn(4).setCellRenderer(center);
        table.getColumnModel().getColumn(5).setCellRenderer(center);

        // Load Questions & Answers
        List<Question> questions = examService.getQuestionsForExam(result.getExamId());
        Map<Integer, String> answers = examService.getAnswersForAttempt(result.getAttemptId());

        if (questions != null) {
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                String choice = answers != null ? answers.get(q.getQuestionId()) : null;
                boolean answered = (choice != null && !choice.trim().isEmpty());
                boolean correct = answered && choice.equalsIgnoreCase(q.getCorrectOption());

                String outcome = !answered ? "Skipped" : (correct ? "✔ Correct" : "✘ Incorrect");
                String points = correct ? String.format("+%.1f", q.getMarks()) : "0.0";

                model.addRow(new Object[]{
                        i + 1,
                        q.getQuestionText(),
                        answered ? "Option " + choice : "Skipped",
                        "Option " + q.getCorrectOption(),
                        outcome,
                        points
                });
            }
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(AppConstants.COLOR_BORDER, 1));

        JPanel centerContainer = new JPanel(new BorderLayout(0, 10));
        centerContainer.setOpaque(false);
        centerContainer.add(metricsBar, BorderLayout.NORTH);
        centerContainer.add(scroll, BorderLayout.CENTER);

        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // 4. Bottom Close Button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);

        JButton closeBtn = new JButton("Close Report");
        closeBtn.setFont(AppConstants.FONT_BODY_BOLD);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        bottomBar.add(closeBtn);

        mainPanel.add(bottomBar, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    private JPanel createMiniCard(String title, String val) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setBackground(new Color(249, 250, 251));
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel tLbl = new JLabel(title, JLabel.CENTER);
        tLbl.setFont(AppConstants.FONT_SMALL);
        tLbl.setForeground(AppConstants.COLOR_TEXT_MUTED);

        JLabel vLbl = new JLabel(val, JLabel.CENTER);
        vLbl.setFont(AppConstants.FONT_BODY_BOLD);
        vLbl.setForeground(AppConstants.COLOR_PRIMARY);

        p.add(tLbl);
        p.add(vLbl);
        return p;
    }
}

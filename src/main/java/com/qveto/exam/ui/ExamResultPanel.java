package com.qveto.exam.ui;

import com.qveto.exam.model.Exam;
import com.qveto.exam.model.ExamResult;
import com.qveto.exam.model.Question;
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
 * Result display panel presenting immediate candidate grading metrics,
 * pass/fail outcome badges, and detailed question-by-question review.
 */
public class ExamResultPanel extends JPanel {

    private final MainFrame mainFrame;

    private JLabel examTitleLabel;
    private JLabel statusBannerLabel;
    private JLabel scoreMetricLabel;
    private JLabel percentageMetricLabel;
    private JLabel accuracyMetricLabel;

    private JTable reviewTable;
    private DefaultTableModel reviewTableModel;

    public ExamResultPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.COLOR_BACKGROUND);

        // Top Navigation Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(AppConstants.COLOR_PRIMARY);
        topBar.setBorder(new EmptyBorder(14, 24, 14, 24));

        examTitleLabel = new JLabel("Examination Result Report");
        examTitleLabel.setFont(AppConstants.FONT_HEADER);
        examTitleLabel.setForeground(Color.WHITE);

        JButton returnBtn = new JButton("← Return to Dashboard");
        returnBtn.setFont(AppConstants.FONT_BODY_BOLD);
        returnBtn.setForeground(AppConstants.COLOR_PRIMARY);
        returnBtn.setBackground(Color.WHITE);
        returnBtn.setFocusPainted(false);
        returnBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        returnBtn.addActionListener(e -> mainFrame.showCard(AppConstants.CARD_STUDENT_DASHBOARD));

        topBar.add(examTitleLabel, BorderLayout.WEST);
        topBar.add(returnBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Content Container
        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setBackground(AppConstants.COLOR_BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Result Banner + KPI Metrics Container
        JPanel summaryContainer = new JPanel(new BorderLayout(0, 12));
        summaryContainer.setOpaque(false);

        // Outcome Banner
        statusBannerLabel = new JLabel("ASSESSMENT COMPLETED", JLabel.CENTER);
        statusBannerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusBannerLabel.setOpaque(true);
        statusBannerLabel.setBorder(new EmptyBorder(12, 16, 12, 16));
        summaryContainer.add(statusBannerLabel, BorderLayout.NORTH);

        // Metrics Grid (Score, Percentage, Accuracy)
        JPanel metricsGrid = new JPanel(new GridLayout(1, 3, 16, 0));
        metricsGrid.setOpaque(false);

        scoreMetricLabel = new JLabel("0.0 / 0.0", JLabel.CENTER);
        percentageMetricLabel = new JLabel("0.0%", JLabel.CENTER);
        accuracyMetricLabel = new JLabel("0 Correct / 0 Incorrect", JLabel.CENTER);

        metricsGrid.add(createMetricCard("Total Score Earned", scoreMetricLabel, AppConstants.COLOR_PRIMARY));
        metricsGrid.add(createMetricCard("Scored Percentage", percentageMetricLabel, AppConstants.COLOR_SECONDARY));
        metricsGrid.add(createMetricCard("Accuracy Breakdown", accuracyMetricLabel, new Color(16, 185, 129)));

        summaryContainer.add(metricsGrid, BorderLayout.CENTER);
        contentPanel.add(summaryContainer, BorderLayout.NORTH);

        // 2. Question-by-Question Review Table
        JPanel reviewPanel = new JPanel(new BorderLayout(0, 10));
        reviewPanel.setBackground(Color.WHITE);
        reviewPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel reviewHeader = new JLabel("Question-by-Question Performance Review");
        reviewHeader.setFont(AppConstants.FONT_HEADER);
        reviewHeader.setForeground(AppConstants.COLOR_TEXT_PRIMARY);

        String[] cols = {"Q#", "Question Statement", "Your Answer", "Correct Answer", "Outcome", "Marks"};
        reviewTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        reviewTable = new JTable(reviewTableModel);
        reviewTable.setRowHeight(30);
        reviewTable.setFont(AppConstants.FONT_BODY);
        reviewTable.getTableHeader().setFont(AppConstants.FONT_BODY_BOLD);

        // Custom column widths
        reviewTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        reviewTable.getColumnModel().getColumn(1).setPreferredWidth(420);
        reviewTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        reviewTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        reviewTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        reviewTable.getColumnModel().getColumn(5).setPreferredWidth(70);

        // Center align Q#, outcome, and marks
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        reviewTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        reviewTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        reviewTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        reviewTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        reviewTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(reviewTable);
        scroll.setBorder(new LineBorder(AppConstants.COLOR_BORDER, 1));

        reviewPanel.add(reviewHeader, BorderLayout.NORTH);
        reviewPanel.add(scroll, BorderLayout.CENTER);

        contentPanel.add(reviewPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppConstants.FONT_SMALL);
        titleLbl.setForeground(AppConstants.COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Loads and displays the final evaluation result.
     */
    public void displayResult(ExamResult result, Exam exam, List<Question> questions, Map<Integer, String> selectedAnswers) {
        if (result == null || exam == null) {
            return;
        }

        examTitleLabel.setText("Result: " + exam.getTitle());

        boolean passed = result.isPassed();
        if (passed) {
            statusBannerLabel.setText("🎉 CONGRATULATIONS! YOU HAVE PASSED THIS EXAMINATION");
            statusBannerLabel.setBackground(new Color(236, 253, 245));
            statusBannerLabel.setForeground(new Color(6, 95, 70));
        } else {
            statusBannerLabel.setText("❌ ASSESSMENT RESULT: PASSING CRITERIA NOT MET");
            statusBannerLabel.setBackground(new Color(254, 242, 242));
            statusBannerLabel.setForeground(new Color(153, 27, 27));
        }

        scoreMetricLabel.setText(String.format("%.1f / %.1f pts", result.getScore(), result.getTotalMarks()));
        percentageMetricLabel.setText(String.format("%.1f%% (Min. %.1f%%)", result.getPercentage(), exam.getPassingPercentage()));
        accuracyMetricLabel.setText(String.format("%d Correct | %d Incorrect", result.getCorrectCount(), result.getIncorrectCount()));

        // Populate question-by-question review table
        reviewTableModel.setRowCount(0);
        if (questions != null) {
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                String studentChoice = selectedAnswers != null ? selectedAnswers.get(q.getQuestionId()) : null;
                boolean answered = (studentChoice != null && !studentChoice.trim().isEmpty());
                boolean isCorrect = answered && studentChoice.equalsIgnoreCase(q.getCorrectOption());

                String outcome = !answered ? "Skipped" : (isCorrect ? "✔ Correct" : "✘ Incorrect");
                String marksAwarded = isCorrect ? String.format("+%.1f", q.getMarks()) : "0.0";

                reviewTableModel.addRow(new Object[]{
                        i + 1,
                        q.getQuestionText(),
                        answered ? "Option " + studentChoice : "None (Skipped)",
                        "Option " + q.getCorrectOption(),
                        outcome,
                        marksAwarded
                });
            }
        }
    }
}

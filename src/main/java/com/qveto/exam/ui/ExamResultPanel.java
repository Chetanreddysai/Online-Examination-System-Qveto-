package com.qveto.exam.ui;

import com.qveto.exam.model.Exam;
import com.qveto.exam.model.ExamResult;
import com.qveto.exam.model.Question;
import com.qveto.exam.model.User;
import com.qveto.exam.util.AppConstants;
import com.qveto.exam.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Result display panel presenting immediate candidate grading metrics,
 * pass/fail outcome badges, KPI performance cards, progress bar gauges,
 * and detailed question-by-question performance review.
 */
public class ExamResultPanel extends JPanel {

    private final MainFrame mainFrame;

    private JLabel examTitleLabel;
    private JLabel statusBannerLabel;
    private JLabel candidateMetaLabel;

    private JLabel scoreMetricLabel;
    private JLabel percentageMetricLabel;
    private JLabel accuracyMetricLabel;
    private JLabel outcomeBadgeLabel;
    private JProgressBar performanceProgressBar;

    private JTable reviewTable;
    private DefaultTableModel reviewTableModel;

    private ExamResult lastResult;
    private Exam lastExam;
    private List<Question> lastQuestions;
    private Map<Integer, String> lastSelectedAnswers;

    public ExamResultPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.COLOR_BACKGROUND);

        // Top Navigation & Action Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(AppConstants.COLOR_PRIMARY);
        topBar.setBorder(new EmptyBorder(14, 24, 14, 24));

        examTitleLabel = new JLabel("Examination Result Scorecard");
        examTitleLabel.setFont(AppConstants.FONT_HEADER);
        examTitleLabel.setForeground(Color.WHITE);

        JPanel topActionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topActionBtns.setOpaque(false);

        JButton summaryBtn = new JButton("📄 View Scorecard Summary");
        summaryBtn.setFont(AppConstants.FONT_BODY);
        summaryBtn.setForeground(AppConstants.COLOR_PRIMARY);
        summaryBtn.setBackground(new Color(241, 245, 249));
        summaryBtn.setFocusPainted(false);
        summaryBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        summaryBtn.addActionListener(e -> showScorecardSummaryDialog());

        JButton returnBtn = new JButton("← Return to Dashboard");
        returnBtn.setFont(AppConstants.FONT_BODY_BOLD);
        returnBtn.setForeground(AppConstants.COLOR_PRIMARY);
        returnBtn.setBackground(Color.WHITE);
        returnBtn.setFocusPainted(false);
        returnBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        returnBtn.addActionListener(e -> mainFrame.showCard(AppConstants.CARD_STUDENT_DASHBOARD));

        topActionBtns.add(summaryBtn);
        topActionBtns.add(returnBtn);

        topBar.add(examTitleLabel, BorderLayout.WEST);
        topBar.add(topActionBtns, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Content Container
        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setBackground(AppConstants.COLOR_BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Result Banner + KPI Metrics Container
        JPanel summaryContainer = new JPanel(new BorderLayout(0, 12));
        summaryContainer.setOpaque(false);

        // Outcome Banner & Candidate Info
        JPanel bannerStack = new JPanel(new BorderLayout(0, 6));
        bannerStack.setOpaque(false);

        statusBannerLabel = new JLabel("ASSESSMENT COMPLETED", JLabel.CENTER);
        statusBannerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusBannerLabel.setOpaque(true);
        statusBannerLabel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        candidateMetaLabel = new JLabel("Candidate: Student | Attempt ID: #-- | Graded: Just now", JLabel.CENTER);
        candidateMetaLabel.setFont(AppConstants.FONT_SMALL);
        candidateMetaLabel.setForeground(AppConstants.COLOR_TEXT_MUTED);

        bannerStack.add(statusBannerLabel, BorderLayout.NORTH);
        bannerStack.add(candidateMetaLabel, BorderLayout.SOUTH);
        summaryContainer.add(bannerStack, BorderLayout.NORTH);

        // 4-Card KPI Metrics Grid
        JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        metricsGrid.setOpaque(false);

        scoreMetricLabel = new JLabel("0.0 / 0.0 pts", JLabel.CENTER);
        percentageMetricLabel = new JLabel("0.0%", JLabel.CENTER);
        accuracyMetricLabel = new JLabel("0 Correct | 0 Incorrect", JLabel.CENTER);
        outcomeBadgeLabel = new JLabel("PENDING", JLabel.CENTER);

        metricsGrid.add(createMetricCard("Total Score Earned", scoreMetricLabel, AppConstants.COLOR_PRIMARY, "Accumulated marks"));
        metricsGrid.add(createMetricCard("Scored Percentage", percentageMetricLabel, AppConstants.COLOR_SECONDARY, "Overall result"));
        metricsGrid.add(createMetricCard("Accuracy Breakdown", accuracyMetricLabel, new Color(16, 185, 129), "Correct vs Incorrect"));
        metricsGrid.add(createMetricCard("Final Assessment Status", outcomeBadgeLabel, new Color(124, 58, 237), "Evaluation outcome"));

        // Progress Bar Gauge for Passing Threshold
        JPanel progressCard = new JPanel(new BorderLayout(10, 4));
        progressCard.setBackground(Color.WHITE);
        progressCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JLabel progressTitle = new JLabel("Passing Threshold Gauge:");
        progressTitle.setFont(AppConstants.FONT_SMALL);
        progressTitle.setForeground(AppConstants.COLOR_TEXT_MUTED);

        performanceProgressBar = new JProgressBar(0, 100);
        performanceProgressBar.setValue(0);
        performanceProgressBar.setStringPainted(true);
        performanceProgressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        performanceProgressBar.setForeground(AppConstants.COLOR_SUCCESS);
        performanceProgressBar.setBackground(new Color(241, 245, 249));
        performanceProgressBar.setPreferredSize(new Dimension(300, 20));

        progressCard.add(progressTitle, BorderLayout.WEST);
        progressCard.add(performanceProgressBar, BorderLayout.CENTER);

        JPanel metricsWrapper = new JPanel(new BorderLayout(0, 10));
        metricsWrapper.setOpaque(false);
        metricsWrapper.add(metricsGrid, BorderLayout.CENTER);
        metricsWrapper.add(progressCard, BorderLayout.SOUTH);

        summaryContainer.add(metricsWrapper, BorderLayout.CENTER);
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
        reviewTable.setRowHeight(32);
        reviewTable.setFont(AppConstants.FONT_BODY);
        reviewTable.getTableHeader().setFont(AppConstants.FONT_BODY_BOLD);

        // Custom column widths
        reviewTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        reviewTable.getColumnModel().getColumn(1).setPreferredWidth(420);
        reviewTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        reviewTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        reviewTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        reviewTable.getColumnModel().getColumn(5).setPreferredWidth(75);

        // Center align Q#, choices, and marks
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        reviewTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        reviewTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        reviewTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        reviewTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Outcome column custom colored renderer
        reviewTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                String text = value != null ? value.toString() : "";
                if (text.contains("Correct")) {
                    setForeground(new Color(5, 150, 105)); // Green
                } else if (text.contains("Incorrect")) {
                    setForeground(new Color(220, 38, 38)); // Red
                } else {
                    setForeground(new Color(107, 114, 128)); // Muted
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(reviewTable);
        scroll.setBorder(new LineBorder(AppConstants.COLOR_BORDER, 1));

        reviewPanel.add(reviewHeader, BorderLayout.NORTH);
        reviewPanel.add(scroll, BorderLayout.CENTER);

        contentPanel.add(reviewPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppConstants.FONT_SMALL);
        titleLbl.setForeground(AppConstants.COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(accentColor);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subLbl.setForeground(new Color(156, 163, 175));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Loads and displays the final evaluation result and populates scorecard metrics.
     */
    public void displayResult(ExamResult result, Exam exam, List<Question> questions, Map<Integer, String> selectedAnswers) {
        if (result == null || exam == null) {
            return;
        }

        this.lastResult = result;
        this.lastExam = exam;
        this.lastQuestions = questions;
        this.lastSelectedAnswers = selectedAnswers;

        examTitleLabel.setText("Scorecard: " + exam.getTitle());

        boolean passed = result.isPassed();
        if (passed) {
            statusBannerLabel.setText("🏆 CONGRATULATIONS! YOU HAVE PASSED THIS EXAMINATION");
            statusBannerLabel.setBackground(new Color(236, 253, 245));
            statusBannerLabel.setForeground(new Color(6, 95, 70));
            statusBannerLabel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(167, 243, 208), 1, true),
                    new EmptyBorder(14, 18, 14, 18)
            ));
            outcomeBadgeLabel.setText("PASSED");
            outcomeBadgeLabel.setForeground(new Color(16, 185, 129));
            performanceProgressBar.setForeground(new Color(16, 185, 129));
        } else {
            statusBannerLabel.setText("⚠️ ASSESSMENT RESULT: PASSING CRITERIA NOT MET");
            statusBannerLabel.setBackground(new Color(254, 242, 242));
            statusBannerLabel.setForeground(new Color(153, 27, 27));
            statusBannerLabel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(254, 202, 202), 1, true),
                    new EmptyBorder(14, 18, 14, 18)
            ));
            outcomeBadgeLabel.setText("FAILED");
            outcomeBadgeLabel.setForeground(AppConstants.COLOR_DANGER);
            performanceProgressBar.setForeground(AppConstants.COLOR_DANGER);
        }

        scoreMetricLabel.setText(String.format("%.1f / %.1f pts", result.getScore(), result.getTotalMarks()));
        percentageMetricLabel.setText(String.format("%.1f%% (Min. %.1f%%)", result.getPercentage(), exam.getPassingPercentage()));
        accuracyMetricLabel.setText(String.format("%d Correct | %d Incorrect", result.getCorrectCount(), result.getIncorrectCount()));

        int percentInt = (int) Math.round(result.getPercentage());
        performanceProgressBar.setValue(percentInt);
        performanceProgressBar.setString(String.format("%.1f%% (Required: %.1f%%)", result.getPercentage(), exam.getPassingPercentage()));

        // Candidate metadata banner
        User user = SessionManager.getCurrentUser();
        String candidateName = user != null ? user.getFullName() : "Candidate";
        String candidateEmail = user != null ? user.getEmail() : "";
        String timestampStr = result.getGradedAt() != null
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(result.getGradedAt())
                : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        candidateMetaLabel.setText(String.format("Candidate: %s (%s)  |  Attempt #%d  |  Graded: %s  |  Status: %s",
                candidateName, candidateEmail, result.getAttemptId(), timestampStr, passed ? "PASS" : "FAIL"));

        // Populate question-by-question review table
        reviewTableModel.setRowCount(0);
        if (questions != null) {
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                String studentChoice = selectedAnswers != null ? selectedAnswers.get(q.getQuestionId()) : null;
                boolean answered = (studentChoice != null && !studentChoice.trim().isEmpty());
                boolean isCorrect = answered && studentChoice.equalsIgnoreCase(q.getCorrectOption());

                String outcome = !answered ? "○ Skipped" : (isCorrect ? "✔ Correct" : "✘ Incorrect");
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

    public void showScorecardSummaryDialog() {
        if (lastResult == null || lastExam == null) {
            JOptionPane.showMessageDialog(this, "No active scorecard data available.", "Summary", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═════════════════════════════════════════════\n");
        sb.append("           OFFICIAL EXAMINATION SCORECARD             \n");
        sb.append("═════════════════════════════════════════════\n\n");
        sb.append("• Examination Title: ").append(lastExam.getTitle()).append("\n");
        sb.append("• Attempt Reference: #").append(lastResult.getAttemptId()).append("\n");
        sb.append("• Final Outcome:     ").append(lastResult.isPassed() ? "PASSED" : "FAILED").append("\n");
        sb.append(String.format("• Score Earned:      %.1f / %.1f pts\n", lastResult.getScore(), lastResult.getTotalMarks()));
        sb.append(String.format("• Percentage:        %.1f%% (Passing: %.1f%%)\n", lastResult.getPercentage(), lastExam.getPassingPercentage()));
        sb.append(String.format("• Correct Answers:   %d of %d\n", lastResult.getCorrectCount(), lastResult.getTotalQuestions()));
        sb.append(String.format("• Incorrect Answers: %d\n", lastResult.getIncorrectCount()));
        int skipped = Math.max(0, lastResult.getTotalQuestions() - lastResult.getAnsweredCount());
        sb.append(String.format("• Skipped Questions: %d\n\n", skipped));
        sb.append("═════════════════════════════════════════════\n");
        sb.append("Result certified and recorded in system repository.\n");

        JOptionPane.showMessageDialog(this, sb.toString(), "Candidate Scorecard Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    public JLabel getStatusBannerLabel() {
        return statusBannerLabel;
    }

    public JLabel getCandidateMetaLabel() {
        return candidateMetaLabel;
    }

    public JLabel getScoreMetricLabel() {
        return scoreMetricLabel;
    }

    public JLabel getPercentageMetricLabel() {
        return percentageMetricLabel;
    }

    public JLabel getAccuracyMetricLabel() {
        return accuracyMetricLabel;
    }

    public JLabel getOutcomeBadgeLabel() {
        return outcomeBadgeLabel;
    }

    public JProgressBar getPerformanceProgressBar() {
        return performanceProgressBar;
    }

    public JTable getReviewTable() {
        return reviewTable;
    }

    public DefaultTableModel getReviewTableModel() {
        return reviewTableModel;
    }

    public ExamResult getLastResult() {
        return lastResult;
    }

    public Exam getLastExam() {
        return lastExam;
    }

    public List<Question> getLastQuestions() {
        return lastQuestions;
    }

    public Map<Integer, String> getLastSelectedAnswers() {
        return lastSelectedAnswers;
    }
}

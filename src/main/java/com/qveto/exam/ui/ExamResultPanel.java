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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Result display panel presenting immediate candidate grading metrics,
 * pass/fail outcome badges, KPI performance cards, progress bar gauges,
 * and detailed question-by-question performance review with outcome filtering,
 * keyword searching, deep question inspection, and anti-leak answer key protection.
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

    // Day 20 Review Table & Inspector Controls
    private JTable reviewTable;
    private DefaultTableModel reviewTableModel;
    private JComboBox<String> filterComboBox;
    private JTextField searchField;
    private JLabel filterStatusLabel;
    private JLabel securityBadgeLabel;
    private JButton inspectBtn;

    private boolean answerKeysRevealed = true;
    private final List<Integer> displayedQuestionIndices = new ArrayList<>();

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

        // 2. Question-by-Question Review Panel
        JPanel reviewPanel = new JPanel(new BorderLayout(0, 10));
        reviewPanel.setBackground(Color.WHITE);
        reviewPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Header + Filtering Controls Toolbar
        JPanel reviewHeaderPanel = new JPanel(new BorderLayout(12, 10));
        reviewHeaderPanel.setOpaque(false);

        JPanel titleWithSubtitle = new JPanel(new GridLayout(2, 1, 0, 2));
        titleWithSubtitle.setOpaque(false);
        JLabel reviewHeader = new JLabel("Question-by-Question Performance Review");
        reviewHeader.setFont(AppConstants.FONT_HEADER);
        reviewHeader.setForeground(AppConstants.COLOR_TEXT_PRIMARY);

        JLabel reviewSubHeader = new JLabel("Filter outcomes, inspect conceptual rationale, and verify responses (double-click row to inspect).");
        reviewSubHeader.setFont(AppConstants.FONT_SMALL);
        reviewSubHeader.setForeground(AppConstants.COLOR_TEXT_MUTED);

        titleWithSubtitle.add(reviewHeader);
        titleWithSubtitle.add(reviewSubHeader);

        // Security badge indicating answer key disclosure status
        securityBadgeLabel = new JLabel("🔓 Answer Keys: Released", JLabel.CENTER);
        securityBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        securityBadgeLabel.setOpaque(true);
        securityBadgeLabel.setBackground(new Color(236, 253, 245));
        securityBadgeLabel.setForeground(new Color(6, 95, 70));
        securityBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(167, 243, 208), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JPanel headerTop = new JPanel(new BorderLayout());
        headerTop.setOpaque(false);
        headerTop.add(titleWithSubtitle, BorderLayout.WEST);
        headerTop.add(securityBadgeLabel, BorderLayout.EAST);

        // Filter and Search Toolbar
        JPanel controlsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        controlsBar.setOpaque(false);

        JLabel filterLbl = new JLabel("Filter:");
        filterLbl.setFont(AppConstants.FONT_BODY_BOLD);
        filterLbl.setForeground(AppConstants.COLOR_TEXT_PRIMARY);

        filterComboBox = new JComboBox<>(new String[]{
                "All Questions",
                "✔ Correct Only",
                "✘ Incorrect Only",
                "○ Skipped Only"
        });
        filterComboBox.setFont(AppConstants.FONT_BODY);
        filterComboBox.setBackground(Color.WHITE);
        filterComboBox.setFocusable(false);
        filterComboBox.addActionListener(e -> applyFilter());

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(AppConstants.FONT_BODY_BOLD);
        searchLbl.setForeground(AppConstants.COLOR_TEXT_PRIMARY);

        searchField = new JTextField(15);
        searchField.setFont(AppConstants.FONT_BODY);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        inspectBtn = new JButton("🔍 Inspect Question");
        inspectBtn.setFont(AppConstants.FONT_BODY_BOLD);
        inspectBtn.setForeground(AppConstants.COLOR_PRIMARY);
        inspectBtn.setBackground(new Color(239, 246, 255));
        inspectBtn.setFocusPainted(false);
        inspectBtn.setEnabled(false);
        inspectBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        inspectBtn.addActionListener(e -> inspectSelectedQuestion());

        controlsBar.add(filterLbl);
        controlsBar.add(filterComboBox);
        controlsBar.add(searchLbl);
        controlsBar.add(searchField);
        controlsBar.add(inspectBtn);

        reviewHeaderPanel.add(headerTop, BorderLayout.NORTH);
        reviewHeaderPanel.add(controlsBar, BorderLayout.SOUTH);

        // Table Model and JTable
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
        reviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom column widths
        reviewTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        reviewTable.getColumnModel().getColumn(1).setPreferredWidth(420);
        reviewTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        reviewTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        reviewTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        reviewTable.getColumnModel().getColumn(5).setPreferredWidth(75);

        // Center align Q#, choices, and marks
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        reviewTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        reviewTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        reviewTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Correct Answer column custom renderer (masks key if protection enabled)
        reviewTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (!answerKeysRevealed) {
                    setFont(new Font("Segoe UI", Font.ITALIC, 11));
                    if (!isSelected) {
                        setForeground(new Color(180, 83, 9)); // Amber / Warning
                    }
                } else {
                    setFont(AppConstants.FONT_BODY);
                    if (!isSelected) {
                        setForeground(AppConstants.COLOR_TEXT_PRIMARY);
                    }
                }
                return c;
            }
        });

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
                } else if (text.contains("Protected") || text.contains("Pending")) {
                    setForeground(new Color(180, 83, 9)); // Amber
                } else {
                    setForeground(new Color(107, 114, 128)); // Muted
                }
                return c;
            }
        });

        // Row Selection listener for Inspect button
        reviewTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                inspectBtn.setEnabled(reviewTable.getSelectedRow() != -1);
            }
        });

        // Double-click row listener to inspect question
        reviewTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && reviewTable.getSelectedRow() != -1) {
                    inspectSelectedQuestion();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(reviewTable);
        scroll.setBorder(new LineBorder(AppConstants.COLOR_BORDER, 1));

        // Bottom status and instructions bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(6, 4, 0, 4));

        filterStatusLabel = new JLabel("Showing 0 of 0 questions");
        filterStatusLabel.setFont(AppConstants.FONT_SMALL);
        filterStatusLabel.setForeground(AppConstants.COLOR_TEXT_MUTED);

        JLabel hintLabel = new JLabel("💡 Tip: Select a question row and click 'Inspect Question' for full rationale.");
        hintLabel.setFont(AppConstants.FONT_SMALL);
        hintLabel.setForeground(AppConstants.COLOR_TEXT_MUTED);

        bottomBar.add(filterStatusLabel, BorderLayout.WEST);
        bottomBar.add(hintLabel, BorderLayout.EAST);

        reviewPanel.add(reviewHeaderPanel, BorderLayout.NORTH);
        reviewPanel.add(scroll, BorderLayout.CENTER);
        reviewPanel.add(bottomBar, BorderLayout.SOUTH);

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
     * Defaults to revealing answer keys since final grading is completed.
     */
    public void displayResult(ExamResult result, Exam exam, List<Question> questions, Map<Integer, String> selectedAnswers) {
        displayResult(result, exam, questions, selectedAnswers, true);
    }

    /**
     * Overloaded displayResult allowing explicit control over whether answer keys are revealed.
     * Prevents premature answer key leakage when reviewing ungraded or unreleased attempts.
     */
    public void displayResult(ExamResult result, Exam exam, List<Question> questions,
                              Map<Integer, String> selectedAnswers, boolean allowAnswerKeyReveal) {
        if (result == null || exam == null) {
            return;
        }

        this.lastResult = result;
        this.lastExam = exam;
        this.lastQuestions = questions;
        this.lastSelectedAnswers = selectedAnswers;
        this.answerKeysRevealed = allowAnswerKeyReveal;

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

        updateSecurityBadge();
        updateFilterComboLabels();
        applyFilter();
    }

    /**
     * Updates the answer key security badge based on current disclosure policy.
     */
    private void updateSecurityBadge() {
        if (securityBadgeLabel == null) return;
        if (answerKeysRevealed) {
            securityBadgeLabel.setText("🔓 Answer Keys: Released");
            securityBadgeLabel.setBackground(new Color(236, 253, 245));
            securityBadgeLabel.setForeground(new Color(6, 95, 70));
            securityBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(167, 243, 208), 1, true),
                    new EmptyBorder(4, 10, 4, 10)
            ));
        } else {
            securityBadgeLabel.setText("🔒 Answer Keys: Protected (Hidden)");
            securityBadgeLabel.setBackground(new Color(254, 243, 199));
            securityBadgeLabel.setForeground(new Color(146, 64, 14));
            securityBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(253, 230, 138), 1, true),
                    new EmptyBorder(4, 10, 4, 10)
            ));
        }
    }

    /**
     * Populates count indicators inside the filter combo options.
     */
    private void updateFilterComboLabels() {
        if (filterComboBox == null || lastQuestions == null) return;
        int total = lastQuestions.size();
        int correct = 0;
        int incorrect = 0;
        int skipped = 0;

        for (Question q : lastQuestions) {
            String studentChoice = lastSelectedAnswers != null ? lastSelectedAnswers.get(q.getQuestionId()) : null;
            boolean answered = (studentChoice != null && !studentChoice.trim().isEmpty());
            boolean isCorrect = answered && studentChoice.equalsIgnoreCase(q.getCorrectOption());
            if (!answered) {
                skipped++;
            } else if (isCorrect) {
                correct++;
            } else {
                incorrect++;
            }
        }

        int prevIdx = filterComboBox.getSelectedIndex();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement(String.format("All Questions (%d)", total));
        model.addElement(String.format("✔ Correct Only (%d)", correct));
        model.addElement(String.format("✘ Incorrect Only (%d)", incorrect));
        model.addElement(String.format("○ Skipped Only (%d)", skipped));
        filterComboBox.setModel(model);
        if (prevIdx >= 0 && prevIdx < model.getSize()) {
            filterComboBox.setSelectedIndex(prevIdx);
        }
    }

    /**
     * Filters the question review table dynamically by outcome and keyword search.
     */
    public void applyFilter() {
        if (reviewTableModel == null) {
            return;
        }

        reviewTableModel.setRowCount(0);
        displayedQuestionIndices.clear();

        if (lastQuestions == null || lastQuestions.isEmpty()) {
            if (filterStatusLabel != null) {
                filterStatusLabel.setText("Showing 0 of 0 questions");
            }
            if (inspectBtn != null) {
                inspectBtn.setEnabled(false);
            }
            return;
        }

        String filterSelection = filterComboBox != null && filterComboBox.getSelectedItem() != null
                ? filterComboBox.getSelectedItem().toString()
                : "All Questions";

        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";

        for (int i = 0; i < lastQuestions.size(); i++) {
            Question q = lastQuestions.get(i);
            String studentChoice = lastSelectedAnswers != null ? lastSelectedAnswers.get(q.getQuestionId()) : null;
            boolean answered = (studentChoice != null && !studentChoice.trim().isEmpty());
            boolean isCorrect = answered && studentChoice.equalsIgnoreCase(q.getCorrectOption());

            // 1. Outcome filtering
            boolean matchesOutcome = true;
            if (filterSelection.contains("Correct") && !filterSelection.contains("Incorrect") && !filterSelection.contains("All")) {
                matchesOutcome = answered && isCorrect;
            } else if (filterSelection.contains("Incorrect")) {
                matchesOutcome = answered && !isCorrect;
            } else if (filterSelection.contains("Skipped")) {
                matchesOutcome = !answered;
            }

            if (!matchesOutcome) {
                continue;
            }

            // 2. Keyword query filtering
            if (!query.isEmpty()) {
                boolean textMatch = q.getQuestionText() != null && q.getQuestionText().toLowerCase().contains(query);
                boolean subjectMatch = q.getSubject() != null && q.getSubject().toLowerCase().contains(query);
                boolean optAMatch = q.getOptionA() != null && q.getOptionA().toLowerCase().contains(query);
                boolean optBMatch = q.getOptionB() != null && q.getOptionB().toLowerCase().contains(query);
                boolean optCMatch = q.getOptionC() != null && q.getOptionC().toLowerCase().contains(query);
                boolean optDMatch = q.getOptionD() != null && q.getOptionD().toLowerCase().contains(query);
                if (!textMatch && !subjectMatch && !optAMatch && !optBMatch && !optCMatch && !optDMatch) {
                    continue;
                }
            }

            String outcomeText;
            String marksAwarded;
            String correctColText;

            if (answerKeysRevealed) {
                outcomeText = !answered ? "○ Skipped" : (isCorrect ? "✔ Correct" : "✘ Incorrect");
                marksAwarded = isCorrect ? String.format("+%.1f", q.getMarks()) : "0.0";
                correctColText = "Option " + q.getCorrectOption();
            } else {
                outcomeText = !answered ? "○ Skipped" : "🔒 Recorded (Grading Protected)";
                marksAwarded = "🔒 Pending";
                correctColText = "🔒 Protected (Hidden)";
            }

            displayedQuestionIndices.add(i);
            reviewTableModel.addRow(new Object[]{
                    i + 1,
                    q.getQuestionText(),
                    answered ? "Option " + studentChoice : "None (Skipped)",
                    correctColText,
                    outcomeText,
                    marksAwarded
            });
        }

        if (filterStatusLabel != null) {
            filterStatusLabel.setText(String.format("Showing %d of %d questions (Filter: %s)",
                    reviewTableModel.getRowCount(), lastQuestions.size(), filterSelection));
        }

        if (inspectBtn != null) {
            inspectBtn.setEnabled(reviewTable.getSelectedRow() != -1);
        }
    }

    /**
     * Programmatic or button trigger to inspect the selected row question.
     */
    public void inspectSelectedQuestion() {
        int selectedRow = reviewTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a question row to inspect.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        showQuestionInspectionDialog(selectedRow);
    }

    /**
     * Generates formatted textual breakdown of question review and rationale.
     * Used for automated assertions and programmatic review.
     */
    public String getQuestionInspectionDetails(int displayRowIndex) {
        if (displayRowIndex < 0 || displayRowIndex >= displayedQuestionIndices.size()) {
            return "No question available at specified row.";
        }
        int qIdx = displayedQuestionIndices.get(displayRowIndex);
        if (lastQuestions == null || qIdx < 0 || qIdx >= lastQuestions.size()) {
            return "Question data unavailable.";
        }
        Question q = lastQuestions.get(qIdx);
        String studentChoice = lastSelectedAnswers != null ? lastSelectedAnswers.get(q.getQuestionId()) : null;
        boolean answered = (studentChoice != null && !studentChoice.trim().isEmpty());
        boolean isCorrect = answered && studentChoice.equalsIgnoreCase(q.getCorrectOption());

        StringBuilder sb = new StringBuilder();
        sb.append("Question #").append(qIdx + 1).append(" (Subject: ").append(q.getSubject() != null ? q.getSubject() : "General").append(")\n");
        sb.append("Statement: ").append(q.getQuestionText()).append("\n\n");
        sb.append("Option A: ").append(q.getOptionA()).append("\n");
        sb.append("Option B: ").append(q.getOptionB()).append("\n");
        sb.append("Option C: ").append(q.getOptionC()).append("\n");
        sb.append("Option D: ").append(q.getOptionD()).append("\n\n");

        sb.append("Candidate Selection: ").append(answered ? "Option " + studentChoice : "Skipped (Unanswered)").append("\n");
        if (answerKeysRevealed) {
            sb.append("Correct Answer: Option ").append(q.getCorrectOption()).append("\n");
            sb.append("Outcome: ").append(!answered ? "Skipped (0.0 marks)" : (isCorrect ? "Correct (+" + q.getMarks() + " marks)" : "Incorrect (0.0 marks)")).append("\n");
            sb.append("Rationale: ").append(getQuestionRationaleText(q, studentChoice, isCorrect)).append("\n");
        } else {
            sb.append("Correct Answer: 🔒 [Protected - Hidden until grading release]\n");
            sb.append("Outcome: 🔒 [Recorded - Evaluation Key Sealed]\n");
            sb.append("Rationale: Official answer key and explanation are locked to prevent premature answer disclosure.\n");
        }
        return sb.toString();
    }

    /**
     * Computes contextual pedagogical explanation for candidate review.
     */
    public String getQuestionRationaleText(Question q, String studentChoice, boolean isCorrect) {
        if (!answerKeysRevealed) {
            return "Answer key is protected by examination proctoring rules.";
        }
        String correct = q.getCorrectOption();
        if (isCorrect) {
            return String.format("Accurate! Option %s is the authoritative answer for '%s'. Full credit awarded (+%.1f pts).",
                    correct, q.getSubject() != null ? q.getSubject() : "this topic", q.getMarks());
        } else if (studentChoice == null || studentChoice.trim().isEmpty()) {
            return String.format("Question was skipped. The correct answer is Option %s. Review this concept in your study materials.",
                    correct);
        } else {
            return String.format("Incorrect selection. You chose Option %s, but the correct answer is Option %s. Review %s principles.",
                    studentChoice, correct, q.getSubject() != null ? q.getSubject() : "relevant");
        }
    }

    /**
     * Displays modal dialog for inspecting question details and rationale.
     */
    public void showQuestionInspectionDialog(int displayRowIndex) {
        if (displayRowIndex < 0 || displayRowIndex >= displayedQuestionIndices.size()) {
            return;
        }

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow instanceof Frame ? (Frame) parentWindow : null, "Question Performance Review & Rationale", true);
        dialog.setSize(680, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPane = new JPanel(new BorderLayout(0, 14));
        mainPane.setBackground(AppConstants.COLOR_BACKGROUND);
        mainPane.setBorder(new EmptyBorder(18, 20, 18, 20));

        final int[] currentDisplayRow = new int[]{displayRowIndex};

        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);

        JLabel dialogTitle = new JLabel("Question Performance Review");
        dialogTitle.setFont(AppConstants.FONT_HEADER);
        dialogTitle.setForeground(AppConstants.COLOR_PRIMARY);

        JLabel statusPill = new JLabel("", JLabel.CENTER);
        statusPill.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusPill.setOpaque(true);
        statusPill.setBorder(new EmptyBorder(4, 12, 4, 12));

        headerPanel.add(dialogTitle, BorderLayout.WEST);
        headerPanel.add(statusPill, BorderLayout.EAST);

        JTextArea statementArea = new JTextArea();
        statementArea.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statementArea.setWrapStyleWord(true);
        statementArea.setLineWrap(true);
        statementArea.setEditable(false);
        statementArea.setOpaque(false);
        statementArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel optionsContainer = new JPanel(new GridLayout(4, 1, 0, 8));
        optionsContainer.setOpaque(false);

        JLabel[] optLabels = new JLabel[4];
        for (int i = 0; i < 4; i++) {
            optLabels[i] = new JLabel();
            optLabels[i].setFont(AppConstants.FONT_BODY);
            optLabels[i].setOpaque(true);
            optLabels[i].setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                    new EmptyBorder(8, 12, 8, 12)
            ));
            optionsContainer.add(optLabels[i]);
        }

        JPanel rationaleCard = new JPanel(new BorderLayout(0, 6));
        rationaleCard.setBackground(Color.WHITE);
        rationaleCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel rationaleHeading = new JLabel("💡 Conceptual Rationale & Analysis");
        rationaleHeading.setFont(AppConstants.FONT_BODY_BOLD);
        rationaleHeading.setForeground(AppConstants.COLOR_PRIMARY);

        JTextArea rationaleText = new JTextArea();
        rationaleText.setFont(AppConstants.FONT_BODY);
        rationaleText.setLineWrap(true);
        rationaleText.setWrapStyleWord(true);
        rationaleText.setEditable(false);
        rationaleText.setOpaque(false);

        rationaleCard.add(rationaleHeading, BorderLayout.NORTH);
        rationaleCard.add(rationaleText, BorderLayout.CENTER);

        JPanel detailsStack = new JPanel(new BorderLayout(0, 12));
        detailsStack.setOpaque(false);
        detailsStack.add(statementArea, BorderLayout.NORTH);
        detailsStack.add(optionsContainer, BorderLayout.CENTER);
        detailsStack.add(rationaleCard, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(detailsStack);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Bottom Navigation Bar (Previous, Next, Close)
        JPanel bottomNav = new JPanel(new BorderLayout());
        bottomNav.setOpaque(false);

        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftNav.setOpaque(false);
        JButton prevBtn = new JButton("← Previous");
        prevBtn.setFont(AppConstants.FONT_BODY);
        prevBtn.setFocusPainted(false);

        JButton nextBtn = new JButton("Next →");
        nextBtn.setFont(AppConstants.FONT_BODY);
        nextBtn.setFocusPainted(false);

        leftNav.add(prevBtn);
        leftNav.add(nextBtn);

        JButton closeBtn = new JButton("Close Review");
        closeBtn.setFont(AppConstants.FONT_BODY_BOLD);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(AppConstants.COLOR_PRIMARY);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dialog.dispose());

        bottomNav.add(leftNav, BorderLayout.WEST);
        bottomNav.add(closeBtn, BorderLayout.EAST);

        Runnable refreshView = () -> {
            int row = currentDisplayRow[0];
            int qIdx = displayedQuestionIndices.get(row);
            Question q = lastQuestions.get(qIdx);
            String studentChoice = lastSelectedAnswers != null ? lastSelectedAnswers.get(q.getQuestionId()) : null;
            boolean answered = (studentChoice != null && !studentChoice.trim().isEmpty());
            boolean isCorrect = answered && studentChoice.equalsIgnoreCase(q.getCorrectOption());

            dialogTitle.setText(String.format("Question #%d of %d (Subject: %s)",
                    qIdx + 1, lastQuestions.size(), q.getSubject() != null ? q.getSubject() : "General"));
            statementArea.setText(q.getQuestionText());

            if (!answerKeysRevealed) {
                statusPill.setText("🔒 KEY SEALED");
                statusPill.setBackground(new Color(254, 243, 199));
                statusPill.setForeground(new Color(146, 64, 14));
                rationaleHeading.setText("🔒 Answer Key Security Guard");
                rationaleText.setText("The official answer key and detailed explanations remain locked in accordance with the examination integrity policy. They will be unsealed after all candidate submissions are finalized.");
            } else if (!answered) {
                statusPill.setText("○ SKIPPED (0.0 pts)");
                statusPill.setBackground(new Color(243, 244, 246));
                statusPill.setForeground(new Color(107, 114, 128));
                rationaleHeading.setText("💡 Review & Analysis");
                rationaleText.setText(getQuestionRationaleText(q, studentChoice, isCorrect));
            } else if (isCorrect) {
                statusPill.setText(String.format("✔ CORRECT (+%.1f pts)", q.getMarks()));
                statusPill.setBackground(new Color(236, 253, 245));
                statusPill.setForeground(new Color(6, 95, 70));
                rationaleHeading.setText("💡 Correct Solution Rationale");
                rationaleText.setText(getQuestionRationaleText(q, studentChoice, isCorrect));
            } else {
                statusPill.setText("✘ INCORRECT (0.0 pts)");
                statusPill.setBackground(new Color(254, 242, 242));
                statusPill.setForeground(new Color(153, 27, 27));
                rationaleHeading.setText("💡 Error Diagnostics & Rationale");
                rationaleText.setText(getQuestionRationaleText(q, studentChoice, isCorrect));
            }

            // Populate 4 options
            String[] optLetters = {"A", "B", "C", "D"};
            String[] optTexts = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};

            for (int i = 0; i < 4; i++) {
                String letter = optLetters[i];
                String text = optTexts[i];
                boolean isUser = answered && letter.equalsIgnoreCase(studentChoice);
                boolean isKey = answerKeysRevealed && letter.equalsIgnoreCase(q.getCorrectOption());

                StringBuilder badge = new StringBuilder();
                if (isUser) badge.append(" [👉 Your Choice]");
                if (isKey) badge.append(" [✔ Correct Answer]");

                optLabels[i].setText(String.format("<html><b>Option %s:</b> %s <font color='%s'><b>%s</b></font></html>",
                        letter, text != null ? text : "",
                        isKey ? "#059669" : (isUser ? (isCorrect ? "#059669" : "#DC2626") : "#4B5563"),
                        badge.toString()));

                if (isKey) {
                    optLabels[i].setBackground(new Color(236, 253, 245));
                    optLabels[i].setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(16, 185, 129), 2, true),
                            new EmptyBorder(8, 12, 8, 12)
                    ));
                } else if (isUser && !isCorrect) {
                    optLabels[i].setBackground(new Color(254, 242, 242));
                    optLabels[i].setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(239, 68, 68), 2, true),
                            new EmptyBorder(8, 12, 8, 12)
                    ));
                } else {
                    optLabels[i].setBackground(Color.WHITE);
                    optLabels[i].setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                            new EmptyBorder(8, 12, 8, 12)
                    ));
                }
            }

            prevBtn.setEnabled(row > 0);
            nextBtn.setEnabled(row < displayedQuestionIndices.size() - 1);
        };

        prevBtn.addActionListener(e -> {
            if (currentDisplayRow[0] > 0) {
                currentDisplayRow[0]--;
                refreshView.run();
            }
        });

        nextBtn.addActionListener(e -> {
            if (currentDisplayRow[0] < displayedQuestionIndices.size() - 1) {
                currentDisplayRow[0]++;
                refreshView.run();
            }
        });

        refreshView.run();

        mainPane.add(headerPanel, BorderLayout.NORTH);
        mainPane.add(scrollPane, BorderLayout.CENTER);
        mainPane.add(bottomNav, BorderLayout.SOUTH);

        dialog.add(mainPane);
        dialog.setVisible(true);
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

    public void setAnswerKeysRevealed(boolean revealed) {
        this.answerKeysRevealed = revealed;
        updateSecurityBadge();
        applyFilter();
    }

    public boolean isAnswerKeysRevealed() {
        return answerKeysRevealed;
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

    public JComboBox<String> getFilterComboBox() {
        return filterComboBox;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JLabel getFilterStatusLabel() {
        return filterStatusLabel;
    }

    public JLabel getSecurityBadgeLabel() {
        return securityBadgeLabel;
    }

    public JButton getInspectBtn() {
        return inspectBtn;
    }

    public List<Integer> getDisplayedQuestionIndices() {
        return displayedQuestionIndices;
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

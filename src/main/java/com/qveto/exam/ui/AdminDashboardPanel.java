package com.qveto.exam.ui;

import com.qveto.exam.model.Exam;
import com.qveto.exam.model.ExamResult;
import com.qveto.exam.model.Question;
import com.qveto.exam.model.User;
import com.qveto.exam.service.AuthService;
import com.qveto.exam.service.ExamService;
import com.qveto.exam.util.AppConstants;
import com.qveto.exam.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Administrator Dashboard Panel (Week 1 Application Shell).
 * Provides overview metrics, exam management listings, question bank preview,
 * and secure session controls.
 */
public class AdminDashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService;
    private final ExamService examService;

    private JLabel adminNameLabel;
    private JLabel totalExamsLabel;
    private JLabel totalQuestionsLabel;
    private JLabel completionRateLabel;
    private JTable examsTable;
    private DefaultTableModel examsTableModel;
    private JTable questionsTable;
    private DefaultTableModel questionsTableModel;

    // Results & Reports Management
    private JTable resultsTable;
    private DefaultTableModel resultsTableModel;
    private final List<ExamResult> allCandidateResults = new ArrayList<>();
    private JComboBox<String> examFilterCombo;
    private JTextField candidateSearchField;

    public AdminDashboardPanel(MainFrame mainFrame, AuthService authService, ExamService examService) {
        this.mainFrame = mainFrame;
        this.authService = authService;
        this.examService = examService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.COLOR_BACKGROUND);

        // Top Navigation Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(AppConstants.COLOR_PRIMARY);
        topBar.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel brandLabel = new JLabel("Qveto Admin Portal — Online Examination Management");
        brandLabel.setFont(AppConstants.FONT_HEADER);
        brandLabel.setForeground(Color.WHITE);

        JPanel rightTopPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightTopPanel.setOpaque(false);

        adminNameLabel = new JLabel("Admin: System Administrator");
        adminNameLabel.setFont(AppConstants.FONT_BODY_BOLD);
        adminNameLabel.setForeground(Color.WHITE);

        JButton profileBtn = new JButton("👤 My Profile");
        profileBtn.setFont(AppConstants.FONT_SMALL);
        profileBtn.setForeground(AppConstants.COLOR_PRIMARY);
        profileBtn.setBackground(Color.WHITE);
        profileBtn.setFocusPainted(false);
        profileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileBtn.addActionListener(e -> new UserProfileDialog(mainFrame, authService, this::refreshData).setVisible(true));

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setFont(AppConstants.FONT_SMALL);
        logoutBtn.setForeground(AppConstants.COLOR_PRIMARY);
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> mainFrame.handleLogout());

        rightTopPanel.add(adminNameLabel);
        rightTopPanel.add(profileBtn);
        rightTopPanel.add(logoutBtn);

        topBar.add(brandLabel, BorderLayout.WEST);
        topBar.add(rightTopPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Main Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setBackground(AppConstants.COLOR_BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Metric Cards Header
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        metricsPanel.setOpaque(false);

        totalExamsLabel = new JLabel("2", JLabel.CENTER);
        totalQuestionsLabel = new JLabel("10", JLabel.CENTER);
        completionRateLabel = new JLabel("100%", JLabel.CENTER);

        metricsPanel.add(createMetricCard("Configured Exams", totalExamsLabel, new Color(30, 64, 175)));
        metricsPanel.add(createMetricCard("Question Bank Items", totalQuestionsLabel, new Color(13, 148, 136)));
        metricsPanel.add(createMetricCard("System Completion Rate", completionRateLabel, new Color(16, 185, 129)));

        contentPanel.add(metricsPanel, BorderLayout.NORTH);

        // Tabbed Workspaces
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(AppConstants.FONT_SUBHEADER);

        // Tab 1: Exams Table
        String[] examCols = {"ID", "Exam Title", "Duration (mins)", "Questions", "Total Marks", "Pass %", "Status"};
        examsTableModel = new DefaultTableModel(examCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        examsTable = new JTable(examsTableModel);
        examsTable.setRowHeight(28);
        examsTable.setFont(AppConstants.FONT_BODY);
        examsTable.getTableHeader().setFont(AppConstants.FONT_BODY_BOLD);
        JScrollPane examScroll = new JScrollPane(examsTable);
        examScroll.setBorder(new LineBorder(AppConstants.COLOR_BORDER, 1));

        JPanel examTab = new JPanel(new BorderLayout(0, 10));
        examTab.setBackground(Color.WHITE);
        examTab.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel examHeaderPanel = new JPanel(new BorderLayout());
        examHeaderPanel.setOpaque(false);

        JLabel examTabHeader = new JLabel("Manage Published & Draft Examination Papers");
        examTabHeader.setFont(AppConstants.FONT_HEADER);
        examTabHeader.setForeground(AppConstants.COLOR_PRIMARY);

        JPanel examActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        examActions.setOpaque(false);

        JButton createExamBtn = new JButton("+ Create New Exam");
        createExamBtn.setFont(AppConstants.FONT_BODY_BOLD);
        createExamBtn.setForeground(Color.WHITE);
        createExamBtn.setBackground(AppConstants.COLOR_PRIMARY);
        createExamBtn.setFocusPainted(false);
        createExamBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createExamBtn.addActionListener(e -> new CreateExamDialog(mainFrame, examService, this::refreshData).setVisible(true));

        JButton toggleExamBtn = new JButton("Toggle Status");
        toggleExamBtn.setFont(AppConstants.FONT_BODY);
        toggleExamBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleExamBtn.addActionListener(e -> {
            int row = examsTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select an exam from the table to toggle its status.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int examId = (Integer) examsTableModel.getValueAt(row, 0);
            examService.toggleExamStatus(examId);
            refreshData();
        });

        JButton refreshExamsBtn = new JButton("🔄 Refresh");
        refreshExamsBtn.setFont(AppConstants.FONT_BODY);
        refreshExamsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshExamsBtn.addActionListener(e -> refreshData());

        examActions.add(createExamBtn);
        examActions.add(toggleExamBtn);
        examActions.add(refreshExamsBtn);

        examHeaderPanel.add(examTabHeader, BorderLayout.WEST);
        examHeaderPanel.add(examActions, BorderLayout.EAST);

        examTab.add(examHeaderPanel, BorderLayout.NORTH);
        examTab.add(examScroll, BorderLayout.CENTER);

        // Tab 2: Question Bank Table
        String[] questionCols = {"ID", "Subject", "Question Text", "Correct Option", "Marks"};
        questionsTableModel = new DefaultTableModel(questionCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        questionsTable = new JTable(questionsTableModel);
        questionsTable.setRowHeight(28);
        questionsTable.setFont(AppConstants.FONT_BODY);
        questionsTable.getTableHeader().setFont(AppConstants.FONT_BODY_BOLD);
        JScrollPane questionScroll = new JScrollPane(questionsTable);
        questionScroll.setBorder(new LineBorder(AppConstants.COLOR_BORDER, 1));

        JPanel questionTab = new JPanel(new BorderLayout(0, 10));
        questionTab.setBackground(Color.WHITE);
        questionTab.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel questionHeaderPanel = new JPanel(new BorderLayout());
        questionHeaderPanel.setOpaque(false);

        JLabel questionTabHeader = new JLabel("Central Reusable Question Bank (Cross-Exam Repository)");
        questionTabHeader.setFont(AppConstants.FONT_HEADER);
        questionTabHeader.setForeground(AppConstants.COLOR_SECONDARY);

        JPanel questionActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        questionActions.setOpaque(false);

        JButton addQuestionBtn = new JButton("+ Add Question to Bank");
        addQuestionBtn.setFont(AppConstants.FONT_BODY_BOLD);
        addQuestionBtn.setForeground(Color.WHITE);
        addQuestionBtn.setBackground(AppConstants.COLOR_SECONDARY);
        addQuestionBtn.setFocusPainted(false);
        addQuestionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addQuestionBtn.addActionListener(e -> new AddQuestionDialog(mainFrame, examService, this::refreshData).setVisible(true));

        JButton refreshQuestionsBtn = new JButton("🔄 Refresh");
        refreshQuestionsBtn.setFont(AppConstants.FONT_BODY);
        refreshQuestionsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshQuestionsBtn.addActionListener(e -> refreshData());

        questionActions.add(addQuestionBtn);
        questionActions.add(refreshQuestionsBtn);

        questionHeaderPanel.add(questionTabHeader, BorderLayout.WEST);
        questionHeaderPanel.add(questionActions, BorderLayout.EAST);

        questionTab.add(questionHeaderPanel, BorderLayout.NORTH);
        questionTab.add(questionScroll, BorderLayout.CENTER);

        tabbedPane.addTab("Examination Papers", examTab);
        tabbedPane.addTab("Question Bank", questionTab);
        tabbedPane.addTab("Candidate Results & Reports", createResultsTab());

        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createResultsTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 10));
        tab.setBackground(Color.WHITE);
        tab.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top Filter & Search Toolbar
        JPanel topToolBar = new JPanel(new BorderLayout(10, 0));
        topToolBar.setOpaque(false);

        JLabel tabTitle = new JLabel("Candidate Submissions & Grading Analytics");
        tabTitle.setFont(AppConstants.FONT_HEADER);
        tabTitle.setForeground(AppConstants.COLOR_PRIMARY);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        JLabel filterLbl = new JLabel("Exam:");
        filterLbl.setFont(AppConstants.FONT_SMALL);

        examFilterCombo = new JComboBox<>();
        examFilterCombo.setFont(AppConstants.FONT_SMALL);
        examFilterCombo.addItem("All Examinations");
        examFilterCombo.addActionListener(e -> applyResultFilters());

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(AppConstants.FONT_SMALL);

        candidateSearchField = new JTextField(10);
        candidateSearchField.setFont(AppConstants.FONT_SMALL);
        candidateSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyResultFilters();
            }
        });

        JButton viewBreakdownBtn = new JButton("🔍 View Breakdown");
        viewBreakdownBtn.setFont(AppConstants.FONT_BODY_BOLD);
        viewBreakdownBtn.setForeground(Color.WHITE);
        viewBreakdownBtn.setBackground(AppConstants.COLOR_PRIMARY);
        viewBreakdownBtn.setFocusPainted(false);
        viewBreakdownBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewBreakdownBtn.addActionListener(e -> {
            int selectedRow = resultsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a submission from the table to inspect.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (selectedRow < allCandidateResults.size()) {
                ExamResult res = allCandidateResults.get(selectedRow);
                new AttemptReviewDialog((Frame) SwingUtilities.getWindowAncestor(this), res, examService).setVisible(true);
            }
        });

        JButton exportBtn = new JButton("📊 Export CSV");
        exportBtn.setFont(AppConstants.FONT_BODY_BOLD);
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setBackground(new Color(16, 185, 129));
        exportBtn.setFocusPainted(false);
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> exportResultsReport());

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(AppConstants.FONT_BODY);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshData());

        controls.add(filterLbl);
        controls.add(examFilterCombo);
        controls.add(searchLbl);
        controls.add(candidateSearchField);
        controls.add(viewBreakdownBtn);
        controls.add(exportBtn);
        controls.add(refreshBtn);

        topToolBar.add(tabTitle, BorderLayout.WEST);
        topToolBar.add(controls, BorderLayout.EAST);
        tab.add(topToolBar, BorderLayout.NORTH);

        // Results Table
        String[] cols = {"Result ID", "Candidate Name", "Examination Title", "Score", "Max Marks", "Percentage", "Outcome", "Graded At"};
        resultsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        resultsTable = new JTable(resultsTableModel);
        resultsTable.setRowHeight(28);
        resultsTable.setFont(AppConstants.FONT_BODY);
        resultsTable.getTableHeader().setFont(AppConstants.FONT_BODY_BOLD);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        resultsTable.getColumnModel().getColumn(0).setCellRenderer(center);
        resultsTable.getColumnModel().getColumn(3).setCellRenderer(center);
        resultsTable.getColumnModel().getColumn(4).setCellRenderer(center);
        resultsTable.getColumnModel().getColumn(5).setCellRenderer(center);
        resultsTable.getColumnModel().getColumn(6).setCellRenderer(center);

        JScrollPane scroll = new JScrollPane(resultsTable);
        scroll.setBorder(new LineBorder(AppConstants.COLOR_BORDER, 1));
        tab.add(scroll, BorderLayout.CENTER);

        return tab;
    }

    private void applyResultFilters() {
        if (resultsTableModel == null) return;
        resultsTableModel.setRowCount(0);

        String selectedExam = (String) examFilterCombo.getSelectedItem();
        boolean filterAllExams = (selectedExam == null || "All Examinations".equals(selectedExam));
        String searchText = candidateSearchField != null ? candidateSearchField.getText().trim().toLowerCase() : "";

        for (ExamResult r : allCandidateResults) {
            boolean examMatches = filterAllExams || (r.getExamTitle() != null && r.getExamTitle().equalsIgnoreCase(selectedExam));
            boolean searchMatches = searchText.isEmpty() ||
                    (r.getStudentName() != null && r.getStudentName().toLowerCase().contains(searchText)) ||
                    (r.getExamTitle() != null && r.getExamTitle().toLowerCase().contains(searchText));

            if (examMatches && searchMatches) {
                resultsTableModel.addRow(new Object[]{
                        r.getResultId(),
                        r.getStudentName() != null ? r.getStudentName() : "Candidate #" + r.getUserId(),
                        r.getExamTitle() != null ? r.getExamTitle() : "Assessment #" + r.getExamId(),
                        String.format("%.1f", r.getScore()),
                        String.format("%.1f", r.getTotalMarks()),
                        String.format("%.1f%%", r.getPercentage()),
                        r.isPassed() ? "✔ PASSED" : "✘ FAILED",
                        r.getGradedAt() != null ? r.getGradedAt().toString() : "Recent"
                });
            }
        }
    }

    private void exportResultsReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("candidate_assessment_results.csv"));
        int choice = chooser.showSaveDialog(this);

        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            boolean ok = examService.exportResultsToCSV(file, allCandidateResults);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Assessment results successfully exported to:\n" + file.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to write CSV file. Please check folder permissions.", "Export Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppConstants.FONT_SMALL);
        titleLbl.setForeground(AppConstants.COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            adminNameLabel.setText("Admin: " + user.getFullName());
        }

        // Refresh exams
        examsTableModel.setRowCount(0);
        List<Exam> exams = examService.getAvailableExams();
        totalExamsLabel.setText(String.valueOf(exams.size()));

        for (Exam ex : exams) {
            examsTableModel.addRow(new Object[]{
                    ex.getExamId(),
                    ex.getTitle(),
                    ex.getDurationMinutes(),
                    ex.getTotalQuestions(),
                    ex.getTotalMarks(),
                    ex.getPassingPercentage() + "%",
                    ex.isActive() ? "Active" : "Inactive"
            });
        }

        // Refresh question bank
        questionsTableModel.setRowCount(0);
        List<Question> bankQuestions = examService.getAllQuestionBank();
        for (Question q : bankQuestions) {
            questionsTableModel.addRow(new Object[]{
                    q.getQuestionId(),
                    q.getSubject(),
                    q.getQuestionText(),
                    "Option " + q.getCorrectOption(),
                    q.getMarks()
            });
        }
        totalQuestionsLabel.setText(String.valueOf(bankQuestions.size()));

        // Refresh Results & Reports
        allCandidateResults.clear();
        List<ExamResult> results = examService.getAllCandidateResults();
        allCandidateResults.addAll(results);

        if (examFilterCombo != null) {
            String currentFilter = (String) examFilterCombo.getSelectedItem();
            examFilterCombo.removeAllItems();
            examFilterCombo.addItem("All Examinations");
            for (Exam ex : exams) {
                examFilterCombo.addItem(ex.getTitle());
            }
            if (currentFilter != null) {
                examFilterCombo.setSelectedItem(currentFilter);
            }
        }

        applyResultFilters();

        // Calculate dynamic system completion / pass rate
        if (!results.isEmpty()) {
            int passedCount = 0;
            for (ExamResult r : results) {
                if (r.isPassed()) passedCount++;
            }
            double passRate = ((double) passedCount / results.size()) * 100.0;
            completionRateLabel.setText(String.format("%.0f%%", passRate));
        } else {
            completionRateLabel.setText("N/A");
        }
    }
}

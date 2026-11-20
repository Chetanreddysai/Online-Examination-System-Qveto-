package com.qveto.exam.ui;

import com.qveto.exam.model.Exam;
import com.qveto.exam.model.ExamResult;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Student Dashboard Panel (Week 1 Application Shell).
 * Displays candidate greeting, profile overview, available examinations catalog,
 * examination instructions, and preparation guidelines.
 */
public class StudentDashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService;
    private final ExamService examService;

    private JLabel studentNameLabel;
    private JLabel studentEmailLabel;
    private JTable examsTable;
    private DefaultTableModel examsTableModel;
    private JTextField examSearchField;
    private final List<Exam> cachedAvailableExams = new ArrayList<>();

    // Assessment History
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private final List<ExamResult> currentStudentHistory = new ArrayList<>();

    public StudentDashboardPanel(MainFrame mainFrame, AuthService authService, ExamService examService) {
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
        topBar.setBackground(AppConstants.COLOR_SECONDARY);
        topBar.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel brandLabel = new JLabel("Qveto Candidate Portal — Online Examination Assessment");
        brandLabel.setFont(AppConstants.FONT_HEADER);
        brandLabel.setForeground(Color.WHITE);

        JPanel rightTopPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightTopPanel.setOpaque(false);

        studentNameLabel = new JLabel("Candidate: Student");
        studentNameLabel.setFont(AppConstants.FONT_BODY_BOLD);
        studentNameLabel.setForeground(Color.WHITE);

        JButton profileBtn = new JButton("👤 My Profile & Security");
        profileBtn.setFont(AppConstants.FONT_SMALL);
        profileBtn.setForeground(AppConstants.COLOR_SECONDARY);
        profileBtn.setBackground(Color.WHITE);
        profileBtn.setFocusPainted(false);
        profileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileBtn.addActionListener(e -> new UserProfileDialog(mainFrame, authService, this::refreshData).setVisible(true));

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setFont(AppConstants.FONT_SMALL);
        logoutBtn.setForeground(AppConstants.COLOR_SECONDARY);
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> mainFrame.handleLogout());

        rightTopPanel.add(studentNameLabel);
        rightTopPanel.add(profileBtn);
        rightTopPanel.add(logoutBtn);

        topBar.add(brandLabel, BorderLayout.WEST);
        topBar.add(rightTopPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Content Area
        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setBackground(AppConstants.COLOR_BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Welcome / Profile Banner
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(Color.WHITE);
        bannerPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel bannerText = new JPanel(new GridLayout(2, 1, 0, 4));
        bannerText.setOpaque(false);
        JLabel welcomeMsg = new JLabel("Welcome to your Assessment Dashboard");
        welcomeMsg.setFont(AppConstants.FONT_TITLE);
        welcomeMsg.setForeground(AppConstants.COLOR_PRIMARY);

        studentEmailLabel = new JLabel("Candidate Email: -");
        studentEmailLabel.setFont(AppConstants.FONT_BODY);
        studentEmailLabel.setForeground(AppConstants.COLOR_TEXT_MUTED);

        bannerText.add(welcomeMsg);
        bannerText.add(studentEmailLabel);
        bannerPanel.add(bannerText, BorderLayout.CENTER);

        contentPanel.add(bannerPanel, BorderLayout.NORTH);

        // Center Split: Left = Available Exams Table, Right = Test Instructions
        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 16, 0));
        centerSplit.setOpaque(false);

        // Left Panel: Available Exams
        JPanel examsPanel = new JPanel(new BorderLayout(0, 10));
        examsPanel.setBackground(Color.WHITE);
        examsPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel examsHeader = new JLabel("Available Assessments");
        examsHeader.setFont(AppConstants.FONT_HEADER);
        examsHeader.setForeground(AppConstants.COLOR_PRIMARY);

        String[] cols = {"ID", "Assessment Title", "Duration", "Questions", "Max Marks"};
        examsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        examsTable = new JTable(examsTableModel);
        examsTable.setRowHeight(28);
        examsTable.setFont(AppConstants.FONT_BODY);
        examsTable.getTableHeader().setFont(AppConstants.FONT_BODY_BOLD);
        JScrollPane scroll = new JScrollPane(examsTable);
        scroll.setBorder(new LineBorder(AppConstants.COLOR_BORDER, 1));

        JPanel examBtnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        examBtnRow.setOpaque(false);

        JButton previewBtn = new JButton("📋 View Details & Syllabus");
        previewBtn.setFont(AppConstants.FONT_BODY);
        previewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        previewBtn.addActionListener(e -> {
            int selectedRow = examsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an examination from the table to preview details.", "No Exam Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int examId = (Integer) examsTableModel.getValueAt(selectedRow, 0);
            List<Exam> exams = examService.getAvailableExams();
            Exam targetExam = null;
            for (Exam ex : exams) {
                if (ex.getExamId() == examId) {
                    targetExam = ex;
                    break;
                }
            }
            new ExamDetailsDialog((Frame) SwingUtilities.getWindowAncestor(this), targetExam).setVisible(true);
        });

        JButton startTestBtn = new JButton("Start Assessment");
        startTestBtn.setFont(AppConstants.FONT_BODY_BOLD);
        startTestBtn.setForeground(Color.WHITE);
        startTestBtn.setBackground(AppConstants.COLOR_PRIMARY);
        startTestBtn.setFocusPainted(false);
        startTestBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startTestBtn.addActionListener(e -> {
            int selectedRow = examsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an examination from the table above.", "No Exam Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int examId = (Integer) examsTableModel.getValueAt(selectedRow, 0);
            List<Exam> exams = examService.getAvailableExams();
            Exam targetExam = null;
            for (Exam ex : exams) {
                if (ex.getExamId() == examId) {
                    targetExam = ex;
                    break;
                }
            }
            if (targetExam != null) {
                mainFrame.startExam(targetExam);
            }
        });

        examBtnRow.add(previewBtn);
        examBtnRow.add(startTestBtn);

        // Filter toolbar
        JPanel filterToolbar = new JPanel(new BorderLayout(8, 0));
        filterToolbar.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍 Filter: ");
        searchIcon.setFont(AppConstants.FONT_BODY_BOLD);
        searchIcon.setForeground(AppConstants.COLOR_TEXT_PRIMARY);

        examSearchField = new JTextField();
        examSearchField.setFont(AppConstants.FONT_BODY);
        examSearchField.setToolTipText("Filter examinations by title or keyword");
        examSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterExamsTable(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterExamsTable(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterExamsTable(); }
        });

        filterToolbar.add(searchIcon, BorderLayout.WEST);
        filterToolbar.add(examSearchField, BorderLayout.CENTER);

        JPanel tableArea = new JPanel(new BorderLayout(0, 8));
        tableArea.setOpaque(false);
        tableArea.add(filterToolbar, BorderLayout.NORTH);
        tableArea.add(scroll, BorderLayout.CENTER);

        examsPanel.add(examsHeader, BorderLayout.NORTH);
        examsPanel.add(tableArea, BorderLayout.CENTER);
        examsPanel.add(examBtnRow, BorderLayout.SOUTH);

        // Right Panel: Instructions
        JPanel instructionsPanel = new JPanel(new BorderLayout(0, 10));
        instructionsPanel.setBackground(Color.WHITE);
        instructionsPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel instructionsHeader = new JLabel("Examination Guidelines & Policy");
        instructionsHeader.setFont(AppConstants.FONT_HEADER);
        instructionsHeader.setForeground(AppConstants.COLOR_SECONDARY);

        JTextArea instructionsText = new JTextArea();
        instructionsText.setFont(AppConstants.FONT_BODY);
        instructionsText.setEditable(false);
        instructionsText.setLineWrap(true);
        instructionsText.setWrapStyleWord(true);
        instructionsText.setBackground(Color.WHITE);
        instructionsText.setText(
                "Important Rules for Candidate Assessment:\n\n" +
                "1. Timed Assessments:\n" +
                "   Every examination has a strict duration countdown powered by javax.swing.Timer.\n\n" +
                "2. Answer Persistence:\n" +
                "   Your selected options are saved continuously as you move between Next and Previous questions.\n\n" +
                "3. Auto-Submission at Expiry:\n" +
                "   When the timer reaches 00:00, your exam will be automatically locked and submitted.\n\n" +
                "4. Manual Submission:\n" +
                "   You may finish and submit early with confirmation once all questions are reviewed.\n\n" +
                "5. Post-Submission Integrity:\n" +
                "   Once an assessment is submitted or expired, answer changes are permanently prohibited.\n\n" +
                "6. Scoring & Immediate Result:\n" +
                "   Upon submission, your marks, percentage, and pass/fail status are calculated immediately."
        );

        instructionsPanel.add(instructionsHeader, BorderLayout.NORTH);
        instructionsPanel.add(new JScrollPane(instructionsText), BorderLayout.CENTER);

        centerSplit.add(examsPanel);
        centerSplit.add(instructionsPanel);

        // Tabbed Workspaces for Student
        JTabbedPane studentTabs = new JTabbedPane();
        studentTabs.setFont(AppConstants.FONT_SUBHEADER);
        studentTabs.addTab("Available Examinations", centerSplit);
        studentTabs.addTab("My Assessment History & Reports", createHistoryPanel());

        contentPanel.add(studentTabs, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Header toolbar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("My Completed Assessments & Graded Reports");
        title.setFont(AppConstants.FONT_HEADER);
        title.setForeground(AppConstants.COLOR_PRIMARY);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton viewBreakdownBtn = new JButton("🔍 View Performance Breakdown");
        viewBreakdownBtn.setFont(AppConstants.FONT_BODY_BOLD);
        viewBreakdownBtn.setForeground(Color.WHITE);
        viewBreakdownBtn.setBackground(AppConstants.COLOR_PRIMARY);
        viewBreakdownBtn.setFocusPainted(false);
        viewBreakdownBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewBreakdownBtn.addActionListener(e -> {
            int selectedRow = historyTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an attempt from the table below to review answers.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (selectedRow < currentStudentHistory.size()) {
                ExamResult res = currentStudentHistory.get(selectedRow);
                new AttemptReviewDialog((Frame) SwingUtilities.getWindowAncestor(this), res, examService).setVisible(true);
            }
        });

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(AppConstants.FONT_BODY);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshData());

        actions.add(viewBreakdownBtn);
        actions.add(refreshBtn);

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(actions, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // History Table
        String[] cols = {"Result ID", "Assessment Title", "Score Earned", "Max Marks", "Percentage", "Outcome", "Date & Time"};
        historyTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.setRowHeight(28);
        historyTable.setFont(AppConstants.FONT_BODY);
        historyTable.getTableHeader().setFont(AppConstants.FONT_BODY_BOLD);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        historyTable.getColumnModel().getColumn(0).setCellRenderer(center);
        historyTable.getColumnModel().getColumn(2).setCellRenderer(center);
        historyTable.getColumnModel().getColumn(3).setCellRenderer(center);
        historyTable.getColumnModel().getColumn(4).setCellRenderer(center);
        historyTable.getColumnModel().getColumn(5).setCellRenderer(center);

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(new LineBorder(AppConstants.COLOR_BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    public void refreshData() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            studentNameLabel.setText("Candidate: " + user.getFullName());
            studentEmailLabel.setText("Candidate Email: " + user.getEmail() + " | Role: Student");
        }

        // 1. Refresh available exams
        cachedAvailableExams.clear();
        List<Exam> exams = examService.getAvailableExams();
        cachedAvailableExams.addAll(exams);
        filterExamsTable();
        if (examsTable.getRowCount() > 0) {
            examsTable.setRowSelectionInterval(0, 0);
        }

        // 2. Refresh student history
        historyTableModel.setRowCount(0);
        currentStudentHistory.clear();
        int userId = user != null ? user.getUserId() : 2;
        List<ExamResult> history = examService.getStudentHistory(userId);
        currentStudentHistory.addAll(history);

        for (ExamResult r : history) {
            historyTableModel.addRow(new Object[]{
                    r.getResultId(),
                    r.getExamTitle() != null ? r.getExamTitle() : "Assessment #" + r.getExamId(),
                    String.format("%.1f", r.getScore()),
                    String.format("%.1f", r.getTotalMarks()),
                    String.format("%.1f%%", r.getPercentage()),
                    r.isPassed() ? "✔ PASSED" : "✘ FAILED",
                    r.getGradedAt() != null ? r.getGradedAt().toString() : "Recent"
            });
        }
    }

    /**
     * Filters the available examinations table dynamically by keyword in title or description.
     */
    public void filterExamsTable() {
        String keyword = examSearchField != null ? examSearchField.getText().trim().toLowerCase() : "";
        examsTableModel.setRowCount(0);
        for (Exam ex : cachedAvailableExams) {
            boolean matches = keyword.isEmpty() ||
                    ex.getTitle().toLowerCase().contains(keyword) ||
                    (ex.getDescription() != null && ex.getDescription().toLowerCase().contains(keyword));
            if (matches) {
                examsTableModel.addRow(new Object[]{
                        ex.getExamId(),
                        ex.getTitle(),
                        ex.getDurationMinutes() + " mins",
                        ex.getTotalQuestions() + " MCQs",
                        ex.getTotalMarks() + " pts"
                });
            }
        }
    }

    public JTable getExamsTable() {
        return examsTable;
    }

    public JTextField getExamSearchField() {
        return examSearchField;
    }

    public List<Exam> getCachedAvailableExams() {
        return Collections.unmodifiableList(cachedAvailableExams);
    }
}

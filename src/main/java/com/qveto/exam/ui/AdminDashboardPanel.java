package com.qveto.exam.ui;

import com.qveto.exam.model.Exam;
import com.qveto.exam.model.Question;
import com.qveto.exam.model.User;
import com.qveto.exam.service.AuthService;
import com.qveto.exam.service.ExamService;
import com.qveto.exam.util.AppConstants;
import com.qveto.exam.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
    private JTable examsTable;
    private DefaultTableModel examsTableModel;
    private JTable questionsTable;
    private DefaultTableModel questionsTableModel;

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

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setFont(AppConstants.FONT_SMALL);
        logoutBtn.setForeground(AppConstants.COLOR_PRIMARY);
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> mainFrame.handleLogout());

        rightTopPanel.add(adminNameLabel);
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
        JLabel completionRateLabel = new JLabel("100%", JLabel.CENTER);

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
        JLabel examTabHeader = new JLabel("Manage Published & Draft Examination Papers");
        examTabHeader.setFont(AppConstants.FONT_HEADER);
        examTabHeader.setForeground(AppConstants.COLOR_PRIMARY);
        examTab.add(examTabHeader, BorderLayout.NORTH);
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
        JLabel questionTabHeader = new JLabel("Central Reusable Question Bank (Cross-Exam Repository)");
        questionTabHeader.setFont(AppConstants.FONT_HEADER);
        questionTabHeader.setForeground(AppConstants.COLOR_SECONDARY);
        questionTab.add(questionTabHeader, BorderLayout.NORTH);
        questionTab.add(questionScroll, BorderLayout.CENTER);

        tabbedPane.addTab("Examination Papers", examTab);
        tabbedPane.addTab("Question Bank", questionTab);

        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
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
        int totalQ = 0;
        for (Exam ex : exams) {
            List<Question> qs = examService.getQuestionsForExam(ex.getExamId());
            for (Question q : qs) {
                totalQ++;
                questionsTableModel.addRow(new Object[]{
                        q.getQuestionId(),
                        q.getSubject(),
                        q.getQuestionText(),
                        "Option " + q.getCorrectOption(),
                        q.getMarks()
                });
            }
        }
        totalQuestionsLabel.setText(String.valueOf(totalQ));
    }
}

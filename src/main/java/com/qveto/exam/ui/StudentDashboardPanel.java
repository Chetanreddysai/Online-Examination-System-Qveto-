package com.qveto.exam.ui;

import com.qveto.exam.model.Exam;
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
            String title = (String) examsTableModel.getValueAt(selectedRow, 1);
            JOptionPane.showMessageDialog(this,
                    "Selected: " + title + "\n\n" +
                    "Examination registered!\n" +
                    "The Timed Assessment Navigation screen with javax.swing.Timer countdown will launch here.",
                    "Examination Ready",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        examBtnRow.add(previewBtn);
        examBtnRow.add(startTestBtn);

        examsPanel.add(examsHeader, BorderLayout.NORTH);
        examsPanel.add(scroll, BorderLayout.CENTER);
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
        contentPanel.add(centerSplit, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            studentNameLabel.setText("Candidate: " + user.getFullName());
            studentEmailLabel.setText("Candidate Email: " + user.getEmail() + " | Role: Student");
        }

        examsTableModel.setRowCount(0);
        List<Exam> exams = examService.getAvailableExams();
        for (Exam ex : exams) {
            examsTableModel.addRow(new Object[]{
                    ex.getExamId(),
                    ex.getTitle(),
                    ex.getDurationMinutes() + " mins",
                    ex.getTotalQuestions() + " MCQs",
                    ex.getTotalMarks() + " pts"
            });
        }
        if (examsTable.getRowCount() > 0) {
            examsTable.setRowSelectionInterval(0, 0);
        }
    }
}

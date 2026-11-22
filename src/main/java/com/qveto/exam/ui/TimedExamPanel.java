package com.qveto.exam.ui;

import com.qveto.exam.model.*;
import com.qveto.exam.service.ExamService;
import com.qveto.exam.util.AppConstants;
import com.qveto.exam.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Timed Examination Panel featuring one-question-at-a-time navigation,
 * real-time answer persistence, question palette jumping, visible javax.swing.Timer countdown,
 * and automatic expiry submission.
 */
public class TimedExamPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ExamService examService;

    // Active session state
    private Exam currentExam;
    private ExamAttempt currentAttempt;
    private List<Question> examQuestions = new ArrayList<>();
    private final Map<Integer, String> selectedAnswers = new HashMap<>();
    private final Set<Integer> flaggedQuestions = new HashSet<>();
    private int currentIndex = 0;
    private boolean examActive = false;

    // Countdown Timer state
    private javax.swing.Timer countdownTimer;
    private int remainingSeconds;
    private int totalDurationSeconds;
    private boolean warned5Min = false;
    private boolean warned1Min = false;

    // UI Components
    private JLabel examTitleLabel;
    private JLabel timerBadgeLabel;
    private JLabel questionIndexLabel;
    private JLabel subjectBadgeLabel;
    private JLabel marksBadgeLabel;
    private JProgressBar examProgressBar;
    private JLabel progressStatusLabel;
    private JTextArea questionTextArea;

    private JRadioButton optionARadio;
    private JRadioButton optionBRadio;
    private JRadioButton optionCRadio;
    private JRadioButton optionDRadio;
    private ButtonGroup optionGroup;

    private JButton prevBtn;
    private JButton nextBtn;
    private JButton clearBtn;
    private JButton flagBtn;
    private JButton submitBtn;

    private JPanel paletteGridPanel;
    private final List<JButton> paletteButtons = new ArrayList<>();

    public TimedExamPanel(MainFrame mainFrame, ExamService examService) {
        this.mainFrame = mainFrame;
        this.examService = examService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.COLOR_BACKGROUND);

        // 1. Top Header Bar with Timer & Title
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(AppConstants.COLOR_PRIMARY);
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        examTitleLabel = new JLabel("Timed Assessment");
        examTitleLabel.setFont(AppConstants.FONT_HEADER);
        examTitleLabel.setForeground(Color.WHITE);

        JPanel timerContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        timerContainer.setOpaque(false);

        timerBadgeLabel = new JLabel("⏱️ 00:00", JLabel.CENTER);
        timerBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timerBadgeLabel.setForeground(Color.WHITE);
        timerBadgeLabel.setOpaque(true);
        timerBadgeLabel.setBackground(new Color(30, 58, 138));
        timerBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(96, 165, 250), 1, true),
                new EmptyBorder(4, 16, 4, 16)
        ));
        timerContainer.add(timerBadgeLabel);

        submitBtn = new JButton("Finish & Submit Exam");
        submitBtn.setFont(AppConstants.FONT_BODY_BOLD);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(AppConstants.COLOR_DANGER);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> confirmAndSubmit(false));

        topBar.add(examTitleLabel, BorderLayout.WEST);
        topBar.add(timerContainer, BorderLayout.CENTER);
        topBar.add(submitBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // 2. Main Content Split: Center = Question Card, East = Question Palette
        JPanel bodyPanel = new JPanel(new BorderLayout(16, 0));
        bodyPanel.setBackground(AppConstants.COLOR_BACKGROUND);
        bodyPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        bodyPanel.add(createQuestionCard(), BorderLayout.CENTER);
        bodyPanel.add(createPaletteCard(), BorderLayout.EAST);

        add(bodyPanel, BorderLayout.CENTER);
        setupKeyBindings();
    }

    private JPanel createQuestionCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        // Subheader with Question Index, Subject and Points
        JPanel qMetaPanel = new JPanel(new BorderLayout());
        qMetaPanel.setOpaque(false);

        questionIndexLabel = new JLabel("Question 1 of 10");
        questionIndexLabel.setFont(AppConstants.FONT_HEADER);
        questionIndexLabel.setForeground(AppConstants.COLOR_PRIMARY);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badges.setOpaque(false);

        subjectBadgeLabel = new JLabel("Java Programming");
        subjectBadgeLabel.setFont(AppConstants.FONT_SMALL);
        subjectBadgeLabel.setForeground(AppConstants.COLOR_SECONDARY);
        subjectBadgeLabel.setOpaque(true);
        subjectBadgeLabel.setBackground(new Color(240, 253, 250));
        subjectBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(204, 251, 241), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        marksBadgeLabel = new JLabel("10.0 pts");
        marksBadgeLabel.setFont(AppConstants.FONT_SMALL);
        marksBadgeLabel.setForeground(AppConstants.COLOR_PRIMARY);
        marksBadgeLabel.setOpaque(true);
        marksBadgeLabel.setBackground(new Color(239, 246, 255));
        marksBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        badges.add(subjectBadgeLabel);
        badges.add(marksBadgeLabel);

        qMetaPanel.add(questionIndexLabel, BorderLayout.WEST);
        qMetaPanel.add(badges, BorderLayout.EAST);

        // Progress Tracking Bar
        JPanel progressPanel = new JPanel(new BorderLayout(10, 0));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(new EmptyBorder(6, 0, 0, 0));

        progressStatusLabel = new JLabel("Answered: 0 of 0 (0%)");
        progressStatusLabel.setFont(AppConstants.FONT_SMALL);
        progressStatusLabel.setForeground(AppConstants.COLOR_TEXT_MUTED);

        examProgressBar = new JProgressBar(0, 100);
        examProgressBar.setValue(0);
        examProgressBar.setStringPainted(true);
        examProgressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        examProgressBar.setForeground(AppConstants.COLOR_SUCCESS);
        examProgressBar.setBackground(new Color(241, 245, 249));
        examProgressBar.setPreferredSize(new Dimension(180, 18));

        progressPanel.add(progressStatusLabel, BorderLayout.WEST);
        progressPanel.add(examProgressBar, BorderLayout.EAST);

        JPanel topHeaderContainer = new JPanel(new BorderLayout(0, 6));
        topHeaderContainer.setOpaque(false);
        topHeaderContainer.add(qMetaPanel, BorderLayout.NORTH);
        topHeaderContainer.add(progressPanel, BorderLayout.SOUTH);

        // Question Statement Area
        questionTextArea = new JTextArea();
        questionTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setEditable(false);
        questionTextArea.setBackground(new Color(249, 250, 251));
        questionTextArea.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane qScroll = new JScrollPane(questionTextArea);
        qScroll.setBorder(new LineBorder(AppConstants.COLOR_BORDER, 1, true));
        qScroll.setPreferredSize(new Dimension(500, 100));

        // Options Radio Buttons Panel
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(false);

        optionGroup = new ButtonGroup();
        optionARadio = createOptionRadio("A");
        optionBRadio = createOptionRadio("B");
        optionCRadio = createOptionRadio("C");
        optionDRadio = createOptionRadio("D");

        optionGroup.add(optionARadio);
        optionGroup.add(optionBRadio);
        optionGroup.add(optionCRadio);
        optionGroup.add(optionDRadio);

        optionsPanel.add(optionARadio);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        optionsPanel.add(optionBRadio);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        optionsPanel.add(optionCRadio);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        optionsPanel.add(optionDRadio);

        // Bottom Controls Toolbar
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setOpaque(false);

        prevBtn = new JButton("← Previous");
        prevBtn.setFont(AppConstants.FONT_BODY_BOLD);
        prevBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        prevBtn.addActionListener(e -> navigateQuestion(currentIndex - 1));

        clearBtn = new JButton("Clear Answer");
        clearBtn.setFont(AppConstants.FONT_BODY);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> clearSelectedAnswer());

        flagBtn = new JButton("🚩 Flag for Review");
        flagBtn.setFont(AppConstants.FONT_BODY);
        flagBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        flagBtn.addActionListener(e -> toggleFlagCurrentQuestion());

        nextBtn = new JButton("Next →");
        nextBtn.setFont(AppConstants.FONT_BODY_BOLD);
        nextBtn.setForeground(Color.WHITE);
        nextBtn.setBackground(AppConstants.COLOR_PRIMARY);
        nextBtn.setFocusPainted(false);
        nextBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nextBtn.addActionListener(e -> navigateQuestion(currentIndex + 1));

        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftNav.setOpaque(false);
        leftNav.add(prevBtn);
        leftNav.add(clearBtn);
        leftNav.add(flagBtn);

        navPanel.add(leftNav, BorderLayout.WEST);
        navPanel.add(nextBtn, BorderLayout.EAST);

        JLabel shortcutHint = new JLabel("⚡ Keyboard Shortcuts: [A-D or 1-4] Select Option | [← / →] Navigate | [F] Flag for Review | [C] Clear");
        shortcutHint.setFont(AppConstants.FONT_SMALL);
        shortcutHint.setForeground(AppConstants.COLOR_TEXT_MUTED);

        JPanel bottomControlPanel = new JPanel(new BorderLayout(0, 6));
        bottomControlPanel.setOpaque(false);
        bottomControlPanel.add(navPanel, BorderLayout.CENTER);
        bottomControlPanel.add(shortcutHint, BorderLayout.SOUTH);

        // Center Stack
        JPanel centerStack = new JPanel(new BorderLayout(0, 14));
        centerStack.setOpaque(false);
        centerStack.add(qScroll, BorderLayout.NORTH);
        centerStack.add(optionsPanel, BorderLayout.CENTER);

        card.add(topHeaderContainer, BorderLayout.NORTH);
        card.add(centerStack, BorderLayout.CENTER);
        card.add(bottomControlPanel, BorderLayout.SOUTH);

        return card;
    }

    private JRadioButton createOptionRadio(String optionKey) {
        JRadioButton radio = new JRadioButton();
        radio.setFont(AppConstants.FONT_BODY);
        radio.setBackground(Color.WHITE);
        radio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        radio.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        radio.setAlignmentX(Component.LEFT_ALIGNMENT);
        radio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        radio.addActionListener(e -> {
            if (radio.isSelected() && currentIndex >= 0 && currentIndex < examQuestions.size()) {
                Question q = examQuestions.get(currentIndex);
                selectedAnswers.put(q.getQuestionId(), optionKey);
                if (currentAttempt != null) {
                    examService.recordAnswer(currentAttempt.getAttemptId(), q.getQuestionId(), optionKey);
                }
                updatePaletteButtons();
            }
        });

        return radio;
    }

    private JPanel createPaletteCard() {
        JPanel palette = new JPanel(new BorderLayout(0, 12));
        palette.setBackground(Color.WHITE);
        palette.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        palette.setPreferredSize(new Dimension(240, 400));

        JLabel palHeader = new JLabel("Question Navigator");
        palHeader.setFont(AppConstants.FONT_SUBHEADER);
        palHeader.setForeground(AppConstants.COLOR_TEXT_PRIMARY);

        paletteGridPanel = new JPanel(new GridLayout(0, 4, 6, 6));
        paletteGridPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(paletteGridPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        // Legend Panel
        JPanel legendPanel = new JPanel(new GridLayout(4, 1, 0, 4));
        legendPanel.setOpaque(false);
        legendPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        legendPanel.add(createLegendItem(new Color(16, 185, 129), "Answered"));
        legendPanel.add(createLegendItem(new Color(245, 158, 11), "Flagged for Review"));
        legendPanel.add(createLegendItem(AppConstants.COLOR_PRIMARY, "Current Question"));
        legendPanel.add(createLegendItem(new Color(229, 231, 235), "Not Answered"));

        palette.add(palHeader, BorderLayout.NORTH);
        palette.add(scroll, BorderLayout.CENTER);
        palette.add(legendPanel, BorderLayout.SOUTH);

        return palette;
    }

    private JPanel createLegendItem(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        JLabel dot = new JLabel("  ");
        dot.setOpaque(true);
        dot.setBackground(color);
        dot.setPreferredSize(new Dimension(14, 14));
        dot.setBorder(new LineBorder(Color.GRAY, 1, true));

        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.FONT_SMALL);
        lbl.setForeground(AppConstants.COLOR_TEXT_MUTED);

        p.add(dot);
        p.add(lbl);
        return p;
    }

    /**
     * Initializes the timed assessment session for a candidate.
     */
    public void startAssessment(Exam exam) {
        this.currentExam = exam;
        this.selectedAnswers.clear();
        this.flaggedQuestions.clear();
        this.currentIndex = 0;
        this.examActive = true;

        User user = SessionManager.getCurrentUser();
        int userId = user != null ? user.getUserId() : 1;

        // Initialize attempt
        this.currentAttempt = examService.startAttempt(userId, exam.getExamId());

        // Fetch questions
        this.examQuestions = examService.getQuestionsForExam(exam.getExamId());
        if (this.examQuestions.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No questions are currently mapped to this examination.",
                    "Empty Examination",
                    JOptionPane.WARNING_MESSAGE);
            mainFrame.showCard(AppConstants.CARD_STUDENT_DASHBOARD);
            return;
        }

        examTitleLabel.setText(exam.getTitle());

        // Build question palette buttons
        paletteGridPanel.removeAll();
        paletteButtons.clear();
        for (int i = 0; i < examQuestions.size(); i++) {
            final int index = i;
            JButton btn = new JButton(String.valueOf(i + 1));
            btn.setFont(AppConstants.FONT_SMALL);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> navigateQuestion(index));
            paletteButtons.add(btn);
            paletteGridPanel.add(btn);
        }
        paletteGridPanel.revalidate();
        paletteGridPanel.repaint();

        // Initialize countdown timer (duration in minutes)
        totalDurationSeconds = exam.getDurationMinutes() * 60;
        remainingSeconds = totalDurationSeconds;
        warned5Min = false;
        warned1Min = false;
        updateTimerDisplay();

        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }

        countdownTimer = new javax.swing.Timer(1000, e -> {
            remainingSeconds--;
            updateTimerDisplay();

            if (remainingSeconds <= 0) {
                countdownTimer.stop();
                handleAutoTimeoutSubmission();
            }
        });
        countdownTimer.start();

        // Load first question
        displayCurrentQuestion();
    }

    public void updateTimerDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String timeStr = String.format("⏱️ %02d:%02d", minutes, seconds);
        timerBadgeLabel.setText(timeStr);

        // Warning color transitions and visual pulse alerts
        if (remainingSeconds <= 30) {
            // Pulse alert in critical last 30 seconds
            if (remainingSeconds % 2 == 0) {
                timerBadgeLabel.setBackground(AppConstants.COLOR_DANGER);
            } else {
                timerBadgeLabel.setBackground(new Color(153, 27, 27)); // Deep crimson pulse
            }
            timerBadgeLabel.setToolTipText("🚨 CRITICAL: Under 30 seconds remaining! System will auto-submit at 00:00.");
        } else if (remainingSeconds <= 60) {
            timerBadgeLabel.setBackground(AppConstants.COLOR_DANGER); // Solid Red (< 1 min)
            warned1Min = true;
            timerBadgeLabel.setToolTipText("🚨 WARNING: 1 minute remaining before automatic submission.");
        } else if (remainingSeconds <= 300) {
            timerBadgeLabel.setBackground(AppConstants.COLOR_WARNING); // Amber (< 5 min)
            warned5Min = true;
            timerBadgeLabel.setToolTipText("⚠️ Notice: 5 minutes remaining. Please review your answers.");
        } else {
            timerBadgeLabel.setBackground(new Color(30, 58, 138));
            timerBadgeLabel.setToolTipText("Assessment timer active. Standard pace.");
        }
    }

    private void displayCurrentQuestion() {
        if (currentIndex < 0 || currentIndex >= examQuestions.size()) {
            return;
        }

        Question q = examQuestions.get(currentIndex);
        questionIndexLabel.setText("Question " + (currentIndex + 1) + " of " + examQuestions.size());
        subjectBadgeLabel.setText(q.getSubject());
        marksBadgeLabel.setText(String.format("%.1f pts", q.getMarks()));
        questionTextArea.setText(q.getQuestionText());

        // Set Option texts
        optionARadio.setText("A.  " + q.getOptionA());
        optionBRadio.setText("B.  " + q.getOptionB());
        optionCRadio.setText("C.  " + q.getOptionC());
        optionDRadio.setText("D.  " + q.getOptionD());

        // Restore previously selected answer if present
        optionGroup.clearSelection();
        String selected = selectedAnswers.get(q.getQuestionId());
        if ("A".equalsIgnoreCase(selected)) optionARadio.setSelected(true);
        else if ("B".equalsIgnoreCase(selected)) optionBRadio.setSelected(true);
        else if ("C".equalsIgnoreCase(selected)) optionCRadio.setSelected(true);
        else if ("D".equalsIgnoreCase(selected)) optionDRadio.setSelected(true);

        // Update Nav button states
        prevBtn.setEnabled(currentIndex > 0);
        if (currentIndex == examQuestions.size() - 1) {
            nextBtn.setText("Review & Submit");
            nextBtn.setBackground(AppConstants.COLOR_SUCCESS);
        } else {
            nextBtn.setText("Next →");
            nextBtn.setBackground(AppConstants.COLOR_PRIMARY);
        }

        updateFlagButtonState();
        updatePaletteButtons();
        updateProgressDisplay();
    }

    private void navigateQuestion(int targetIndex) {
        if (targetIndex >= examQuestions.size()) {
            confirmAndSubmit(false);
            return;
        }
        if (targetIndex >= 0 && targetIndex < examQuestions.size()) {
            currentIndex = targetIndex;
            displayCurrentQuestion();
        }
    }

    private void clearSelectedAnswer() {
        if (currentIndex >= 0 && currentIndex < examQuestions.size()) {
            Question q = examQuestions.get(currentIndex);
            selectedAnswers.remove(q.getQuestionId());
            optionGroup.clearSelection();
            if (currentAttempt != null) {
                examService.recordAnswer(currentAttempt.getAttemptId(), q.getQuestionId(), null);
            }
            updatePaletteButtons();
            updateProgressDisplay();
        }
    }

    private void updatePaletteButtons() {
        for (int i = 0; i < examQuestions.size(); i++) {
            Question q = examQuestions.get(i);
            JButton btn = paletteButtons.get(i);

            boolean isAnswered = selectedAnswers.containsKey(q.getQuestionId());
            boolean isFlagged = flaggedQuestions.contains(q.getQuestionId());
            boolean isCurrent = (i == currentIndex);

            if (isCurrent) {
                btn.setBackground(AppConstants.COLOR_PRIMARY);
                btn.setForeground(Color.WHITE);
            } else if (isFlagged) {
                btn.setBackground(new Color(245, 158, 11)); // Amber
                btn.setForeground(Color.WHITE);
            } else if (isAnswered) {
                btn.setBackground(new Color(16, 185, 129)); // Green
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(new Color(243, 244, 246));
                btn.setForeground(AppConstants.COLOR_TEXT_PRIMARY);
            }
        }
    }

    private void confirmAndSubmit(boolean isTimeout) {
        int answeredCount = selectedAnswers.size();
        int total = examQuestions.size();
        int flaggedCount = flaggedQuestions.size();

        if (!isTimeout) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("You have answered %d of %d questions.\n", answeredCount, total));
            if (flaggedCount > 0) {
                sb.append(String.format("⚠️ Note: You have %d question(s) flagged for review.\n", flaggedCount));
            }
            sb.append("\nOnce submitted, answers cannot be modified.\n");
            sb.append("Are you sure you want to finish and submit your exam?");

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    sb.toString(),
                    "Confirm Final Submission",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        finalizeExamSubmission(isTimeout ? SubmissionType.AUTO_TIMEOUT : SubmissionType.MANUAL);
    }

    private void handleAutoTimeoutSubmission() {
        // Disable options immediately to prevent post-submission tampering
        optionARadio.setEnabled(false);
        optionBRadio.setEnabled(false);
        optionCRadio.setEnabled(false);
        optionDRadio.setEnabled(false);
        submitBtn.setEnabled(false);

        JOptionPane.showMessageDialog(
                this,
                "Time has expired!\nYour assessment answers have been locked and submitted automatically.",
                "Examination Timeout",
                JOptionPane.WARNING_MESSAGE
        );

        finalizeExamSubmission(SubmissionType.AUTO_TIMEOUT);
    }

    private void finalizeExamSubmission(SubmissionType submissionType) {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        User user = SessionManager.getCurrentUser();
        int userId = user != null ? user.getUserId() : 1;
        int attemptId = currentAttempt != null ? currentAttempt.getAttemptId() : 1;

        // Grade attempt
        ExamResult result = examService.gradeAttempt(
                attemptId, userId, currentExam, examQuestions, selectedAnswers, submissionType
        );

        this.examActive = false;

        // Transition to Result Screen
        mainFrame.showExamResult(result, currentExam, examQuestions, selectedAnswers);
    }

    /**
     * Toggles the "Mark for Review" state of the current active question.
     */
    public void toggleFlagCurrentQuestion() {
        if (currentIndex >= 0 && currentIndex < examQuestions.size()) {
            Question q = examQuestions.get(currentIndex);
            if (flaggedQuestions.contains(q.getQuestionId())) {
                flaggedQuestions.remove(q.getQuestionId());
            } else {
                flaggedQuestions.add(q.getQuestionId());
            }
            updateFlagButtonState();
            updatePaletteButtons();
            updateProgressDisplay();
        }
    }

    private void updateFlagButtonState() {
        if (flagBtn == null) return;
        if (currentIndex >= 0 && currentIndex < examQuestions.size()) {
            Question q = examQuestions.get(currentIndex);
            boolean isFlagged = flaggedQuestions.contains(q.getQuestionId());
            if (isFlagged) {
                flagBtn.setText("★ Flagged for Review");
                flagBtn.setBackground(new Color(254, 243, 199));
                flagBtn.setForeground(new Color(180, 83, 9));
            } else {
                flagBtn.setText("🚩 Flag for Review");
                flagBtn.setBackground(null);
                flagBtn.setForeground(AppConstants.COLOR_TEXT_PRIMARY);
            }
        }
    }

    /**
     * Selects an option (A, B, C, D) programmatically or via keyboard shortcut.
     */
    public void selectOption(String optionKey) {
        if (currentIndex >= 0 && currentIndex < examQuestions.size()) {
            Question q = examQuestions.get(currentIndex);
            selectedAnswers.put(q.getQuestionId(), optionKey);

            if ("A".equalsIgnoreCase(optionKey)) optionARadio.setSelected(true);
            else if ("B".equalsIgnoreCase(optionKey)) optionBRadio.setSelected(true);
            else if ("C".equalsIgnoreCase(optionKey)) optionCRadio.setSelected(true);
            else if ("D".equalsIgnoreCase(optionKey)) optionDRadio.setSelected(true);

            if (currentAttempt != null) {
                examService.recordAnswer(currentAttempt.getAttemptId(), q.getQuestionId(), optionKey);
            }
            updatePaletteButtons();
            updateProgressDisplay();
        }
    }

    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, 0), "keySelectA");
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, 0), "keySelectA");
        am.put("keySelectA", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (optionARadio != null && optionARadio.isEnabled()) selectOption("A");
            }
        });

        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, 0), "keySelectB");
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, 0), "keySelectB");
        am.put("keySelectB", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (optionBRadio != null && optionBRadio.isEnabled()) selectOption("B");
            }
        });

        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, 0), "keySelectC");
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, 0), "keySelectC");
        am.put("keySelectC", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (optionCRadio != null && optionCRadio.isEnabled()) selectOption("C");
            }
        });

        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, 0), "keySelectD");
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, 0), "keySelectD");
        am.put("keySelectD", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (optionDRadio != null && optionDRadio.isEnabled()) selectOption("D");
            }
        });

        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), "keyPrev");
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, 0), "keyPrev");
        am.put("keyPrev", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (currentIndex > 0) navigateQuestion(currentIndex - 1);
            }
        });

        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "keyNext");
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, 0), "keyNext");
        am.put("keyNext", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                navigateQuestion(currentIndex + 1);
            }
        });

        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0), "keyFlag");
        am.put("keyFlag", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                toggleFlagCurrentQuestion();
            }
        });
    }

    // Getters for programmatic verification & UI testing
    public Set<Integer> getFlaggedQuestions() {
        return Collections.unmodifiableSet(flaggedQuestions);
    }

    public Map<Integer, String> getSelectedAnswers() {
        return Collections.unmodifiableMap(selectedAnswers);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public boolean isQuestionFlagged(int questionId) {
        return flaggedQuestions.contains(questionId);
    }

    /**
     * Updates the completion progress bar and status text.
     */
    public void updateProgressDisplay() {
        if (examProgressBar == null || examQuestions == null || examQuestions.isEmpty()) return;
        int total = examQuestions.size();
        int answered = selectedAnswers.size();
        int flagged = flaggedQuestions.size();
        int percent = (int) Math.round(((double) answered / total) * 100);

        examProgressBar.setValue(percent);
        examProgressBar.setString(percent + "%");
        String statusText = String.format("Answered: %d of %d (%d%%)", answered, total, percent);
        if (flagged > 0) {
            statusText += String.format(" | ⭐ Flagged: %d", flagged);
        }
        progressStatusLabel.setText(statusText);
    }

    /**
     * Abandons the active exam attempt when window closing or interruption is confirmed.
     */
    public void abandonActiveSession() {
        if (!examActive) return;
        examActive = false;
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        if (currentAttempt != null) {
            examService.abandonAttempt(currentAttempt.getAttemptId());
        }
    }

    public boolean isExamActive() {
        return examActive;
    }

    public JProgressBar getExamProgressBar() {
        return examProgressBar;
    }

    public void setRemainingSeconds(int seconds) {
        this.remainingSeconds = seconds;
        updateTimerDisplay();
    }

    public boolean isWarned5Min() {
        return warned5Min;
    }

    public boolean isWarned1Min() {
        return warned1Min;
    }

    public javax.swing.Timer getCountdownTimer() {
        return countdownTimer;
    }

    public JLabel getTimerBadgeLabel() {
        return timerBadgeLabel;
    }
}

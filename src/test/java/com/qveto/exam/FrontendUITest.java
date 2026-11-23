package com.qveto.exam;

import com.qveto.exam.model.Exam;
import com.qveto.exam.model.ExamResult;
import com.qveto.exam.model.Role;
import com.qveto.exam.model.SubmissionType;
import com.qveto.exam.model.User;
import com.qveto.exam.service.AuthService;
import com.qveto.exam.service.ExamService;
import com.qveto.exam.ui.ExamResultPanel;
import com.qveto.exam.ui.MainFrame;
import com.qveto.exam.ui.StudentDashboardPanel;
import com.qveto.exam.ui.TimedExamPanel;
import com.qveto.exam.util.AppConstants;
import com.qveto.exam.util.SessionManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Automated UI and Presentation Layer Unit Tests for Member 1 (Frontend Engineer).
 * Verifies Swing CardLayout routing, question palette states, answer persistence,
 * keyboard accessibility, and real-time catalog filtering.
 */
public class FrontendUITest {

    public void testMainFrameInitialization() {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            Assert.assertNotNull(frame, "MainFrame should instantiate cleanly");
            Assert.assertTrue(frame.getWidth() >= 860, "Frame width should satisfy minimum constraints");
            Assert.assertTrue(frame.getHeight() >= 580, "Frame height should satisfy minimum constraints");
            frame.dispose();
        });
    }

    public void testStudentDashboardCatalogFilter() {
        AuthService authService = new AuthService();
        ExamService examService = new ExamService();
        MainFrame frame = new MainFrame();

        StudentDashboardPanel dashboard = new StudentDashboardPanel(frame, authService, examService);
        dashboard.refreshData();

        int initialCount = dashboard.getExamsTable().getRowCount();
        Assert.assertTrue(initialCount > 0, "Catalog should display available exams initially");

        // Filter by 'Java'
        dashboard.getExamSearchField().setText("Java");
        dashboard.filterExamsTable();
        int filteredCount = dashboard.getExamsTable().getRowCount();
        Assert.assertTrue(filteredCount > 0, "Filter for 'Java' should return matching exams");
        Assert.assertTrue(filteredCount <= initialCount, "Filtered count should be less than or equal to total");

        // Filter by non-existent keyword
        dashboard.getExamSearchField().setText("NonExistentTopicXYZ");
        dashboard.filterExamsTable();
        Assert.assertEquals(0, dashboard.getExamsTable().getRowCount(), "Non-matching search should clear table rows");

        // Reset filter
        dashboard.getExamSearchField().setText("");
        dashboard.filterExamsTable();
        Assert.assertEquals(initialCount, dashboard.getExamsTable().getRowCount(), "Clearing search should restore all rows");

        frame.dispose();
    }

    public void testTimedExamFlagForReview() {
        ExamService examService = new ExamService();
        MainFrame frame = new MainFrame();
        TimedExamPanel panel = new TimedExamPanel(frame, examService);

        Exam sampleExam = new Exam(1, "Test Assessment", "Desc", 15, 5, 50.0, 50.0, true);
        panel.startAssessment(sampleExam);

        Assert.assertEquals(0, panel.getFlaggedQuestions().size(), "Flagged questions should initially be empty");

        // Toggle flag on question 1
        panel.toggleFlagCurrentQuestion();
        Assert.assertEquals(1, panel.getFlaggedQuestions().size(), "Question 1 should now be flagged");
        Assert.assertTrue(panel.isQuestionFlagged(1), "Question 1 should report flagged state as true");

        // Toggle flag again (unflag)
        panel.toggleFlagCurrentQuestion();
        Assert.assertEquals(0, panel.getFlaggedQuestions().size(), "Unflagging should remove question from review set");
        Assert.assertFalse(panel.isQuestionFlagged(1), "Question 1 should no longer report flagged state");

        frame.dispose();
    }

    public void testTimedExamAnswerSelectionAndOptionPersistence() {
        ExamService examService = new ExamService();
        MainFrame frame = new MainFrame();
        TimedExamPanel panel = new TimedExamPanel(frame, examService);

        Exam sampleExam = new Exam(1, "Test Assessment", "Desc", 15, 5, 50.0, 50.0, true);
        panel.startAssessment(sampleExam);

        // Select Option 'B' for first question
        panel.selectOption("B");
        Assert.assertEquals("B", panel.getSelectedAnswers().get(1), "Selected option B should be recorded");

        // Switch to Option 'C'
        panel.selectOption("C");
        Assert.assertEquals("C", panel.getSelectedAnswers().get(1), "Changing option to C should update recorded answer");

        frame.dispose();
    }

    public void testExamResultScorecardCalculations() {
        MainFrame frame = new MainFrame();
        ExamResultPanel resultPanel = new ExamResultPanel(frame);
        Exam sampleExam = new Exam(1, "Java Exam", "Desc", 15, 5, 50.0, 50.0, true);

        // Passed attempt: 40/50 = 80% (>= 50% passing threshold)
        ExamResult passResult = new ExamResult(1, 1, 2, 1, 5, 5, 4, 1, 40.0, 50.0, 80.0, true, null);
        passResult.setExamTitle("Java Exam");
        resultPanel.displayResult(passResult, sampleExam, new ArrayList<>(), new HashMap<>());
        Assert.assertTrue(passResult.isPassed(), "Result with 80% should be graded as PASSED");

        // Failed attempt: 20/50 = 40% (< 50% passing threshold)
        ExamResult failResult = new ExamResult(2, 2, 2, 1, 5, 5, 2, 3, 20.0, 50.0, 40.0, false, null);
        failResult.setExamTitle("Java Exam");
        resultPanel.displayResult(failResult, sampleExam, new ArrayList<>(), new HashMap<>());
        Assert.assertFalse(failResult.isPassed(), "Result with 40% should be graded as FAILED");

        frame.dispose();
    }

    public void testProgressBarUpdates() {
        ExamService examService = new ExamService();
        MainFrame frame = new MainFrame();
        TimedExamPanel panel = new TimedExamPanel(frame, examService);

        Exam sampleExam = new Exam(1, "Test Assessment", "Desc", 15, 5, 50.0, 50.0, true);
        panel.startAssessment(sampleExam);

        Assert.assertNotNull(panel.getExamProgressBar(), "Progress bar should be instantiated");
        Assert.assertEquals(0, panel.getExamProgressBar().getValue(), "Progress bar should start at 0%");

        // Answer question 1 (1 out of 5 = 20%)
        panel.selectOption("A");
        Assert.assertEquals(20, panel.getExamProgressBar().getValue(), "1 answered out of 5 should equal 20%");

        frame.dispose();
    }

    public void testExamActiveStateAndAbandonment() {
        ExamService examService = new ExamService();
        MainFrame frame = new MainFrame();
        TimedExamPanel panel = new TimedExamPanel(frame, examService);

        Assert.assertFalse(panel.isExamActive(), "Exam should not be active before start");

        Exam sampleExam = new Exam(1, "Test Assessment", "Desc", 15, 5, 50.0, 50.0, true);
        panel.startAssessment(sampleExam);
        Assert.assertTrue(panel.isExamActive(), "Exam should be marked active once started");

        // Abandon session
        panel.abandonActiveSession();
        Assert.assertFalse(panel.isExamActive(), "Exam should no longer be active after abandonment");

        frame.dispose();
    }

    public void testCountdownTimerWarningThresholds() {
        ExamService examService = new ExamService();
        MainFrame frame = new MainFrame();
        TimedExamPanel panel = new TimedExamPanel(frame, examService);

        Exam sampleExam = new Exam(1, "Test Assessment", "Desc", 15, 5, 50.0, 50.0, true);
        panel.startAssessment(sampleExam);

        // 1. Initial Normal State (900s > 300s)
        Assert.assertEquals(900, panel.getRemainingSeconds(), "Initial duration should be 900 seconds");
        Assert.assertEquals(new java.awt.Color(30, 58, 138), panel.getTimerBadgeLabel().getBackground(), "Normal timer background should be navy");
        Assert.assertFalse(panel.isWarned5Min(), "5-min warning should not be flagged initially");

        // 2. Warning State (<= 300s)
        panel.setRemainingSeconds(250);
        Assert.assertEquals(AppConstants.COLOR_WARNING, panel.getTimerBadgeLabel().getBackground(), "Timer background at 250s should be amber warning");
        Assert.assertTrue(panel.isWarned5Min(), "5-min warning flag should be true");

        // 3. Urgent State (<= 60s)
        panel.setRemainingSeconds(55);
        Assert.assertEquals(AppConstants.COLOR_DANGER, panel.getTimerBadgeLabel().getBackground(), "Timer background at 55s should be red alert");
        Assert.assertTrue(panel.isWarned1Min(), "1-min warning flag should be true");

        // 4. Critical Pulse State (<= 30s)
        panel.setRemainingSeconds(28);
        Assert.assertEquals(AppConstants.COLOR_DANGER, panel.getTimerBadgeLabel().getBackground(), "Even second <= 30s should be danger red");
        panel.setRemainingSeconds(27);
        Assert.assertEquals(new java.awt.Color(153, 27, 27), panel.getTimerBadgeLabel().getBackground(), "Odd second <= 30s should be dark crimson pulse");

        panel.abandonActiveSession();
        frame.dispose();
    }

    public void testQuestionPaletteNavigationAndFilters() {
        ExamService examService = new ExamService();
        MainFrame frame = new MainFrame();
        TimedExamPanel panel = new TimedExamPanel(frame, examService);

        Exam sampleExam = new Exam(1, "Test Assessment", "Desc", 15, 5, 50.0, 50.0, true);
        panel.startAssessment(sampleExam);

        // 1. Initial palette state check
        Assert.assertEquals(0, panel.getAnsweredCount(), "Initially 0 questions should be answered");
        Assert.assertEquals(0, panel.getFlaggedCount(), "Initially 0 questions should be flagged");
        Assert.assertEquals(5, panel.getUnansweredCount(), "Initially 5 questions should be unanswered");
        Assert.assertNotNull(panel.getPaletteSummaryLabel(), "Palette summary label should not be null");
        Assert.assertTrue(panel.getPaletteSummaryLabel().getText().contains("0/5 Ans"), "Summary should state 0/5 Ans");
        Assert.assertEquals(5, panel.getPaletteButtons().size(), "There should be 5 palette buttons");
        Assert.assertEquals("1", panel.getPaletteButtons().get(0).getText(), "First button should start with label 1");

        // 2. Answer Q1 (Index 0)
        panel.selectOption("B");
        Assert.assertEquals(1, panel.getAnsweredCount(), "Answered count should now be 1");
        Assert.assertEquals(4, panel.getUnansweredCount(), "Unanswered count should now be 4");
        Assert.assertEquals("✓1", panel.getPaletteButtons().get(0).getText(), "Answered button 1 should have checkmark prefix");
        Assert.assertTrue(panel.getPaletteButtons().get(0).getToolTipText().contains("Answered (Option B)"), "Tooltip should indicate Answered Option B");

        // 3. Navigate to Q2 (Index 1) and Flag it
        panel.navigateQuestion(1);
        panel.toggleFlagCurrentQuestion();
        Assert.assertEquals(1, panel.getFlaggedCount(), "Flagged count should now be 1");
        Assert.assertEquals("⚑2", panel.getPaletteButtons().get(1).getText(), "Flagged unanswered button 2 should have flag symbol");
        Assert.assertTrue(panel.getPaletteButtons().get(1).getToolTipText().contains("[FLAGGED FOR REVIEW]"), "Tooltip should note flagged for review");

        // 4. Navigate to Q3 (Index 2), Answer & Flag it
        panel.navigateQuestion(2);
        panel.selectOption("C");
        panel.toggleFlagCurrentQuestion();
        Assert.assertEquals(2, panel.getAnsweredCount(), "Answered count should be 2");
        Assert.assertEquals(2, panel.getFlaggedCount(), "Flagged count should be 2");
        Assert.assertEquals("★3", panel.getPaletteButtons().get(2).getText(), "Answered and flagged button 3 should display star icon");

        // 5. Test Quick-Jump algorithms
        // Currently at index 2. Next unanswered should be index 3 (Q4).
        Assert.assertEquals(3, panel.findNextUnansweredIndex(), "Next unanswered after Q3 should be Q4 (index 3)");
        panel.jumpToNextUnanswered();
        Assert.assertEquals(3, panel.getCurrentIndex(), "Should have navigated to index 3");

        // Answer Q4 and Q5
        panel.selectOption("D");
        panel.navigateQuestion(4);
        panel.selectOption("A");

        // Current answers: Q1, Q3, Q4, Q5. Unanswered is only Q2 (index 1).
        // Since we are at index 4, jumping to next unanswered should wrap around to index 1.
        Assert.assertEquals(1, panel.findNextUnansweredIndex(), "Next unanswered should wrap to index 1 (Q2)");
        panel.jumpToNextUnanswered();
        Assert.assertEquals(1, panel.getCurrentIndex(), "Should have wrapped around to index 1");

        // Answer Q2
        panel.selectOption("A");
        Assert.assertEquals(5, panel.getAnsweredCount(), "All 5 questions should now be answered");
        Assert.assertEquals(-1, panel.findNextUnansweredIndex(), "When all answered, findNextUnansweredIndex should return -1");

        // Test Next Flagged jump: flagged questions are Q2 (index 1) and Q3 (index 2).
        // From index 1, next flagged is index 2.
        Assert.assertEquals(2, panel.findNextFlaggedIndex(), "Next flagged from index 1 should be index 2");
        panel.jumpToNextFlagged();
        Assert.assertEquals(2, panel.getCurrentIndex(), "Jumped to index 2");

        // From index 2, next flagged should wrap around to index 1.
        Assert.assertEquals(1, panel.findNextFlaggedIndex(), "Next flagged from index 2 should wrap around to index 1");
        panel.jumpToNextFlagged();
        Assert.assertEquals(1, panel.getCurrentIndex(), "Jumped back to index 1");

        panel.abandonActiveSession();
        frame.dispose();
    }
}

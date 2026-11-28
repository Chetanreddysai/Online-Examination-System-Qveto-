package com.qveto.exam;

import com.qveto.exam.model.*;
import com.qveto.exam.service.AuthService;
import com.qveto.exam.service.ExamService;
import com.qveto.exam.ui.ExamResultPanel;
import com.qveto.exam.ui.MainFrame;
import com.qveto.exam.ui.StudentDashboardPanel;
import com.qveto.exam.ui.TimedExamPanel;
import com.qveto.exam.util.AppConstants;
import com.qveto.exam.util.SessionManager;

import javax.swing.*;
import java.util.*;

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

    public void testContinuousAnswerPersistenceAcrossNavigation() {
        ExamService examService = new ExamService();
        MainFrame frame = new MainFrame();
        TimedExamPanel panel = new TimedExamPanel(frame, examService);

        Exam sampleExam = new Exam(1, "Test Assessment", "Desc", 15, 5, 50.0, 50.0, true);
        panel.startAssessment(sampleExam);

        int attemptId = panel.getCurrentAttempt().getAttemptId();

        // Initially on Q1 (index 0). No option selected yet.
        Assert.assertNotNull(panel.getAutosaveBadgeLabel(), "Autosave badge label should exist");
        Assert.assertTrue(panel.getAutosaveBadgeLabel().getText().contains("Cleared")
                || panel.getAutosaveBadgeLabel().getText().contains("Not Answered"),
                "Initial autosave badge should indicate no answer");

        // 1. Select Option "B" on Q1 (Question ID 1)
        panel.selectOption("B");
        Assert.assertEquals("B", panel.getSelectedAnswers().get(1), "Selected answer for Q1 should be B");
        Assert.assertEquals("● Saved", panel.getAutosaveBadgeLabel().getText(), "Autosave badge should say Saved");
        Assert.assertEquals("B", examService.getAnswersForAttempt(attemptId).get(1), "Persistence store should reflect answer B");

        // 2. Navigate forward to Q2 (index 1)
        panel.navigateQuestion(1);
        Assert.assertEquals(1, panel.getCurrentIndex(), "Should now be on index 1");
        Assert.assertTrue(panel.getAutosaveBadgeLabel().getText().contains("Cleared")
                || panel.getAutosaveBadgeLabel().getText().contains("Not Answered"),
                "Q2 should not be answered yet");

        // 3. Select Option "D" on Q2 (Question ID 2)
        panel.selectOption("D");
        Assert.assertEquals("D", panel.getSelectedAnswers().get(2), "Selected answer for Q2 should be D");
        Assert.assertEquals("● Saved", panel.getAutosaveBadgeLabel().getText(), "Autosave badge on Q2 should say Saved");

        // 4. Jump via palette to Q4 (index 3, Question ID 4)
        panel.navigateQuestion(3);
        panel.selectOption("A");
        Assert.assertEquals("A", panel.getSelectedAnswers().get(4), "Selected answer for Q4 should be A");

        // 5. Navigate BACK to Q1 (index 0) - test answer restoration
        panel.navigateQuestion(0);
        Assert.assertEquals("B", panel.getSelectedAnswers().get(1), "Q1 selection must persist across navigation");
        Assert.assertEquals("● Saved", panel.getAutosaveBadgeLabel().getText(), "Autosave badge should show Saved on return to Q1");

        // 6. Test Clear Answer on Q1
        panel.clearSelectedAnswer();
        Assert.assertNull(panel.getSelectedAnswers().get(1), "Answer for Q1 should be removed from selectedAnswers");
        Assert.assertEquals("○ Cleared", panel.getAutosaveBadgeLabel().getText(), "Autosave badge should show Cleared");
        Assert.assertNull(examService.getAnswersForAttempt(attemptId).get(1), "Persistence store should clear answer for Q1");

        // 7. Navigate away and back to verify cleared state remains intact
        panel.navigateQuestion(1);
        Assert.assertEquals("D", panel.getSelectedAnswers().get(2), "Q2 answer D must still be intact");
        panel.navigateQuestion(0);
        Assert.assertNull(panel.getSelectedAnswers().get(1), "Q1 must still remain cleared after navigating away and back");

        panel.abandonActiveSession();
        frame.dispose();
    }

    public void testSessionRecoveryAndBulkAnswerRestoration() {
        ExamService examService = new ExamService();
        MainFrame frame = new MainFrame();
        TimedExamPanel panel = new TimedExamPanel(frame, examService);

        Exam sampleExam = new Exam(1, "Test Assessment", "Desc", 15, 5, 50.0, 50.0, true);

        // Pre-create an attempt session and persist answers for Q1 ("C") and Q3 ("A")
        com.qveto.exam.model.ExamAttempt restoredAttempt = examService.startAttempt(1, 1);
        examService.recordAnswer(restoredAttempt.getAttemptId(), 1, "C");
        examService.recordAnswer(restoredAttempt.getAttemptId(), 3, "A");

        // Launch assessment with existing attempt
        panel.startAssessment(sampleExam, restoredAttempt);

        // Verify restored session state
        Assert.assertEquals(2, panel.getAnsweredCount(), "Restored session should have 2 answered questions");
        Assert.assertEquals("C", panel.getSelectedAnswers().get(1), "Q1 should be restored with answer C");
        Assert.assertEquals("A", panel.getSelectedAnswers().get(3), "Q3 should be restored with answer A");
        Assert.assertEquals("✓1", panel.getPaletteButtons().get(0).getText(), "Palette button 1 should have checkmark");
        Assert.assertEquals("✓3", panel.getPaletteButtons().get(2).getText(), "Palette button 3 should have checkmark");
        Assert.assertEquals(40, panel.getExamProgressBar().getValue(), "Progress bar should show 40% (2/5)");
        Assert.assertTrue(panel.getPaletteSummaryLabel().getText().contains("2/5 Ans"), "Summary should show 2/5 Ans");

        // On initial Q1 load, autosave badge should indicate Saved
        Assert.assertEquals("● Saved", panel.getAutosaveBadgeLabel().getText(), "Autosave badge should reflect restored saved choice");

        panel.abandonActiveSession();
        frame.dispose();
    }

    public void testSubmissionControlsAndTimeoutLockdown() {
        ExamService examService = new ExamService();
        MainFrame frame = new MainFrame();
        TimedExamPanel panel = new TimedExamPanel(frame, examService);

        Exam sampleExam = new Exam(1, "Test Assessment", "Desc", 15, 5, 50.0, 50.0, true);
        panel.startAssessment(sampleExam);

        // 1. Initial State before submission
        Assert.assertFalse(panel.isSubmissionLocked(), "Assessment must start in unlocked state");
        Assert.assertTrue(panel.isExamActive(), "Assessment must be active initially");
        Assert.assertTrue(panel.getOptionARadio().isEnabled(), "Radio options must be enabled initially");
        Assert.assertTrue(panel.getSubmitBtn().isEnabled(), "Submit button must be enabled initially");
        Assert.assertTrue(panel.getPaletteButtons().get(0).isEnabled(), "Palette buttons must be enabled initially");

        // 2. Select Option on Q1 (index 0) and flag Q2 (index 1)
        panel.selectOption("B");
        panel.navigateQuestion(1);
        panel.toggleFlagCurrentQuestion();
        Assert.assertEquals("B", panel.getSelectedAnswers().get(1), "Q1 should have choice B");
        Assert.assertTrue(panel.isQuestionFlagged(2), "Q2 should be flagged");

        // 3. Trigger Auto-Timeout Submission Lockdown
        panel.handleAutoTimeoutSubmission(false);

        // 4. Verify Immediate Lockdown
        Assert.assertTrue(panel.isSubmissionLocked(), "Assessment must be locked after auto-timeout");
        Assert.assertFalse(panel.isExamActive(), "Assessment must no longer be active");
        Assert.assertEquals(SubmissionType.AUTO_TIMEOUT, panel.getLastSubmissionType(), "Submission type should be AUTO_TIMEOUT");
        Assert.assertNotNull(panel.getLastGradedResult(), "Graded exam result must be produced");
        Assert.assertEquals(1, panel.getLastGradedResult().getAnsweredCount(), "Result should record 1 answered question");

        // Verify all interactive UI controls are disabled
        Assert.assertFalse(panel.getOptionARadio().isEnabled(), "Option A radio must be disabled upon lockdown");
        Assert.assertFalse(panel.getOptionBRadio().isEnabled(), "Option B radio must be disabled upon lockdown");
        Assert.assertFalse(panel.getOptionCRadio().isEnabled(), "Option C radio must be disabled upon lockdown");
        Assert.assertFalse(panel.getOptionDRadio().isEnabled(), "Option D radio must be disabled upon lockdown");
        Assert.assertFalse(panel.getSubmitBtn().isEnabled(), "Submit button must be disabled upon lockdown");
        Assert.assertFalse(panel.getPrevBtn().isEnabled(), "Previous button must be disabled upon lockdown");
        Assert.assertFalse(panel.getNextBtn().isEnabled(), "Next button must be disabled upon lockdown");
        Assert.assertFalse(panel.getClearBtn().isEnabled(), "Clear button must be disabled upon lockdown");
        Assert.assertFalse(panel.getFlagBtn().isEnabled(), "Flag button must be disabled upon lockdown");
        Assert.assertFalse(panel.getJumpUnansweredBtn().isEnabled(), "Jump unanswered button must be disabled upon lockdown");
        Assert.assertFalse(panel.getJumpFlaggedBtn().isEnabled(), "Jump flagged button must be disabled upon lockdown");

        for (JButton palBtn : panel.getPaletteButtons()) {
            Assert.assertFalse(palBtn.isEnabled(), "Palette button must be disabled upon lockdown");
        }

        // Verify timer and autosave visual indicators
        Assert.assertTrue(panel.getTimerBadgeLabel().getText().contains("00:00"), "Timer badge should indicate 00:00 expiry");
        Assert.assertTrue(panel.getAutosaveBadgeLabel().getText().contains("Locked"), "Autosave badge should indicate Locked");

        // 5. Verify Tamper-Proofing (no post-submission mutations allowed)
        panel.selectOption("C");
        Assert.assertFalse("C".equals(panel.getSelectedAnswers().get(2)), "Option selection after lock must be rejected");

        panel.clearSelectedAnswer();
        Assert.assertEquals("B", panel.getSelectedAnswers().get(1), "Clearing answer after lock must be rejected");

        panel.toggleFlagCurrentQuestion();
        Assert.assertTrue(panel.isQuestionFlagged(2), "Toggling flag after lock must be rejected");

        int curIdx = panel.getCurrentIndex();
        panel.navigateQuestion(3);
        Assert.assertEquals(curIdx, panel.getCurrentIndex(), "Navigation after lock must be rejected");

        frame.dispose();
    }

    public void testManualSubmissionControlsAndReLockPrevention() {
        ExamService examService = new ExamService();
        MainFrame frame = new MainFrame();
        TimedExamPanel panel = new TimedExamPanel(frame, examService);

        Exam sampleExam = new Exam(1, "Test Assessment", "Desc", 15, 5, 50.0, 50.0, true);
        panel.startAssessment(sampleExam);

        // Answer Q1 with "C" (which is correct)
        panel.selectOption("C");

        // Submit via manual submission
        panel.submitAssessment(SubmissionType.MANUAL);

        Assert.assertTrue(panel.isSubmissionLocked(), "Assessment should be locked after manual submission");
        Assert.assertEquals(SubmissionType.MANUAL, panel.getLastSubmissionType(), "Last submission type should be MANUAL");
        Assert.assertNotNull(panel.getLastGradedResult(), "Graded exam result should be generated");
        Assert.assertEquals(1, panel.getLastGradedResult().getAnsweredCount(), "1 question answered");
        Assert.assertTrue(panel.getAutosaveBadgeLabel().getText().contains("Submitted"), "Autosave badge should show Submitted");

        // Test Unlock / New Session reset
        panel.unlockAllControls();
        Assert.assertFalse(panel.isSubmissionLocked(), "Controls should be unlocked after unlockAllControls");
        Assert.assertTrue(panel.getSubmitBtn().isEnabled(), "Submit button should be re-enabled");

        frame.dispose();
    }

    public void testExamResultScorecardUIDeckAndOutcomeBanners() {
        MainFrame frame = new MainFrame();
        ExamResultPanel resultPanel = new ExamResultPanel(frame);
        Exam sampleExam = new Exam(1, "Fullstack Java Certification", "Comprehensive evaluation", 60, 3, 30.0, 50.0, true);

        List<Question> questions = new ArrayList<>();
        questions.add(new Question(101, "Java", "Which collection is synchronized?", "ArrayList", "Vector", "LinkedList", "HashSet", "B", 10.0));
        questions.add(new Question(102, "Java", "What is default value of boolean?", "true", "false", "null", "0", "B", 10.0));
        questions.add(new Question(103, "Java", "Which keyword prevents inheritance?", "static", "abstract", "final", "const", "C", 10.0));

        // 1. Test PASSED Scorecard Scenario (20/30 = 66.7% >= 50%)
        Map<Integer, String> passedAnswers = new HashMap<>();
        passedAnswers.put(101, "B"); // Correct (+10)
        passedAnswers.put(102, "A"); // Incorrect (0)
        // 103 is skipped (0)
        ExamResult passResult = new ExamResult(10, 501, 2, 1, 3, 2, 1, 1, 10.0, 30.0, 66.7, true, new java.sql.Timestamp(System.currentTimeMillis()));
        passResult.setScore(20.0);
        passResult.setPercentage(66.7);
        passResult.setPassed(true);

        resultPanel.displayResult(passResult, sampleExam, questions, passedAnswers);

        Assert.assertNotNull(resultPanel.getStatusBannerLabel(), "Status banner should exist");
        Assert.assertTrue(resultPanel.getStatusBannerLabel().getText().contains("PASSED"), "Status banner must celebrate PASSED");
        Assert.assertEquals("PASSED", resultPanel.getOutcomeBadgeLabel().getText(), "Outcome badge should state PASSED");
        Assert.assertEquals(new java.awt.Color(16, 185, 129), resultPanel.getOutcomeBadgeLabel().getForeground(), "Outcome badge should be green");
        Assert.assertTrue(resultPanel.getScoreMetricLabel().getText().contains("20.0"), "Score metric should display 20.0");
        Assert.assertTrue(resultPanel.getPercentageMetricLabel().getText().contains("66.7%"), "Percentage metric should display 66.7%");
        Assert.assertEquals(67, resultPanel.getPerformanceProgressBar().getValue(), "Progress bar should round to 67%");
        Assert.assertTrue(resultPanel.getCandidateMetaLabel().getText().contains("Attempt #501"), "Candidate meta bar must include attempt ID");

        // Verify Review Table outcomes (Correct, Incorrect, Skipped)
        Assert.assertEquals(3, resultPanel.getReviewTableModel().getRowCount(), "Table must have 3 question rows");
        Assert.assertEquals("✔ Correct", resultPanel.getReviewTableModel().getValueAt(0, 4), "Q1 should be graded as Correct");
        Assert.assertEquals("✘ Incorrect", resultPanel.getReviewTableModel().getValueAt(1, 4), "Q2 should be graded as Incorrect");
        Assert.assertEquals("○ Skipped", resultPanel.getReviewTableModel().getValueAt(2, 4), "Q3 should be graded as Skipped");

        // 2. Test FAILED Scorecard Scenario (0/30 = 0% < 50%)
        ExamResult failResult = new ExamResult(11, 502, 2, 1, 3, 1, 0, 1, 0.0, 30.0, 0.0, false, new java.sql.Timestamp(System.currentTimeMillis()));
        resultPanel.displayResult(failResult, sampleExam, questions, new HashMap<>());

        Assert.assertTrue(resultPanel.getStatusBannerLabel().getText().contains("NOT MET"), "Status banner must indicate criteria NOT MET");
        Assert.assertEquals("FAILED", resultPanel.getOutcomeBadgeLabel().getText(), "Outcome badge should state FAILED");
        Assert.assertEquals(AppConstants.COLOR_DANGER, resultPanel.getOutcomeBadgeLabel().getForeground(), "Outcome badge should be red");
        Assert.assertEquals(0, resultPanel.getPerformanceProgressBar().getValue(), "Progress bar should show 0%");

        // 3. Test references preservation
        Assert.assertEquals(failResult, resultPanel.getLastResult(), "Last result reference must be stored");
        Assert.assertEquals(sampleExam, resultPanel.getLastExam(), "Last exam reference must be stored");

        frame.dispose();
    }

    public void testQuestionReviewTableOutcomeFiltering() {
        MainFrame frame = new MainFrame();
        ExamResultPanel resultPanel = new ExamResultPanel(frame);
        Exam sampleExam = new Exam(1, "Advanced Java & Streams", "Evaluation", 45, 4, 40.0, 50.0, true);

        List<Question> questions = new ArrayList<>();
        questions.add(new Question(201, "Java", "Which interface does List implement?", "Collection", "Map", "Set", "Queue", "A", 10.0));
        questions.add(new Question(202, "Java", "Which method starts a thread?", "run()", "start()", "init()", "execute()", "B", 10.0));
        questions.add(new Question(203, "Java", "What is size of char in Java?", "8-bit", "16-bit", "32-bit", "64-bit", "B", 10.0));
        questions.add(new Question(204, "Java", "Is String immutable in Java?", "No", "Yes", "Depends", "None", "B", 10.0));

        Map<Integer, String> selected = new HashMap<>();
        selected.put(201, "A"); // Correct (+10)
        selected.put(202, "A"); // Incorrect (User 'A', Key 'B')
        // 203 skipped
        selected.put(204, "B"); // Correct (+10)

        ExamResult result = new ExamResult(15, 601, 2, 1, 4, 3, 2, 1, 20.0, 40.0, 50.0, true, new java.sql.Timestamp(System.currentTimeMillis()));
        resultPanel.displayResult(result, sampleExam, questions, selected);

        // 1. Initial State: All 4 rows displayed
        Assert.assertEquals(4, resultPanel.getReviewTableModel().getRowCount(), "Initial review table should contain all 4 questions");
        Assert.assertTrue(resultPanel.getFilterStatusLabel().getText().contains("4 of 4"), "Status label should report 4 of 4 questions");

        // 2. Filter: Correct Only
        resultPanel.getFilterComboBox().setSelectedIndex(1);
        Assert.assertEquals(2, resultPanel.getReviewTableModel().getRowCount(), "Filter 'Correct Only' should display exactly 2 rows");
        Assert.assertEquals("✔ Correct", resultPanel.getReviewTableModel().getValueAt(0, 4), "Filtered row 1 outcome should be Correct");
        Assert.assertEquals("✔ Correct", resultPanel.getReviewTableModel().getValueAt(1, 4), "Filtered row 2 outcome should be Correct");

        // 3. Filter: Incorrect Only
        resultPanel.getFilterComboBox().setSelectedIndex(2);
        Assert.assertEquals(1, resultPanel.getReviewTableModel().getRowCount(), "Filter 'Incorrect Only' should display exactly 1 row");
        Assert.assertEquals("✘ Incorrect", resultPanel.getReviewTableModel().getValueAt(0, 4), "Filtered row outcome should be Incorrect");
        Assert.assertTrue(resultPanel.getReviewTableModel().getValueAt(0, 1).toString().contains("starts a thread"), "Row should be Question 202");

        // 4. Filter: Skipped Only
        resultPanel.getFilterComboBox().setSelectedIndex(3);
        Assert.assertEquals(1, resultPanel.getReviewTableModel().getRowCount(), "Filter 'Skipped Only' should display exactly 1 row");
        Assert.assertEquals("○ Skipped", resultPanel.getReviewTableModel().getValueAt(0, 4), "Filtered row outcome should be Skipped");
        Assert.assertTrue(resultPanel.getReviewTableModel().getValueAt(0, 1).toString().contains("size of char"), "Row should be Question 203");

        // 5. Keyword search filter
        resultPanel.getFilterComboBox().setSelectedIndex(0); // Reset to All
        Assert.assertEquals(4, resultPanel.getReviewTableModel().getRowCount(), "Reset to All should display 4 rows");

        resultPanel.getSearchField().setText("immutable");
        Assert.assertEquals(1, resultPanel.getReviewTableModel().getRowCount(), "Search 'immutable' should return 1 matching question");
        Assert.assertTrue(resultPanel.getReviewTableModel().getValueAt(0, 1).toString().contains("String immutable"), "Should find Question 204");

        resultPanel.getSearchField().setText("nonExistentKeyword123");
        Assert.assertEquals(0, resultPanel.getReviewTableModel().getRowCount(), "Search non-existent should clear all rows");

        resultPanel.getSearchField().setText("");
        Assert.assertEquals(4, resultPanel.getReviewTableModel().getRowCount(), "Clearing search field should restore all 4 questions");

        frame.dispose();
    }

    public void testAnswerKeyProtectionAndAntiLeakSafeguards() {
        MainFrame frame = new MainFrame();
        ExamResultPanel resultPanel = new ExamResultPanel(frame);
        Exam sampleExam = new Exam(1, "Secure Assessment", "Evaluation", 30, 2, 20.0, 50.0, true);

        List<Question> questions = new ArrayList<>();
        questions.add(new Question(301, "Security", "What does TLS provide?", "Confidentiality", "Noise", "Lag", "None", "A", 10.0));
        questions.add(new Question(302, "Security", "Which is a hashing algorithm?", "AES", "SHA-256", "RSA", "DES", "B", 10.0));

        Map<Integer, String> selected = new HashMap<>();
        selected.put(301, "A"); // Correct
        selected.put(302, "C"); // Incorrect

        ExamResult result = new ExamResult(16, 701, 2, 1, 2, 2, 1, 1, 10.0, 20.0, 50.0, true, new java.sql.Timestamp(System.currentTimeMillis()));

        // 1. Test Protected Mode (allowAnswerKeyReveal = false)
        resultPanel.displayResult(result, sampleExam, questions, selected, false);

        Assert.assertFalse(resultPanel.isAnswerKeysRevealed(), "Answer keys should be marked protected");
        Assert.assertTrue(resultPanel.getSecurityBadgeLabel().getText().contains("Protected"), "Security badge should warn Answer Keys Protected");

        // Correct answer column must be masked as Protected (Hidden) - NEVER leak "Option A" or "Option B"
        String col3Row0 = resultPanel.getReviewTableModel().getValueAt(0, 3).toString();
        String col3Row1 = resultPanel.getReviewTableModel().getValueAt(1, 3).toString();
        Assert.assertTrue(col3Row0.contains("Protected"), "Correct answer column must mask key in Row 0");
        Assert.assertTrue(col3Row1.contains("Protected"), "Correct answer column must mask key in Row 1");
        Assert.assertFalse(col3Row0.contains("Option A"), "Column 3 MUST NOT leak 'Option A' when protected");
        Assert.assertFalse(col3Row1.contains("Option B"), "Column 3 MUST NOT leak 'Option B' when protected");

        // Inspection details text must safeguard the key
        String inspectDetails = resultPanel.getQuestionInspectionDetails(0);
        Assert.assertTrue(inspectDetails.contains("Protected"), "Inspection text must state answer key is protected");
        Assert.assertFalse(inspectDetails.contains("Correct Answer: Option A"), "Inspection text MUST NOT leak authoritative key when sealed");

        // 2. Test Unsealed Mode (allowAnswerKeyReveal = true)
        resultPanel.setAnswerKeysRevealed(true);
        Assert.assertTrue(resultPanel.isAnswerKeysRevealed(), "Answer keys should now be unsealed");
        Assert.assertTrue(resultPanel.getSecurityBadgeLabel().getText().contains("Released"), "Security badge should state Answer Keys Released");

        String unsealedCol3Row0 = resultPanel.getReviewTableModel().getValueAt(0, 3).toString();
        String unsealedCol3Row1 = resultPanel.getReviewTableModel().getValueAt(1, 3).toString();
        Assert.assertEquals("Option A", unsealedCol3Row0, "Unsealed row 0 should display Option A");
        Assert.assertEquals("Option B", unsealedCol3Row1, "Unsealed row 1 should display Option B");

        String unsealedInspect = resultPanel.getQuestionInspectionDetails(0);
        Assert.assertTrue(unsealedInspect.contains("Option A"), "Unsealed inspection should now present Option A");
        Assert.assertTrue(unsealedInspect.contains("Accurate!"), "Unsealed rationale should provide pedagogical reinforcement");

        frame.dispose();
    }
}

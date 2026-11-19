package com.qveto.exam;

import com.qveto.exam.model.ExamResult;
import com.qveto.exam.service.ExamService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

import static com.qveto.exam.Assert.*;

public class ResultReportingTest {

    private ExamService examService;

    public void setUp() {
        examService = new ExamService();
    }

    public void testGetAllCandidateResults() {
        setUp();
        List<ExamResult> results = examService.getAllCandidateResults();
        assertNotNull(results, "Candidate results must not be null");
        assertFalse(results.isEmpty(), "Candidate results should contain seeded demo records");

        ExamResult first = results.get(0);
        assertTrue(first.getResultId() > 0);
        assertTrue(first.getTotalQuestions() > 0);
    }

    public void testGetStudentHistory() {
        setUp();
        // User 2 (Aarav Patel) has a seeded attempt
        List<ExamResult> history = examService.getStudentHistory(2);
        assertNotNull(history);
        assertFalse(history.isEmpty(), "Student 2 must have assessment history");

        for (ExamResult r : history) {
            assertEquals(2, r.getUserId(), "History must be strictly filtered to the requested student ID");
        }
    }

    public void testAnswersForAttemptRetrieval() {
        setUp();
        Map<Integer, String> answers = examService.getAnswersForAttempt(1);
        assertNotNull(answers, "Attempt answers must not be null");
        assertFalse(answers.isEmpty(), "Attempt 1 must contain answers");
        assertTrue(answers.containsKey(1), "Question 1 must be answered in Attempt 1");
    }

    public void testExportResultsToCSV() throws Exception {
        setUp();
        List<ExamResult> results = examService.getAllCandidateResults();
        File tempFile = File.createTempFile("test_results_export", ".csv");
        tempFile.deleteOnExit();

        boolean exported = examService.exportResultsToCSV(tempFile, results);
        assertTrue(exported, "CSV export operation must return true");
        assertTrue(tempFile.exists(), "CSV file must exist on disk");
        assertTrue(tempFile.length() > 0, "CSV file must not be empty");

        // Verify CSV header
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String header = reader.readLine();
            assertNotNull(header);
            assertTrue(header.contains("Result ID"));
            assertTrue(header.contains("Candidate Name"));
            assertTrue(header.contains("Score"));
            assertTrue(header.contains("Outcome"));

            String firstRow = reader.readLine();
            assertNotNull(firstRow, "Data row must be present in exported CSV");
        }
    }

    public void testPassRateCalculation() {
        setUp();
        List<ExamResult> results = examService.getAllCandidateResults();
        int passed = 0;
        for (ExamResult r : results) {
            if (r.isPassed()) passed++;
        }
        double rate = ((double) passed / results.size()) * 100.0;
        assertTrue(rate >= 0.0 && rate <= 100.0, "Pass rate must fall within 0 - 100%");
    }
}

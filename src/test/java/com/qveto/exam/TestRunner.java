package com.qveto.exam;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight Standalone Test Runner to execute unit tests without external test frameworks.
 */
public class TestRunner {

    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("   Running Qveto Online Examination System Unit Tests   ");
        System.out.println("========================================================");

        List<Class<?>> testClasses = new ArrayList<>();
        testClasses.add(PasswordUtilTest.class);
        testClasses.add(InputValidatorTest.class);
        testClasses.add(ModelTest.class);
        testClasses.add(DAOTest.class);
        testClasses.add(AuthServiceTest.class);
        testClasses.add(ExamServiceTest.class);
        testClasses.add(TimedExamWorkflowTest.class);
        testClasses.add(ResultReportingTest.class);

        int total = 0;
        int passed = 0;
        int failed = 0;

        for (Class<?> testClass : testClasses) {
            System.out.println("\n[SUITE] " + testClass.getSimpleName());
            Method[] methods = testClass.getDeclaredMethods();

            for (Method method : methods) {
                if (method.getName().startsWith("test")) {
                    total++;
                    try {
                        Object instance = testClass.getDeclaredConstructor().newInstance();
                        method.invoke(instance);
                        System.out.println("  ✔ PASS: " + method.getName());
                        passed++;
                    } catch (Throwable t) {
                        System.err.println("  ✘ FAIL: " + method.getName());
                        Throwable cause = t.getCause() != null ? t.getCause() : t;
                        System.err.println("    Reason: " + cause.getMessage());
                        failed++;
                    }
                }
            }
        }

        System.out.println("\n--------------------------------------------------------");
        System.out.println("Execution Summary:");
        System.out.println("  Total Tests:  " + total);
        System.out.println("  Passed Tests: " + passed);
        System.out.println("  Failed Tests: " + failed);
        System.out.println("========================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}

-- ========================================================
-- Qveto Online Examination System (Project 3)
-- Synthetic Seed Data
-- ========================================================

USE online_exam_db;

-- --------------------------------------------------------
-- 1. Seed Users (Admin & Students)
-- Passwords:
-- Admin:   Admin@123   -> e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7
-- Student: Student@123 -> b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54
-- --------------------------------------------------------
INSERT INTO users (user_id, full_name, email, password_hash, role, phone)
VALUES
(1, 'System Administrator', 'admin@qveto.com', 'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7', 'ADMIN', '+91 98765 00001'),
(2, 'Aarav Patel', 'student1@qveto.com', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'STUDENT', '+91 98765 00002'),
(3, 'Diya Sharma', 'student2@qveto.com', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'STUDENT', '+91 98765 00003')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- --------------------------------------------------------
-- 2. Seed Reusable Question Bank
-- --------------------------------------------------------
INSERT INTO questions (question_id, subject, question_text, option_a, option_b, option_c, option_d, correct_option, marks)
VALUES
-- Java Questions
(1, 'Java Programming', 'Which of the following is NOT a Java primitive data type?', 'int', 'boolean', 'String', 'char', 'C', 10.00),
(2, 'Java Programming', 'Which keyword is used to prevent method overriding in Java?', 'static', 'final', 'abstract', 'protected', 'B', 10.00),
(3, 'Java Programming', 'What is the default layout manager for a Swing JFrame content pane?', 'FlowLayout', 'BorderLayout', 'GridLayout', 'CardLayout', 'B', 10.00),
(4, 'Java Programming', 'Which interface must a class implement to be executed as a background thread?', 'Runnable', 'Comparable', 'Serializable', 'Cloneable', 'A', 10.00),
(5, 'Java Programming', 'Which package contains the Java Swing components like JFrame and JButton?', 'java.awt', 'javax.swing', 'java.util', 'java.lang', 'B', 10.00),

-- Database & SQL Questions
(6, 'Database Systems', 'Which SQL clause is used to filter records resulting from a GROUP BY statement?', 'WHERE', 'ORDER BY', 'HAVING', 'JOIN', 'C', 10.00),
(7, 'Database Systems', 'Which JDBC interface is used to execute parameterized SQL queries securely?', 'Statement', 'PreparedStatement', 'CallableStatement', 'ResultSet', 'B', 10.00),
(8, 'Database Systems', 'What type of relationship exists between an Exam and Question when a question can belong to multiple exams?', 'One-to-One', 'One-to-Many', 'Many-to-Many', 'Self-Referencing', 'C', 10.00),
(9, 'Database Systems', 'Which constraint ensures that all values in a database column are unique?', 'PRIMARY KEY', 'NOT NULL', 'UNIQUE', 'FOREIGN KEY', 'C', 10.00),
(10, 'Database Systems', 'What does ACID stand for in database transaction management?', 'Atomicity, Consistency, Isolation, Durability', 'Accuracy, Concurrency, Integrity, Durability', 'Atomicity, Compatibility, Isolation, Dependency', 'Access, Consistency, Indexing, Data', 'A', 10.00)
ON DUPLICATE KEY UPDATE question_text = VALUES(question_text);

-- --------------------------------------------------------
-- 3. Seed Exams
-- --------------------------------------------------------
INSERT INTO exams (exam_id, title, description, duration_minutes, total_questions, total_marks, passing_percentage, is_active)
VALUES
(1, 'Java Core & Swing Fundamentals', 'Timed assessment evaluating core Java concepts, OOP, multithreading, and Swing GUI architecture.', 15, 5, 50.00, 50.00, TRUE),
(2, 'Database Management Systems & SQL', 'Comprehensive assessment covering relational databases, normalization, SQL queries, and JDBC transactions.', 20, 5, 50.00, 50.00, TRUE)
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- --------------------------------------------------------
-- 4. Map Questions to Exams (Question Bank Reuse)
-- --------------------------------------------------------
INSERT INTO exam_questions (exam_id, question_id, question_order)
VALUES
-- Exam 1: Java (Questions 1 to 5)
(1, 1, 1),
(1, 2, 2),
(1, 3, 3),
(1, 4, 4),
(1, 5, 5),
-- Exam 2: DBMS & SQL (Questions 6 to 10)
(2, 6, 1),
(2, 7, 2),
(2, 8, 3),
(2, 9, 4),
(2, 10, 5)
ON DUPLICATE KEY UPDATE question_order = VALUES(question_order);

-- --------------------------------------------------------
-- 5. Seed a Sample Completed Attempt for Student 1
-- --------------------------------------------------------
INSERT INTO exam_attempts (attempt_id, user_id, exam_id, start_time, end_time, submission_type, status, score, percentage, passed)
VALUES
(1, 2, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 'MANUAL', 'SUBMITTED', 40.00, 80.00, TRUE)
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO attempt_answers (attempt_id, question_id, selected_option, is_correct)
VALUES
(1, 1, 'C', TRUE),
(1, 2, 'B', TRUE),
(1, 3, 'B', TRUE),
(1, 4, 'A', TRUE),
(1, 5, 'A', FALSE)
ON DUPLICATE KEY UPDATE selected_option = VALUES(selected_option);

INSERT INTO exam_results (result_id, attempt_id, user_id, exam_id, total_questions, answered_count, correct_count, incorrect_count, score, total_marks, percentage, passed, graded_at)
VALUES
(1, 1, 2, 1, 5, 5, 4, 1, 40.00, 50.00, 80.00, TRUE, NOW())
ON DUPLICATE KEY UPDATE score = VALUES(score);

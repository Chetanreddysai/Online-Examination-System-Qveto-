-- ========================================================
-- Qveto Online Examination System (Project 3)
-- Database Schema Definition (MySQL)
-- ========================================================

CREATE DATABASE IF NOT EXISTS online_exam_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE online_exam_db;

-- --------------------------------------------------------
-- 1. Users Table: Admins and Students
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STUDENT') NOT NULL DEFAULT 'STUDENT',
    phone VARCHAR(20) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- --------------------------------------------------------
-- 2. Exams Table: Examination definitions
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS exams (
    exam_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    duration_minutes INT NOT NULL DEFAULT 30,
    total_questions INT NOT NULL DEFAULT 10,
    total_marks DECIMAL(7,2) NOT NULL DEFAULT 100.00,
    passing_percentage DECIMAL(5,2) NOT NULL DEFAULT 40.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (duration_minutes > 0),
    CHECK (total_questions > 0),
    CHECK (total_marks > 0),
    CHECK (passing_percentage >= 0 AND passing_percentage <= 100)
) ENGINE=InnoDB;

-- --------------------------------------------------------
-- 3. Questions Table: Reusable Question Bank
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS questions (
    question_id INT PRIMARY KEY AUTO_INCREMENT,
    subject VARCHAR(100) NOT NULL,
    question_text TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_option ENUM('A', 'B', 'C', 'D') NOT NULL,
    marks DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (marks > 0)
) ENGINE=InnoDB;

-- --------------------------------------------------------
-- 4. Exam Questions Table: Many-to-Many Question Bank Mapping
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS exam_questions (
    exam_id INT NOT NULL,
    question_id INT NOT NULL,
    question_order INT NOT NULL DEFAULT 1,
    PRIMARY KEY (exam_id, question_id),
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------
-- 5. Exam Attempts Table: Student Exam Sessions & Lifecycle
-- State Machine: IN_PROGRESS -> SUBMITTED | TIMED_OUT | ABANDONED
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS exam_attempts (
    attempt_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    exam_id INT NOT NULL,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL DEFAULT NULL,
    submission_type ENUM('MANUAL', 'AUTO_TIMEOUT') DEFAULT NULL,
    status ENUM('IN_PROGRESS', 'SUBMITTED', 'TIMED_OUT', 'ABANDONED') NOT NULL DEFAULT 'IN_PROGRESS',
    score DECIMAL(7,2) DEFAULT NULL,
    percentage DECIMAL(5,2) DEFAULT NULL,
    passed BOOLEAN DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE,
    CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'TIMED_OUT', 'ABANDONED')),
    CHECK (submission_type IS NULL OR submission_type IN ('MANUAL', 'AUTO_TIMEOUT'))
) ENGINE=InnoDB;

-- --------------------------------------------------------
-- 6. Attempt Answers Table: Answer persistence during test
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS attempt_answers (
    answer_id INT PRIMARY KEY AUTO_INCREMENT,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_option ENUM('A', 'B', 'C', 'D') DEFAULT NULL,
    is_correct BOOLEAN DEFAULT NULL,
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_attempt_question (attempt_id, question_id),
    FOREIGN KEY (attempt_id) REFERENCES exam_attempts(attempt_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------
-- 7. Exam Results Table: Final graded scores & summaries
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS exam_results (
    result_id INT PRIMARY KEY AUTO_INCREMENT,
    attempt_id INT NOT NULL UNIQUE,
    user_id INT NOT NULL,
    exam_id INT NOT NULL,
    total_questions INT NOT NULL,
    answered_count INT NOT NULL DEFAULT 0,
    correct_count INT NOT NULL DEFAULT 0,
    incorrect_count INT NOT NULL DEFAULT 0,
    score DECIMAL(7,2) NOT NULL DEFAULT 0.00,
    total_marks DECIMAL(7,2) NOT NULL DEFAULT 0.00,
    percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    passed BOOLEAN NOT NULL DEFAULT FALSE,
    graded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attempt_id) REFERENCES exam_attempts(attempt_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Create Indexes for fast querying
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_exams_active ON exams(is_active);
CREATE INDEX idx_questions_subject ON questions(subject);
CREATE INDEX idx_attempts_user_exam ON exam_attempts(user_id, exam_id);
CREATE INDEX idx_results_user ON exam_results(user_id);

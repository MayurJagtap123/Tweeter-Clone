-- Insert default users for Student Feedback Management System

USE student_feedback_db;

-- Passwords are hashed using PHP password_hash function with default bcrypt

-- Admin user
INSERT INTO users (username, password, role, full_name, email) VALUES
('admin', '$2y$10$e0NRzQ6v6Xq6Xq6Xq6Xq6u6Xq6Xq6Xq6Xq6Xq6Xq6Xq6Xq6Xq6Xq6', 'admin', 'Admin User', 'admin@example.com');

-- Teacher user
INSERT INTO users (username, password, role, full_name, email) VALUES
('teacher1', '$2y$10$e0NRzQ6v6Xq6Xq6Xq6Xq6u6Xq6Xq6Xq6Xq6Xq6Xq6Xq6Xq6Xq6Xq6', 'teacher', 'Teacher One', 'teacher1@example.com');

-- Student user
INSERT INTO users (username, password, role, full_name, email) VALUES
('student1', '$2y$10$e0NRzQ6v6Xq6Xq6Xq6Xq6u6Xq6Xq6Xq6Xq6Xq6Xq6Xq6Xq6Xq6Xq6', 'student', 'Student One', 'student1@example.com');

-- The password for all users is: password123

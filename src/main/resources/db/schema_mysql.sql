-- CREATE DATABASE IF NOT EXISTS student_topic_management CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- USE student_topic_management;
SET NAMES utf8mb4;
SET time_zone = '+00:00';

CREATE TABLE `departments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(150) NOT NULL,
  `description` TEXT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_departments_1` (`name`)
 ) ENGINE=InnoDB;

CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(100) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `full_name` VARCHAR(150) NOT NULL,
  `email` VARCHAR(254) NOT NULL,
  `enabled` BOOLEAN NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_1` (`username`),
  UNIQUE KEY `uq_users_2` (`email`)
 ) ENGINE=InnoDB;

CREATE TABLE `roles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_roles_1` (`code`)
 ) ENGINE=InnoDB;

CREATE TABLE `user_roles` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  CONSTRAINT `fk_user_roles_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_roles_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `students` (
  `user_id` BIGINT NOT NULL,
  `student_code` VARCHAR(30) NOT NULL,
  `class_name` VARCHAR(100) NULL,
  `department_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uq_students_1` (`student_code`),
  CONSTRAINT `fk_students_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_students_2` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `lecturers` (
  `user_id` BIGINT NOT NULL,
  `lecturer_code` VARCHAR(30) NOT NULL,
  `department_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uq_lecturers_1` (`lecturer_code`),
  CONSTRAINT `fk_lecturers_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_lecturers_2` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `registration_rounds` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `round_type` VARCHAR(20) NOT NULL,
  `proposal_starts_at` DATETIME NOT NULL,
  `proposal_ends_at` DATETIME NOT NULL,
  `student_starts_at` DATETIME NOT NULL,
  `student_ends_at` DATETIME NOT NULL,
  `reviewer_due_at` DATETIME NULL,
  `council_date` DATE NULL,
  `report_due_at` DATETIME NULL,
  `group_min_size` TINYINT NOT NULL DEFAULT 3,
  `group_max_size` TINYINT NOT NULL DEFAULT 3,
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  `created_by` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `ck_registration_rounds_1` CHECK (group_min_size BETWEEN 1 AND 5 AND group_max_size BETWEEN group_min_size AND 5),
  CONSTRAINT `ck_registration_rounds_2` CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED')),
  CONSTRAINT `ck_registration_rounds_3` CHECK (round_type IN ('COURSE','RESEARCH','SPECIALIZATION','THESIS')),
  CONSTRAINT `ck_registration_rounds_4` CHECK (proposal_starts_at < proposal_ends_at AND proposal_ends_at <= student_starts_at AND student_starts_at < student_ends_at),
  CONSTRAINT `ck_registration_rounds_5` CHECK (((round_type IN ('SPECIALIZATION','THESIS') AND reviewer_due_at IS NOT NULL AND reviewer_due_at > student_ends_at) OR (round_type IN ('COURSE','RESEARCH') AND reviewer_due_at IS NULL))),
  CONSTRAINT `ck_registration_rounds_6` CHECK (((round_type = 'THESIS' AND council_date IS NOT NULL AND council_date >= DATE(student_ends_at)) OR (round_type <> 'THESIS' AND council_date IS NULL))),
  CONSTRAINT `ck_registration_rounds_7` CHECK (report_due_at IS NULL OR report_due_at > student_ends_at),
  CONSTRAINT `fk_registration_rounds_1` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `topics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `round_id` BIGINT NOT NULL,
  `department_id` BIGINT NOT NULL,
  `code` VARCHAR(40) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT NOT NULL,
  `requirements` TEXT NULL,
  `proposed_by` BIGINT NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  `department_decision` VARCHAR(10) NULL,
  `department_reviewed_by` BIGINT NULL,
  `department_reviewed_at` DATETIME NULL,
  `department_comment` TEXT NULL,
  `approved_by` BIGINT NULL,
  `approved_at` DATETIME NULL,
  `rejection_reason` TEXT NULL,
  `published_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_topics_1` (`round_id`, `code`),
  UNIQUE KEY `uq_topics_2` (`id`, `round_id`),
  KEY `ix_topics_1` (`round_id`, `status`),
  CONSTRAINT `ck_topics_1` CHECK (status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'PUBLISHED')),
  CONSTRAINT `ck_topics_2` CHECK (department_decision IS NULL OR department_decision IN ('APPROVED','REJECTED')),
  CONSTRAINT `fk_topics_1` FOREIGN KEY (`department_reviewed_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_topics_2` FOREIGN KEY (`round_id`) REFERENCES `registration_rounds` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_topics_3` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_topics_4` FOREIGN KEY (`proposed_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_topics_5` FOREIGN KEY (`approved_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `topic_supervisors` (
  `topic_id` BIGINT NOT NULL,
  `lecturer_id` BIGINT NOT NULL,
  `slot_no` TINYINT NOT NULL,
  PRIMARY KEY (`topic_id`, `lecturer_id`),
  UNIQUE KEY `uq_topic_supervisors_1` (`topic_id`, `slot_no`),
  CONSTRAINT `ck_topic_supervisors_1` CHECK (slot_no BETWEEN 1 AND 2),
  CONSTRAINT `fk_topic_supervisors_1` FOREIGN KEY (`topic_id`) REFERENCES `topics` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_topic_supervisors_2` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturers` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `student_groups` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `round_id` BIGINT NOT NULL,
  `name` VARCHAR(150) NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'FORMING',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `closed_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_student_groups_1` (`id`, `round_id`),
  CONSTRAINT `ck_student_groups_1` CHECK (status IN ('FORMING', 'LOCKED', 'COMPLETED')),
  CONSTRAINT `fk_student_groups_1` FOREIGN KEY (`round_id`) REFERENCES `registration_rounds` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `group_members` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `slot_no` TINYINT NOT NULL,
  `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `left_at` DATETIME NULL,
  `active_student_id` BIGINT GENERATED ALWAYS AS (CASE WHEN left_at IS NULL THEN student_id ELSE NULL END) STORED,
  `active_slot_no` TINYINT GENERATED ALWAYS AS (CASE WHEN left_at IS NULL THEN slot_no ELSE NULL END) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_group_members_1` (`active_student_id`),
  UNIQUE KEY `uq_group_members_2` (`group_id`, `active_slot_no`),
  UNIQUE KEY `uq_group_members_3` (`group_id`, `student_id`),
  CONSTRAINT `ck_group_members_1` CHECK (slot_no BETWEEN 1 AND 5),
  CONSTRAINT `ck_group_members_2` CHECK (left_at IS NULL OR left_at >= joined_at),
  CONSTRAINT `fk_group_members_1` FOREIGN KEY (`group_id`) REFERENCES `student_groups` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_group_members_2` FOREIGN KEY (`student_id`) REFERENCES `students` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `topic_registrations` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT NOT NULL,
  `topic_id` BIGINT NOT NULL,
  `round_id` BIGINT NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `submitted_by` BIGINT NOT NULL,
  `submitted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `decided_by` BIGINT NULL,
  `decided_at` DATETIME NULL,
  `decision_note` TEXT NULL,
  `reserved_topic_id` BIGINT GENERATED ALWAYS AS (CASE WHEN status IN ('PENDING','APPROVED') THEN topic_id ELSE NULL END) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_topic_registrations_1` (`group_id`),
  UNIQUE KEY `uq_topic_registrations_2` (`reserved_topic_id`),
  UNIQUE KEY `uq_topic_registrations_3` (`id`, `round_id`),
  KEY `ix_topic_registrations_1` (`round_id`, `status`),
  CONSTRAINT `ck_topic_registrations_1` CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
  CONSTRAINT `fk_topic_registrations_1` FOREIGN KEY (`group_id`, `round_id`) REFERENCES `student_groups` (`id`, `round_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_topic_registrations_2` FOREIGN KEY (`topic_id`, `round_id`) REFERENCES `topics` (`id`, `round_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_topic_registrations_3` FOREIGN KEY (`submitted_by`) REFERENCES `students` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_topic_registrations_4` FOREIGN KEY (`decided_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

-- C03: Luu snapshot thanh vien tai thoi diem nop dang ky de tai.
CREATE TABLE `registration_members` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `registration_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `slot_no` TINYINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_registration_members_student` (`registration_id`, `student_id`),
  UNIQUE KEY `uq_registration_members_slot` (`registration_id`, `slot_no`),
  CONSTRAINT `ck_registration_members_slot` CHECK (`slot_no` BETWEEN 1 AND 5),
  CONSTRAINT `fk_registration_members_registration` FOREIGN KEY (`registration_id`) REFERENCES `topic_registrations` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_registration_members_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`user_id`) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE `reports` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `registration_id` BIGINT NOT NULL,
  `version_no` INT NOT NULL,
  `storage_key` VARCHAR(255) NOT NULL,
  `original_name` VARCHAR(255) NOT NULL,
  `content_type` VARCHAR(100) NOT NULL,
  `size_bytes` BIGINT NOT NULL,
  `sha256` CHAR(64) NOT NULL,
  `submitted_by` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_reports_1` (`registration_id`, `version_no`),
  UNIQUE KEY `uq_reports_2` (`storage_key`),
  CONSTRAINT `ck_reports_1` CHECK (version_no > 0),
  CONSTRAINT `ck_reports_2` CHECK (size_bytes > 0),
  CONSTRAINT `fk_reports_1` FOREIGN KEY (`registration_id`) REFERENCES `topic_registrations` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reports_2` FOREIGN KEY (`submitted_by`) REFERENCES `students` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `councils` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `round_id` BIGINT NOT NULL,
  `name` VARCHAR(150) NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  `created_by` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_councils_1` (`id`, `round_id`),
  CONSTRAINT `ck_councils_1` CHECK (status IN ('DRAFT', 'ACTIVE', 'CLOSED')),
  CONSTRAINT `fk_councils_1` FOREIGN KEY (`round_id`) REFERENCES `registration_rounds` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_councils_2` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `council_members` (
  `council_id` BIGINT NOT NULL,
  `lecturer_id` BIGINT NOT NULL,
  `slot_no` TINYINT NOT NULL,
  PRIMARY KEY (`council_id`, `lecturer_id`),
  UNIQUE KEY `uq_council_members_1` (`council_id`, `slot_no`),
  CONSTRAINT `ck_council_members_1` CHECK (slot_no BETWEEN 1 AND 5),
  CONSTRAINT `fk_council_members_1` FOREIGN KEY (`council_id`) REFERENCES `councils` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_council_members_2` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturers` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `defenses` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `registration_id` BIGINT NOT NULL,
  `round_id` BIGINT NOT NULL,
  `council_id` BIGINT NOT NULL,
  `scheduled_at` DATETIME NOT NULL,
  `location` VARCHAR(200) NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_defenses_1` (`registration_id`),
  CONSTRAINT `ck_defenses_1` CHECK (status IN ('SCHEDULED', 'DONE', 'CANCELLED')),
  CONSTRAINT `fk_defenses_1` FOREIGN KEY (`registration_id`, `round_id`) REFERENCES `topic_registrations` (`id`, `round_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_defenses_2` FOREIGN KEY (`council_id`, `round_id`) REFERENCES `councils` (`id`, `round_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `reviewer_assignments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `registration_id` BIGINT NOT NULL,
  `lecturer_id` BIGINT NOT NULL,
  `assignment_type` VARCHAR(20) NOT NULL,
  `assigned_by` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_reviewer_assignments_1` (`registration_id`, `lecturer_id`),
  CONSTRAINT `ck_reviewer_assignments_1` CHECK (assignment_type IN ('REVIEWER','COUNCIL')),
  CONSTRAINT `fk_reviewer_assignments_1` FOREIGN KEY (`registration_id`) REFERENCES `topic_registrations` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reviewer_assignments_2` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturers` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reviewer_assignments_3` FOREIGN KEY (`assigned_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `evaluations` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `assignment_id` BIGINT NOT NULL,
  `score` DECIMAL(4,2) NOT NULL,
  `comments` TEXT NULL,
  `submitted_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_evaluations_1` (`assignment_id`),
  CONSTRAINT `ck_evaluations_1` CHECK (score BETWEEN 0 AND 10),
  CONSTRAINT `fk_evaluations_1` FOREIGN KEY (`assignment_id`) REFERENCES `reviewer_assignments` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `results` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `registration_id` BIGINT NOT NULL,
  `final_score` DECIMAL(4,2) NOT NULL,
  `component_count` INT NOT NULL,
  `conclusion` TEXT NOT NULL,
  `confirmed_by` BIGINT NOT NULL,
  `confirmed_at` DATETIME NOT NULL,
  `published_by` BIGINT NULL,
  `published_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_results_1` (`registration_id`),
  CONSTRAINT `ck_results_1` CHECK (final_score BETWEEN 0 AND 10),
  CONSTRAINT `ck_results_2` CHECK (component_count > 0),
  CONSTRAINT `ck_results_3` CHECK ((published_by IS NULL AND published_at IS NULL) OR (published_by IS NOT NULL AND published_at IS NOT NULL)),
  CONSTRAINT `ck_results_4` CHECK (published_at IS NULL OR published_at >= confirmed_at),
  CONSTRAINT `fk_results_1` FOREIGN KEY (`registration_id`) REFERENCES `topic_registrations` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_results_2` FOREIGN KEY (`confirmed_by`) REFERENCES `lecturers` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_results_3` FOREIGN KEY (`published_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `announcements` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT NOT NULL,
  `author_id` BIGINT NOT NULL,
  `published_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_announcements_1` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `audit_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `actor_id` BIGINT NULL,
  `action` VARCHAR(60) NOT NULL,
  `entity_type` VARCHAR(60) NOT NULL,
  `entity_id` BIGINT NULL,
  `details` TEXT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `ix_audit_logs_1` (`entity_type`, `entity_id`, `created_at`),
  CONSTRAINT `fk_audit_logs_1` FOREIGN KEY (`actor_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

CREATE TABLE `email_outbox` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `result_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `recipient_email` VARCHAR(254) NOT NULL,
  `subject` VARCHAR(255) NOT NULL,
  `body` TEXT NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `attempts` INT NOT NULL DEFAULT 0,
  `next_attempt_at` DATETIME NULL,
  `locked_at` DATETIME NULL,
  `sent_at` DATETIME NULL,
  `last_error` VARCHAR(500) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_email_outbox_1` (`result_id`, `student_id`),
  KEY `ix_email_outbox_1` (`status`, `next_attempt_at`),
  CONSTRAINT `ck_email_outbox_1` CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED')),
  CONSTRAINT `ck_email_outbox_2` CHECK (attempts >= 0),
  CONSTRAINT `fk_email_outbox_1` FOREIGN KEY (`result_id`) REFERENCES `results` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_email_outbox_2` FOREIGN KEY (`student_id`) REFERENCES `students` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=InnoDB;

INSERT INTO roles(code) VALUES ('ADMIN'), ('DEAN'), ('DEPARTMENT_HEAD'), ('LECTURER'), ('STUDENT');
-- Tạo tài khoản demo bằng ứng dụng sử dụng PasswordEncoder; không nhúng mật khẩu thật vào SQL.

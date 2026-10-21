USE `defaultdb`;
SET NAMES utf8mb4;
SET SESSION time_zone = '+00:00';
SET @demo_password_prefix = '';

DROP PROCEDURE IF EXISTS `seed_demo_hcmute_20261021`;
DELIMITER $$
CREATE PROCEDURE `seed_demo_hcmute_20261021`()
SQL SECURITY INVOKER
BEGIN
  DECLARE v_day DATETIME;
  DECLARE v_password_hash VARCHAR(255);
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  SET v_day = TIMESTAMP(CURRENT_DATE());
  SET v_password_hash = CONCAT(COALESCE(@demo_password_prefix, ''), '$2b$10$s2hRwosbs0EVwPxPxGSrqO5wUAgDN..rAVQUADITGaBvwLjCYIgD2');
  START TRANSACTION;
  IF DATABASE() <> 'defaultdb' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Hay chon database defaultdb.';
  END IF;
  IF (SELECT COUNT(*) FROM roles WHERE code IN
      ('ADMIN','DEAN','DEPARTMENT_HEAD','LECTURER','STUDENT')) <> 5 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thieu role: chay schema truoc khi nap demo.';
  END IF;
  IF EXISTS (SELECT 1 FROM `departments` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: departments.';
  END IF;
  IF EXISTS (SELECT 1 FROM `users` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: users.';
  END IF;
  IF EXISTS (SELECT 1 FROM `user_roles` WHERE `user_id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: user_roles.';
  END IF;
  IF EXISTS (SELECT 1 FROM `lecturers` WHERE `user_id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: lecturers.';
  END IF;
  IF EXISTS (SELECT 1 FROM `students` WHERE `user_id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: students.';
  END IF;
  IF EXISTS (SELECT 1 FROM `registration_rounds` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: registration_rounds.';
  END IF;
  IF EXISTS (SELECT 1 FROM `topics` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: topics.';
  END IF;
  IF EXISTS (SELECT 1 FROM `topic_supervisors` WHERE `topic_id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: topic_supervisors.';
  END IF;
  IF EXISTS (SELECT 1 FROM `student_groups` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: student_groups.';
  END IF;
  IF EXISTS (SELECT 1 FROM `group_members` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: group_members.';
  END IF;
  IF EXISTS (SELECT 1 FROM `topic_registrations` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: topic_registrations.';
  END IF;
  IF EXISTS (SELECT 1 FROM `registration_members` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: registration_members.';
  END IF;
  IF EXISTS (SELECT 1 FROM `reports` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: reports.';
  END IF;
  IF EXISTS (SELECT 1 FROM `councils` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: councils.';
  END IF;
  IF EXISTS (SELECT 1 FROM `council_members` WHERE `council_id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: council_members.';
  END IF;
  IF EXISTS (SELECT 1 FROM `defenses` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: defenses.';
  END IF;
  IF EXISTS (SELECT 1 FROM `reviewer_assignments` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: reviewer_assignments.';
  END IF;
  IF EXISTS (SELECT 1 FROM `evaluations` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: evaluations.';
  END IF;
  IF EXISTS (SELECT 1 FROM `results` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: results.';
  END IF;
  IF EXISTS (SELECT 1 FROM `announcements` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: announcements.';
  END IF;
  IF EXISTS (SELECT 1 FROM `audit_logs` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: audit_logs.';
  END IF;
  IF EXISTS (SELECT 1 FROM `email_outbox` WHERE `id` BETWEEN 910000 AND 910999) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Demo da ton tai hoac trung dai ID: email_outbox.';
  END IF;

  -- departments: 3 bản ghi mẫu.
  INSERT INTO `departments` (`id`, `name`, `description`, `created_at`) VALUES
    (910001, 'DEMO - Công nghệ phần mềm', 'Bộ môn mẫu phục vụ trình diễn, không phải dữ liệu chính thức.', TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910002, 'DEMO - Hệ thống thông tin', 'Bộ môn mẫu phục vụ trình diễn, không phải dữ liệu chính thức.', TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910003, 'DEMO - Mạng máy tính', 'Bộ môn mẫu phục vụ trình diễn, không phải dữ liệu chính thức.', TIMESTAMPADD(MINUTE, -129600, v_day));

  -- users: 34 bản ghi mẫu.
  INSERT INTO `users` (`id`, `username`, `password_hash`, `full_name`, `email`, `enabled`, `created_at`) VALUES
    (910001, 'demo.admin', v_password_hash, 'Quản trị viên Demo', 'demo.admin@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910002, 'demo.dean', v_password_hash, 'Trưởng khoa Demo', 'demo.dean@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910101, 'demo.gv01', v_password_hash, 'Nguyễn Minh An', 'demo.gv01@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910102, 'demo.gv02', v_password_hash, 'Trần Thanh Bình', 'demo.gv02@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910103, 'demo.gv03', v_password_hash, 'Lê Hoàng Châu', 'demo.gv03@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910104, 'demo.gv04', v_password_hash, 'Phạm Ngọc Dung', 'demo.gv04@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910105, 'demo.gv05', v_password_hash, 'Võ Thành Đạt', 'demo.gv05@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910106, 'demo.gv06', v_password_hash, 'Đặng Thị Hạnh', 'demo.gv06@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910107, 'demo.gv07', v_password_hash, 'Bùi Quốc Khánh', 'demo.gv07@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910108, 'demo.gv08', v_password_hash, 'Đỗ Minh Lâm', 'demo.gv08@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910201, 'demo.sv01', v_password_hash, 'Nguyễn Minh Anh', 'demo.sv01@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910202, 'demo.sv02', v_password_hash, 'Trần Quốc Bảo', 'demo.sv02@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910203, 'demo.sv03', v_password_hash, 'Lê Ngọc Chi', 'demo.sv03@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910204, 'demo.sv04', v_password_hash, 'Phạm Tiến Dũng', 'demo.sv04@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910205, 'demo.sv05', v_password_hash, 'Võ Thùy Dương', 'demo.sv05@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910206, 'demo.sv06', v_password_hash, 'Đặng Minh Đức', 'demo.sv06@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910207, 'demo.sv07', v_password_hash, 'Bùi Gia Hân', 'demo.sv07@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910208, 'demo.sv08', v_password_hash, 'Đỗ Trung Hiếu', 'demo.sv08@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910209, 'demo.sv09', v_password_hash, 'Hồ Hoàng Khang', 'demo.sv09@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910210, 'demo.sv10', v_password_hash, 'Ngô Bảo Khánh', 'demo.sv10@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910211, 'demo.sv11', v_password_hash, 'Dương Ngọc Lan', 'demo.sv11@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910212, 'demo.sv12', v_password_hash, 'Lý Thành Long', 'demo.sv12@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910213, 'demo.sv13', v_password_hash, 'Mai Ngọc Mai', 'demo.sv13@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910214, 'demo.sv14', v_password_hash, 'Tạ Anh Minh', 'demo.sv14@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910215, 'demo.sv15', v_password_hash, 'Cao Phương Nam', 'demo.sv15@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910216, 'demo.sv16', v_password_hash, 'Huỳnh Thanh Ngân', 'demo.sv16@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910217, 'demo.sv17', v_password_hash, 'Phan Đức Phúc', 'demo.sv17@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910218, 'demo.sv18', v_password_hash, 'Trịnh Quỳnh Phương', 'demo.sv18@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910219, 'demo.sv19', v_password_hash, 'Vũ Minh Quân', 'demo.sv19@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910220, 'demo.sv20', v_password_hash, 'Tô Hoàng Sơn', 'demo.sv20@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910221, 'demo.sv21', v_password_hash, 'Đinh Thanh Tâm', 'demo.sv21@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910222, 'demo.sv22', v_password_hash, 'Đoàn Anh Thư', 'demo.sv22@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910223, 'demo.sv23', v_password_hash, 'Lâm Ngọc Trang', 'demo.sv23@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910224, 'demo.sv24', v_password_hash, 'Châu Quốc Việt', 'demo.sv24@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day));

  -- user_roles: 37 bản ghi mẫu.
  INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
    (910001, (SELECT id FROM roles WHERE code = 'ADMIN')),
    (910002, (SELECT id FROM roles WHERE code = 'DEAN')),
    (910101, (SELECT id FROM roles WHERE code = 'LECTURER')),
    (910101, (SELECT id FROM roles WHERE code = 'DEPARTMENT_HEAD')),
    (910102, (SELECT id FROM roles WHERE code = 'LECTURER')),
    (910103, (SELECT id FROM roles WHERE code = 'LECTURER')),
    (910104, (SELECT id FROM roles WHERE code = 'LECTURER')),
    (910105, (SELECT id FROM roles WHERE code = 'LECTURER')),
    (910105, (SELECT id FROM roles WHERE code = 'DEPARTMENT_HEAD')),
    (910106, (SELECT id FROM roles WHERE code = 'LECTURER')),
    (910107, (SELECT id FROM roles WHERE code = 'LECTURER')),
    (910108, (SELECT id FROM roles WHERE code = 'LECTURER')),
    (910108, (SELECT id FROM roles WHERE code = 'DEPARTMENT_HEAD')),
    (910201, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910202, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910203, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910204, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910205, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910206, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910207, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910208, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910209, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910210, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910211, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910212, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910213, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910214, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910215, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910216, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910217, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910218, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910219, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910220, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910221, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910222, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910223, (SELECT id FROM roles WHERE code = 'STUDENT')),
    (910224, (SELECT id FROM roles WHERE code = 'STUDENT'));

  -- lecturers: 8 bản ghi mẫu.
  INSERT INTO `lecturers` (`user_id`, `lecturer_code`, `department_id`) VALUES
    (910101, 'DEMO-GV01', 910001),
    (910102, 'DEMO-GV02', 910001),
    (910103, 'DEMO-GV03', 910001),
    (910104, 'DEMO-GV04', 910001),
    (910105, 'DEMO-GV05', 910002),
    (910106, 'DEMO-GV06', 910002),
    (910107, 'DEMO-GV07', 910002),
    (910108, 'DEMO-GV08', 910003);

  -- students: 24 bản ghi mẫu.
  INSERT INTO `students` (`user_id`, `student_code`, `class_name`, `department_id`) VALUES
    (910201, 'DEMO-SV01', 'DEMO-22110', 910001),
    (910202, 'DEMO-SV02', 'DEMO-22110', 910001),
    (910203, 'DEMO-SV03', 'DEMO-22110', 910001),
    (910204, 'DEMO-SV04', 'DEMO-22110', 910001),
    (910205, 'DEMO-SV05', 'DEMO-22110', 910001),
    (910206, 'DEMO-SV06', 'DEMO-22110', 910001),
    (910207, 'DEMO-SV07', 'DEMO-22110', 910001),
    (910208, 'DEMO-SV08', 'DEMO-22110', 910001),
    (910209, 'DEMO-SV09', 'DEMO-22110', 910001),
    (910210, 'DEMO-SV10', 'DEMO-22110', 910001),
    (910211, 'DEMO-SV11', 'DEMO-22110', 910001),
    (910212, 'DEMO-SV12', 'DEMO-22110', 910001),
    (910213, 'DEMO-SV13', 'DEMO-22110', 910001),
    (910214, 'DEMO-SV14', 'DEMO-22110', 910001),
    (910215, 'DEMO-SV15', 'DEMO-22110', 910001),
    (910216, 'DEMO-SV16', 'DEMO-22110', 910001),
    (910217, 'DEMO-SV17', 'DEMO-22110', 910001),
    (910218, 'DEMO-SV18', 'DEMO-22110', 910001),
    (910219, 'DEMO-SV19', 'DEMO-22110', 910001),
    (910220, 'DEMO-SV20', 'DEMO-22110', 910001),
    (910221, 'DEMO-SV21', 'DEMO-22110', 910001),
    (910222, 'DEMO-SV22', 'DEMO-22110', 910001),
    (910223, 'DEMO-SV23', 'DEMO-22110', 910001),
    (910224, 'DEMO-SV24', 'DEMO-22110', 910001);

  -- registration_rounds: 4 bản ghi mẫu.
  INSERT INTO `registration_rounds` (`id`, `name`, `round_type`, `proposal_starts_at`, `proposal_ends_at`, `student_starts_at`, `student_ends_at`, `reviewer_due_at`, `council_date`, `report_due_at`, `group_min_size`, `group_max_size`, `status`, `created_by`, `created_at`) VALUES
    (910001, 'DEMO R1 - Đang mở đăng ký', 'COURSE', TIMESTAMPADD(MINUTE, -43200, v_day), TIMESTAMPADD(MINUTE, -14400, v_day), TIMESTAMPADD(MINUTE, -12960, v_day), TIMESTAMPADD(MINUTE, 20160, v_day), NULL, NULL, TIMESTAMPADD(MINUTE, 37439, v_day), 3, 3, 'OPEN', 910002, TIMESTAMPADD(MINUTE, -46080, v_day)),
    (910002, 'DEMO R2 - Đang nhận đề xuất', 'RESEARCH', TIMESTAMPADD(MINUTE, -4320, v_day), TIMESTAMPADD(MINUTE, 20160, v_day), TIMESTAMPADD(MINUTE, 21600, v_day), TIMESTAMPADD(MINUTE, 43200, v_day), NULL, NULL, TIMESTAMPADD(MINUTE, 66239, v_day), 3, 3, 'OPEN', 910002, TIMESTAMPADD(MINUTE, -7200, v_day)),
    (910003, 'DEMO R3 - Đang nộp báo cáo và phản biện', 'SPECIALIZATION', TIMESTAMPADD(MINUTE, -72000, v_day), TIMESTAMPADD(MINUTE, -50400, v_day), TIMESTAMPADD(MINUTE, -48960, v_day), TIMESTAMPADD(MINUTE, -21600, v_day), TIMESTAMPADD(MINUTE, 11519, v_day), NULL, TIMESTAMPADD(MINUTE, 8639, v_day), 3, 3, 'CLOSED', 910002, TIMESTAMPADD(MINUTE, -74880, v_day)),
    (910004, 'DEMO R4 - Chốt và công bố kết quả', 'THESIS', TIMESTAMPADD(MINUTE, -115200, v_day), TIMESTAMPADD(MINUTE, -93600, v_day), TIMESTAMPADD(MINUTE, -92160, v_day), TIMESTAMPADD(MINUTE, -57600, v_day), TIMESTAMPADD(MINUTE, -1441, v_day), DATE(TIMESTAMPADD(DAY, -1, v_day)), TIMESTAMPADD(MINUTE, -12961, v_day), 3, 3, 'CLOSED', 910002, TIMESTAMPADD(MINUTE, -118080, v_day));

  -- topics: 13 bản ghi mẫu.
  INSERT INTO `topics` (`id`, `round_id`, `department_id`, `code`, `title`, `description`, `requirements`, `proposed_by`, `status`, `department_decision`, `department_reviewed_by`, `department_reviewed_at`, `department_comment`, `approved_by`, `approved_at`, `rejection_reason`, `published_at`, `created_at`) VALUES
    (910001, 910001, 910001, 'DEMO-T01', 'Quản lý thư viện trực tuyến', 'Đề tài mẫu: Quản lý thư viện trực tuyến. Xây dựng ứng dụng Spring Boot MVC, Thymeleaf, Bootstrap 5 và MySQL.', 'Có phân quyền, kiểm tra dữ liệu đầu vào, kiểm thử và tài liệu hướng dẫn.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -33120, v_day), 'Dữ liệu minh họa đã sơ duyệt.', 910002, TIMESTAMPADD(MINUTE, -31680, v_day), NULL, TIMESTAMPADD(MINUTE, -15840, v_day), TIMESTAMPADD(MINUTE, -36000, v_day)),
    (910002, 910001, 910001, 'DEMO-T02', 'Đặt lịch phòng thực hành', 'Đề tài mẫu: Đặt lịch phòng thực hành. Xây dựng ứng dụng Spring Boot MVC, Thymeleaf, Bootstrap 5 và MySQL.', 'Có phân quyền, kiểm tra dữ liệu đầu vào, kiểm thử và tài liệu hướng dẫn.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -33120, v_day), 'Dữ liệu minh họa đã sơ duyệt.', 910002, TIMESTAMPADD(MINUTE, -31680, v_day), NULL, TIMESTAMPADD(MINUTE, -15840, v_day), TIMESTAMPADD(MINUTE, -36000, v_day)),
    (910003, 910001, 910001, 'DEMO-T03', 'Quản lý câu lạc bộ sinh viên', 'Đề tài mẫu: Quản lý câu lạc bộ sinh viên. Xây dựng ứng dụng Spring Boot MVC, Thymeleaf, Bootstrap 5 và MySQL.', 'Có phân quyền, kiểm tra dữ liệu đầu vào, kiểm thử và tài liệu hướng dẫn.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -33120, v_day), 'Dữ liệu minh họa đã sơ duyệt.', 910002, TIMESTAMPADD(MINUTE, -31680, v_day), NULL, TIMESTAMPADD(MINUTE, -15840, v_day), TIMESTAMPADD(MINUTE, -36000, v_day)),
    (910004, 910001, 910001, 'DEMO-T04', 'Cổng thông tin thực tập', 'Đề tài mẫu: Cổng thông tin thực tập. Xây dựng ứng dụng Spring Boot MVC, Thymeleaf, Bootstrap 5 và MySQL.', 'Có phân quyền, kiểm tra dữ liệu đầu vào, kiểm thử và tài liệu hướng dẫn.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -33120, v_day), 'Dữ liệu minh họa đã sơ duyệt.', 910002, TIMESTAMPADD(MINUTE, -31680, v_day), NULL, TIMESTAMPADD(MINUTE, -15840, v_day), TIMESTAMPADD(MINUTE, -36000, v_day)),
    (910005, 910002, 910002, 'DEMO-T05', 'Phân tích phản hồi sinh viên', 'Đề tài mẫu: Phân tích phản hồi sinh viên. Xây dựng ứng dụng Spring Boot MVC, Thymeleaf, Bootstrap 5 và MySQL.', 'Có phân quyền, kiểm tra dữ liệu đầu vào, kiểm thử và tài liệu hướng dẫn.', 910105, 'DRAFT', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TIMESTAMPADD(MINUTE, -2400, v_day)),
    (910006, 910002, 910002, 'DEMO-T06', 'Dự báo nhu cầu mượn sách', 'Đề tài mẫu: Dự báo nhu cầu mượn sách. Xây dựng ứng dụng Spring Boot MVC, Thymeleaf, Bootstrap 5 và MySQL.', 'Có phân quyền, kiểm tra dữ liệu đầu vào, kiểm thử và tài liệu hướng dẫn.', 910105, 'SUBMITTED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TIMESTAMPADD(MINUTE, -2400, v_day)),
    (910007, 910002, 910002, 'DEMO-T07', 'Gợi ý học phần theo sở thích', 'Đề tài mẫu: Gợi ý học phần theo sở thích. Xây dựng ứng dụng Spring Boot MVC, Thymeleaf, Bootstrap 5 và MySQL.', 'Có phân quyền, kiểm tra dữ liệu đầu vào, kiểm thử và tài liệu hướng dẫn.', 910105, 'APPROVED', 'APPROVED', 910105, TIMESTAMPADD(MINUTE, -900, v_day), 'Dữ liệu minh họa đã sơ duyệt.', 910002, TIMESTAMPADD(MINUTE, -840, v_day), NULL, NULL, TIMESTAMPADD(MINUTE, -2400, v_day)),
    (910008, 910002, 910002, 'DEMO-T08', 'Phân tích dữ liệu quá rộng', 'Đề tài mẫu: Phân tích dữ liệu quá rộng. Xây dựng ứng dụng Spring Boot MVC, Thymeleaf, Bootstrap 5 và MySQL.', 'Có phân quyền, kiểm tra dữ liệu đầu vào, kiểm thử và tài liệu hướng dẫn.', 910105, 'REJECTED', 'REJECTED', 910105, TIMESTAMPADD(MINUTE, -900, v_day), 'Dữ liệu minh họa đã sơ duyệt.', NULL, NULL, 'Phạm vi quá rộng, cần thu hẹp chức năng.', NULL, TIMESTAMPADD(MINUTE, -2400, v_day)),
    (910009, 910003, 910001, 'DEMO-T09', 'Quản lý đăng ký sự kiện', 'Đề tài mẫu: Quản lý đăng ký sự kiện. Xây dựng ứng dụng Spring Boot MVC, Thymeleaf, Bootstrap 5 và MySQL.', 'Có phân quyền, kiểm tra dữ liệu đầu vào, kiểm thử và tài liệu hướng dẫn.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -61920, v_day), 'Dữ liệu minh họa đã sơ duyệt.', 910002, TIMESTAMPADD(MINUTE, -60480, v_day), NULL, TIMESTAMPADD(MINUTE, -51840, v_day), TIMESTAMPADD(MINUTE, -64800, v_day)),
    (910010, 910003, 910001, 'DEMO-T10', 'Hệ thống hỗ trợ cố vấn học tập', 'Đề tài mẫu: Hệ thống hỗ trợ cố vấn học tập. Xây dựng ứng dụng Spring Boot MVC, Thymeleaf, Bootstrap 5 và MySQL.', 'Có phân quyền, kiểm tra dữ liệu đầu vào, kiểm thử và tài liệu hướng dẫn.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -61920, v_day), 'Dữ liệu minh họa đã sơ duyệt.', 910002, TIMESTAMPADD(MINUTE, -60480, v_day), NULL, TIMESTAMPADD(MINUTE, -51840, v_day), TIMESTAMPADD(MINUTE, -64800, v_day)),
    (910011, 910004, 910001, 'DEMO-T11', 'Quản lý đề tài sinh viên', 'Đề tài mẫu: Quản lý đề tài sinh viên. Xây dựng ứng dụng Spring Boot MVC, Thymeleaf, Bootstrap 5 và MySQL.', 'Có phân quyền, kiểm tra dữ liệu đầu vào, kiểm thử và tài liệu hướng dẫn.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -105120, v_day), 'Dữ liệu minh họa đã sơ duyệt.', 910002, TIMESTAMPADD(MINUTE, -103680, v_day), NULL, TIMESTAMPADD(MINUTE, -95040, v_day), TIMESTAMPADD(MINUTE, -108000, v_day)),
    (910012, 910004, 910001, 'DEMO-T12', 'Cổng tuyển dụng thực tập', 'Đề tài mẫu: Cổng tuyển dụng thực tập. Xây dựng ứng dụng Spring Boot MVC, Thymeleaf, Bootstrap 5 và MySQL.', 'Có phân quyền, kiểm tra dữ liệu đầu vào, kiểm thử và tài liệu hướng dẫn.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -105120, v_day), 'Dữ liệu minh họa đã sơ duyệt.', 910002, TIMESTAMPADD(MINUTE, -103680, v_day), NULL, TIMESTAMPADD(MINUTE, -95040, v_day), TIMESTAMPADD(MINUTE, -108000, v_day)),
    (910013, 910004, 910001, 'DEMO-T13', 'Quản lý phòng thí nghiệm', 'Đề tài mẫu: Quản lý phòng thí nghiệm. Xây dựng ứng dụng Spring Boot MVC, Thymeleaf, Bootstrap 5 và MySQL.', 'Có phân quyền, kiểm tra dữ liệu đầu vào, kiểm thử và tài liệu hướng dẫn.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -105120, v_day), 'Dữ liệu minh họa đã sơ duyệt.', 910002, TIMESTAMPADD(MINUTE, -103680, v_day), NULL, TIMESTAMPADD(MINUTE, -95040, v_day), TIMESTAMPADD(MINUTE, -108000, v_day));

  -- topic_supervisors: 14 bản ghi mẫu.
  INSERT INTO `topic_supervisors` (`topic_id`, `lecturer_id`, `slot_no`) VALUES
    (910001, 910101, 1),
    (910002, 910101, 1),
    (910003, 910101, 1),
    (910004, 910101, 1),
    (910005, 910105, 1),
    (910006, 910105, 1),
    (910007, 910105, 1),
    (910008, 910105, 1),
    (910009, 910101, 1),
    (910010, 910101, 1),
    (910011, 910101, 1),
    (910011, 910102, 2),
    (910012, 910101, 1),
    (910013, 910101, 1);

  -- student_groups: 8 bản ghi mẫu.
  INSERT INTO `student_groups` (`id`, `round_id`, `name`, `status`, `created_at`, `closed_at`) VALUES
    (910001, 910001, 'DEMO G01', 'FORMING', TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910002, 910001, 'DEMO G02', 'FORMING', TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910003, 910001, 'DEMO G03', 'FORMING', TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910004, 910003, 'DEMO G04', 'LOCKED', TIMESTAMPADD(MINUTE, -43200, v_day), NULL),
    (910005, 910003, 'DEMO G05', 'LOCKED', TIMESTAMPADD(MINUTE, -43200, v_day), NULL),
    (910006, 910004, 'DEMO G06', 'LOCKED', TIMESTAMPADD(MINUTE, -86400, v_day), NULL),
    (910007, 910004, 'DEMO G07', 'LOCKED', TIMESTAMPADD(MINUTE, -86400, v_day), NULL),
    (910008, 910004, 'DEMO G08', 'COMPLETED', TIMESTAMPADD(MINUTE, -86400, v_day), TIMESTAMPADD(MINUTE, -480, v_day));

  -- group_members: 24 bản ghi mẫu.
  INSERT INTO `group_members` (`id`, `group_id`, `student_id`, `slot_no`, `joined_at`, `left_at`) VALUES
    (910001, 910001, 910201, 1, TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910002, 910001, 910202, 2, TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910003, 910001, 910203, 3, TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910004, 910002, 910204, 1, TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910005, 910002, 910205, 2, TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910006, 910002, 910206, 3, TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910007, 910003, 910207, 1, TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910008, 910003, 910208, 2, TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910009, 910003, 910209, 3, TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910010, 910004, 910210, 1, TIMESTAMPADD(MINUTE, -43200, v_day), NULL),
    (910011, 910004, 910211, 2, TIMESTAMPADD(MINUTE, -43200, v_day), NULL),
    (910012, 910004, 910212, 3, TIMESTAMPADD(MINUTE, -43200, v_day), NULL),
    (910013, 910005, 910213, 1, TIMESTAMPADD(MINUTE, -43200, v_day), NULL),
    (910014, 910005, 910214, 2, TIMESTAMPADD(MINUTE, -43200, v_day), NULL),
    (910015, 910005, 910215, 3, TIMESTAMPADD(MINUTE, -43200, v_day), NULL),
    (910016, 910006, 910216, 1, TIMESTAMPADD(MINUTE, -86400, v_day), NULL),
    (910017, 910006, 910217, 2, TIMESTAMPADD(MINUTE, -86400, v_day), NULL),
    (910018, 910006, 910218, 3, TIMESTAMPADD(MINUTE, -86400, v_day), NULL),
    (910019, 910007, 910219, 1, TIMESTAMPADD(MINUTE, -86400, v_day), NULL),
    (910020, 910007, 910220, 2, TIMESTAMPADD(MINUTE, -86400, v_day), NULL),
    (910021, 910007, 910221, 3, TIMESTAMPADD(MINUTE, -86400, v_day), NULL),
    (910022, 910008, 910222, 1, TIMESTAMPADD(MINUTE, -86400, v_day), TIMESTAMPADD(MINUTE, -480, v_day)),
    (910023, 910008, 910223, 2, TIMESTAMPADD(MINUTE, -86400, v_day), TIMESTAMPADD(MINUTE, -480, v_day)),
    (910024, 910008, 910224, 3, TIMESTAMPADD(MINUTE, -86400, v_day), TIMESTAMPADD(MINUTE, -480, v_day));

  -- topic_registrations: 7 bản ghi mẫu.
  INSERT INTO `topic_registrations` (`id`, `group_id`, `topic_id`, `round_id`, `status`, `submitted_by`, `submitted_at`, `decided_by`, `decided_at`, `decision_note`) VALUES
    (910002, 910002, 910002, 910001, 'PENDING', 910204, TIMESTAMPADD(MINUTE, -8640, v_day), NULL, NULL, NULL),
    (910003, 910003, 910003, 910001, 'REJECTED', 910207, TIMESTAMPADD(MINUTE, -8640, v_day), 910002, TIMESTAMPADD(MINUTE, -7200, v_day), 'Cần bổ sung kế hoạch triển khai.'),
    (910004, 910004, 910009, 910003, 'APPROVED', 910210, TIMESTAMPADD(MINUTE, -40320, v_day), 910002, TIMESTAMPADD(MINUTE, -38880, v_day), 'Chấp thuận hồ sơ demo.'),
    (910005, 910005, 910010, 910003, 'APPROVED', 910213, TIMESTAMPADD(MINUTE, -40320, v_day), 910002, TIMESTAMPADD(MINUTE, -38880, v_day), 'Chấp thuận hồ sơ demo.'),
    (910006, 910006, 910011, 910004, 'APPROVED', 910216, TIMESTAMPADD(MINUTE, -83520, v_day), 910002, TIMESTAMPADD(MINUTE, -82080, v_day), 'Chấp thuận hồ sơ demo.'),
    (910007, 910007, 910012, 910004, 'APPROVED', 910219, TIMESTAMPADD(MINUTE, -83520, v_day), 910002, TIMESTAMPADD(MINUTE, -82080, v_day), 'Chấp thuận hồ sơ demo.'),
    (910008, 910008, 910013, 910004, 'APPROVED', 910222, TIMESTAMPADD(MINUTE, -83520, v_day), 910002, TIMESTAMPADD(MINUTE, -82080, v_day), 'Chấp thuận hồ sơ demo.');

  -- registration_members: 21 bản ghi mẫu.
  INSERT INTO `registration_members` (`id`, `registration_id`, `student_id`, `slot_no`) VALUES
    (910004, 910002, 910204, 1),
    (910005, 910002, 910205, 2),
    (910006, 910002, 910206, 3),
    (910007, 910003, 910207, 1),
    (910008, 910003, 910208, 2),
    (910009, 910003, 910209, 3),
    (910010, 910004, 910210, 1),
    (910011, 910004, 910211, 2),
    (910012, 910004, 910212, 3),
    (910013, 910005, 910213, 1),
    (910014, 910005, 910214, 2),
    (910015, 910005, 910215, 3),
    (910016, 910006, 910216, 1),
    (910017, 910006, 910217, 2),
    (910018, 910006, 910218, 3),
    (910019, 910007, 910219, 1),
    (910020, 910007, 910220, 2),
    (910021, 910007, 910221, 3),
    (910022, 910008, 910222, 1),
    (910023, 910008, 910223, 2),
    (910024, 910008, 910224, 3);

  -- reports: 5 bản ghi mẫu.
  INSERT INTO `reports` (`id`, `registration_id`, `version_no`, `storage_key`, `original_name`, `content_type`, `size_bytes`, `sha256`, `submitted_by`, `created_at`) VALUES
    (910004, 910004, 1, 'demo-hcmute/report-g04-v1.pdf', 'report-g04-v1.pdf', 'application/pdf', 46732, '4240981d7980164a867f920b2b19094086fa74d4d522126569d676ecdbc9144d', 910210, TIMESTAMPADD(MINUTE, -5220, v_day)),
    (910005, 910005, 1, 'demo-hcmute/report-g05-v1.pdf', 'report-g05-v1.pdf', 'application/pdf', 46732, '8838fc3bac9b8934047dce8f704c0e994ac3c490e47e46ca20291e9e08c6b5bd', 910213, TIMESTAMPADD(MINUTE, -5220, v_day)),
    (910006, 910006, 1, 'demo-hcmute/report-g06-v1.pdf', 'report-g06-v1.pdf', 'application/pdf', 46732, '97fafef0a227d32de6ad84cbace9f833f8dd32bd89ab79cf6da1b57ae62d8a49', 910216, TIMESTAMPADD(MINUTE, -16740, v_day)),
    (910007, 910007, 1, 'demo-hcmute/report-g07-v1.pdf', 'report-g07-v1.pdf', 'application/pdf', 46732, 'ee64e700117e2d8d720944f938f81381f330d08005fe6fa80c52eda7cb25331b', 910219, TIMESTAMPADD(MINUTE, -16740, v_day)),
    (910008, 910008, 1, 'demo-hcmute/report-g08-v1.pdf', 'report-g08-v1.pdf', 'application/pdf', 46732, 'dd6a45306aa6ddf0b8819af724f6a6868cec45a0f176bc488085c76b77012adb', 910222, TIMESTAMPADD(MINUTE, -16740, v_day));

  -- councils: 3 bản ghi mẫu.
  INSERT INTO `councils` (`id`, `round_id`, `name`, `status`, `created_by`, `created_at`) VALUES
    (910001, 910003, 'DEMO H01', 'ACTIVE', 910002, TIMESTAMPADD(MINUTE, -7200, v_day)),
    (910002, 910004, 'DEMO H02', 'ACTIVE', 910002, TIMESTAMPADD(MINUTE, -28800, v_day)),
    (910003, 910001, 'DEMO H03', 'DRAFT', 910002, TIMESTAMPADD(MINUTE, -7200, v_day));

  -- council_members: 8 bản ghi mẫu.
  INSERT INTO `council_members` (`council_id`, `lecturer_id`, `slot_no`) VALUES
    (910001, 910103, 1),
    (910001, 910104, 2),
    (910001, 910106, 3),
    (910002, 910103, 1),
    (910002, 910104, 2),
    (910002, 910106, 3),
    (910003, 910103, 1),
    (910003, 910104, 2);

  -- defenses: 4 bản ghi mẫu.
  INSERT INTO `defenses` (`id`, `registration_id`, `round_id`, `council_id`, `scheduled_at`, `location`, `status`) VALUES
    (910005, 910005, 910003, 910001, TIMESTAMPADD(MINUTE, 3420, v_day), 'Phòng DEMO A101', 'SCHEDULED'),
    (910006, 910006, 910004, 910002, TIMESTAMPADD(MINUTE, -960, v_day), 'Phòng DEMO A101', 'DONE'),
    (910007, 910007, 910004, 910002, TIMESTAMPADD(MINUTE, -840, v_day), 'Phòng DEMO A101', 'DONE'),
    (910008, 910008, 910004, 910002, TIMESTAMPADD(MINUTE, -720, v_day), 'Phòng DEMO A101', 'DONE');

  -- reviewer_assignments: 16 bản ghi mẫu.
  INSERT INTO `reviewer_assignments` (`id`, `registration_id`, `lecturer_id`, `assignment_type`, `assigned_by`, `created_at`) VALUES
    (910051, 910005, 910103, 'COUNCIL', 910002, TIMESTAMPADD(MINUTE, -4320, v_day)),
    (910052, 910005, 910104, 'COUNCIL', 910002, TIMESTAMPADD(MINUTE, -4320, v_day)),
    (910053, 910005, 910106, 'COUNCIL', 910002, TIMESTAMPADD(MINUTE, -4320, v_day)),
    (910054, 910005, 910107, 'REVIEWER', 910002, TIMESTAMPADD(MINUTE, -4320, v_day)),
    (910061, 910006, 910103, 'COUNCIL', 910002, TIMESTAMPADD(MINUTE, -15840, v_day)),
    (910062, 910006, 910104, 'COUNCIL', 910002, TIMESTAMPADD(MINUTE, -15840, v_day)),
    (910063, 910006, 910106, 'COUNCIL', 910002, TIMESTAMPADD(MINUTE, -15840, v_day)),
    (910064, 910006, 910107, 'REVIEWER', 910002, TIMESTAMPADD(MINUTE, -15840, v_day)),
    (910071, 910007, 910103, 'COUNCIL', 910002, TIMESTAMPADD(MINUTE, -15840, v_day)),
    (910072, 910007, 910104, 'COUNCIL', 910002, TIMESTAMPADD(MINUTE, -15840, v_day)),
    (910073, 910007, 910106, 'COUNCIL', 910002, TIMESTAMPADD(MINUTE, -15840, v_day)),
    (910074, 910007, 910107, 'REVIEWER', 910002, TIMESTAMPADD(MINUTE, -15840, v_day)),
    (910081, 910008, 910103, 'COUNCIL', 910002, TIMESTAMPADD(MINUTE, -15840, v_day)),
    (910082, 910008, 910104, 'COUNCIL', 910002, TIMESTAMPADD(MINUTE, -15840, v_day)),
    (910083, 910008, 910106, 'COUNCIL', 910002, TIMESTAMPADD(MINUTE, -15840, v_day)),
    (910084, 910008, 910107, 'REVIEWER', 910002, TIMESTAMPADD(MINUTE, -15840, v_day));

  -- evaluations: 15 bản ghi mẫu.
  INSERT INTO `evaluations` (`id`, `assignment_id`, `score`, `comments`, `submitted_at`, `created_at`) VALUES
    (910051, 910051, 8.00, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', NULL, TIMESTAMPADD(MINUTE, -2340, v_day)),
    (910052, 910052, 7.50, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', NULL, TIMESTAMPADD(MINUTE, -2340, v_day)),
    (910054, 910054, 8.00, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', TIMESTAMPADD(MINUTE, -900, v_day), TIMESTAMPADD(MINUTE, -2340, v_day)),
    (910061, 910061, 8.00, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', TIMESTAMPADD(MINUTE, -900, v_day), TIMESTAMPADD(MINUTE, -900, v_day)),
    (910062, 910062, 7.00, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', TIMESTAMPADD(MINUTE, -900, v_day), TIMESTAMPADD(MINUTE, -900, v_day)),
    (910063, 910063, 9.00, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', TIMESTAMPADD(MINUTE, -900, v_day), TIMESTAMPADD(MINUTE, -900, v_day)),
    (910064, 910064, 6.00, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', TIMESTAMPADD(MINUTE, -3720, v_day), TIMESTAMPADD(MINUTE, -5220, v_day)),
    (910071, 910071, 8.50, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', TIMESTAMPADD(MINUTE, -780, v_day), TIMESTAMPADD(MINUTE, -780, v_day)),
    (910072, 910072, 8.00, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', TIMESTAMPADD(MINUTE, -780, v_day), TIMESTAMPADD(MINUTE, -780, v_day)),
    (910073, 910073, 9.00, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', TIMESTAMPADD(MINUTE, -780, v_day), TIMESTAMPADD(MINUTE, -780, v_day)),
    (910074, 910074, 8.50, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', TIMESTAMPADD(MINUTE, -3720, v_day), TIMESTAMPADD(MINUTE, -5220, v_day)),
    (910081, 910081, 9.00, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', TIMESTAMPADD(MINUTE, -660, v_day), TIMESTAMPADD(MINUTE, -660, v_day)),
    (910082, 910082, 8.00, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', TIMESTAMPADD(MINUTE, -660, v_day), TIMESTAMPADD(MINUTE, -660, v_day)),
    (910083, 910083, 8.50, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', TIMESTAMPADD(MINUTE, -660, v_day), TIMESTAMPADD(MINUTE, -660, v_day)),
    (910084, 910084, 8.50, 'Nhận xét demo: đáp ứng yêu cầu và giải thích được phần triển khai.', TIMESTAMPADD(MINUTE, -3720, v_day), TIMESTAMPADD(MINUTE, -5220, v_day));

  -- results: 2 bản ghi mẫu.
  INSERT INTO `results` (`id`, `registration_id`, `final_score`, `component_count`, `conclusion`, `confirmed_by`, `confirmed_at`, `published_by`, `published_at`) VALUES
    (910007, 910007, 8.50, 4, 'Kết quả giả lập để trình diễn; điểm là trung bình cộng bốn phiếu.', 910103, TIMESTAMPADD(MINUTE, -750, v_day), NULL, NULL),
    (910008, 910008, 8.50, 4, 'Kết quả giả lập để trình diễn; điểm là trung bình cộng bốn phiếu.', 910103, TIMESTAMPADD(MINUTE, -630, v_day), 910002, TIMESTAMPADD(MINUTE, -540, v_day));

  -- announcements: 3 bản ghi mẫu.
  INSERT INTO `announcements` (`id`, `title`, `content`, `author_id`, `published_at`, `created_at`) VALUES
    (910001, 'Mở đăng ký đề tài demo', 'Thông báo mẫu. Sinh viên đăng nhập, kiểm tra đợt và làm theo hướng dẫn của nhóm triển khai.', 910001, TIMESTAMPADD(MINUTE, -2400, v_day), TIMESTAMPADD(MINUTE, -3840, v_day)),
    (910002, 'Hướng dẫn nộp báo cáo PDF', 'Thông báo mẫu. Sinh viên đăng nhập, kiểm tra đợt và làm theo hướng dẫn của nhóm triển khai.', 910001, TIMESTAMPADD(MINUTE, -2340, v_day), TIMESTAMPADD(MINUTE, -3780, v_day)),
    (910003, 'Nháp thông báo hội đồng', 'Thông báo mẫu. Sinh viên đăng nhập, kiểm tra đợt và làm theo hướng dẫn của nhóm triển khai.', 910001, NULL, TIMESTAMPADD(MINUTE, -3720, v_day));

  -- audit_logs: 4 bản ghi mẫu.
  INSERT INTO `audit_logs` (`id`, `actor_id`, `action`, `entity_type`, `entity_id`, `details`, `created_at`) VALUES
    (910001, 910001, 'DEMO_SEED', 'DEMO', 910000, 'Bản ghi giả lập do seed demo tạo, không phải thao tác người dùng đã thực hiện.', TIMESTAMPADD(MINUTE, 0, v_day)),
    (910002, 910103, 'RESULT_CONFIRMED', 'RESULT', 910007, 'Bản ghi giả lập do seed demo tạo, không phải thao tác người dùng đã thực hiện.', TIMESTAMPADD(MINUTE, -750, v_day)),
    (910003, 910103, 'RESULT_CONFIRMED', 'RESULT', 910008, 'Bản ghi giả lập do seed demo tạo, không phải thao tác người dùng đã thực hiện.', TIMESTAMPADD(MINUTE, -630, v_day)),
    (910004, 910002, 'RESULT_PUBLISHED', 'RESULT', 910008, 'Bản ghi giả lập do seed demo tạo, không phải thao tác người dùng đã thực hiện.', TIMESTAMPADD(MINUTE, -540, v_day));

  -- email_outbox: 3 bản ghi mẫu.
  INSERT INTO `email_outbox` (`id`, `result_id`, `student_id`, `recipient_email`, `subject`, `body`, `status`, `attempts`, `next_attempt_at`, `locked_at`, `sent_at`, `last_error`, `created_at`) VALUES
    (910022, 910008, 910222, 'demo.sv22@hcmute.edu.vn', '[DEMO] Thông báo kết quả đề tài', 'Dữ liệu giả lập: kết quả của DEMO G08 đã được công bố. Đăng nhập ứng dụng demo để tra cứu.', 'FAILED', 5, NULL, NULL, NULL, 'DEMO_ONLY: chưa gửi email thật; job được giữ FAILED để không tự chạy.', TIMESTAMPADD(MINUTE, -540, v_day)),
    (910023, 910008, 910223, 'demo.sv23@hcmute.edu.vn', '[DEMO] Thông báo kết quả đề tài', 'Dữ liệu giả lập: kết quả của DEMO G08 đã được công bố. Đăng nhập ứng dụng demo để tra cứu.', 'FAILED', 5, NULL, NULL, NULL, 'DEMO_ONLY: chưa gửi email thật; job được giữ FAILED để không tự chạy.', TIMESTAMPADD(MINUTE, -540, v_day)),
    (910024, 910008, 910224, 'demo.sv24@hcmute.edu.vn', '[DEMO] Thông báo kết quả đề tài', 'Dữ liệu giả lập: kết quả của DEMO G08 đã được công bố. Đăng nhập ứng dụng demo để tra cứu.', 'FAILED', 5, NULL, NULL, NULL, 'DEMO_ONLY: chưa gửi email thật; job được giữ FAILED để không tự chạy.', TIMESTAMPADD(MINUTE, -540, v_day));

  -- Kiểm chứng hậu điều kiện quan trọng trước commit.
  IF EXISTS (
    SELECT 1 FROM reviewer_assignments a
    JOIN topic_registrations tr ON tr.id=a.registration_id
    JOIN topic_supervisors s ON s.topic_id=tr.topic_id AND s.lecturer_id=a.lecturer_id
    WHERE a.id BETWEEN 910000 AND 910999
  ) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loi seed: GVHD bi phan cong cham.';
  END IF;
  IF EXISTS (
    SELECT 1 FROM results r JOIN (
      SELECT a.registration_id, COUNT(*) n, ROUND(AVG(e.score),2) score,
             SUM(e.submitted_at IS NULL) drafts
      FROM reviewer_assignments a JOIN evaluations e ON e.assignment_id=a.id
      GROUP BY a.registration_id
    ) x ON x.registration_id=r.registration_id
    WHERE r.id BETWEEN 910000 AND 910999
      AND (r.final_score<>x.score OR r.component_count<>x.n OR x.drafts<>0)
  ) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loi seed: diem ket qua khong khop phieu.';
  END IF;
  COMMIT;
  SELECT 'NAP DATA DEMO THANH CONG' AS ket_qua,
         DATABASE() AS database_name, @@session.time_zone AS time_zone,
         v_day AS ngay_goc_viet_nam;
END$$
DELIMITER ;

CALL `seed_demo_hcmute_20261021`();
DROP PROCEDURE `seed_demo_hcmute_20261021`;

-- Tài khoản demo và quyền (roles được tra theo code, không giả định ID role là 1..5).
SELECT u.username, u.full_name, u.email,
       GROUP_CONCAT(r.code ORDER BY r.code SEPARATOR ', ') AS roles
FROM users u JOIN user_roles ur ON ur.user_id=u.id JOIN roles r ON r.id=ur.role_id
WHERE u.id BETWEEN 910000 AND 910999
GROUP BY u.id,u.username,u.full_name,u.email ORDER BY u.id;

-- Hồ sơ cần demo. G01 chưa đăng ký nên không xuất hiện ở query này.
SELECT g.name AS nhom, tr.id AS registration_id, tr.status AS trang_thai_dang_ky,
       t.title, r.final_score, r.published_at
FROM topic_registrations tr JOIN student_groups g ON g.id=tr.group_id
JOIN topics t ON t.id=tr.topic_id LEFT JOIN results r ON r.registration_id=tr.id
WHERE tr.id BETWEEN 910000 AND 910999 ORDER BY tr.id;

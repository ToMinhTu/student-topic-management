USE `defaultdb`;
SET NAMES utf8mb4;
SET SESSION time_zone = '+00:00';
SET @demo_password_prefix = '';

DROP PROCEDURE IF EXISTS `seed_hcmute_20261021`;
DELIMITER $$
CREATE PROCEDURE `seed_hcmute_20261021`()
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
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thieu role: chay schema truoc khi nap du lieu.';
  END IF;
  
  -- Kiểm tra dữ liệu đã tồn tại
  IF EXISTS (SELECT 1 FROM `departments` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: departments.'; END IF;
  IF EXISTS (SELECT 1 FROM `users` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: users.'; END IF;
  IF EXISTS (SELECT 1 FROM `user_roles` WHERE `user_id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: user_roles.'; END IF;
  IF EXISTS (SELECT 1 FROM `lecturers` WHERE `user_id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: lecturers.'; END IF;
  IF EXISTS (SELECT 1 FROM `students` WHERE `user_id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: students.'; END IF;
  IF EXISTS (SELECT 1 FROM `registration_rounds` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: registration_rounds.'; END IF;
  IF EXISTS (SELECT 1 FROM `topics` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: topics.'; END IF;
  IF EXISTS (SELECT 1 FROM `topic_supervisors` WHERE `topic_id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: topic_supervisors.'; END IF;
  IF EXISTS (SELECT 1 FROM `student_groups` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: student_groups.'; END IF;
  IF EXISTS (SELECT 1 FROM `group_members` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: group_members.'; END IF;
  IF EXISTS (SELECT 1 FROM `topic_registrations` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: topic_registrations.'; END IF;
  IF EXISTS (SELECT 1 FROM `registration_members` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: registration_members.'; END IF;
  IF EXISTS (SELECT 1 FROM `reports` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: reports.'; END IF;
  IF EXISTS (SELECT 1 FROM `councils` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: councils.'; END IF;
  IF EXISTS (SELECT 1 FROM `council_members` WHERE `council_id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: council_members.'; END IF;
  IF EXISTS (SELECT 1 FROM `defenses` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: defenses.'; END IF;
  IF EXISTS (SELECT 1 FROM `reviewer_assignments` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: reviewer_assignments.'; END IF;
  IF EXISTS (SELECT 1 FROM `evaluations` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: evaluations.'; END IF;
  IF EXISTS (SELECT 1 FROM `results` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: results.'; END IF;
  IF EXISTS (SELECT 1 FROM `announcements` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: announcements.'; END IF;
  IF EXISTS (SELECT 1 FROM `audit_logs` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: audit_logs.'; END IF;
  IF EXISTS (SELECT 1 FROM `email_outbox` WHERE `id` BETWEEN 910000 AND 910999) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Du lieu da ton tai: email_outbox.'; END IF;

  -- departments
  INSERT INTO `departments` (`id`, `name`, `description`, `created_at`) VALUES
    (910001, 'Công nghệ phần mềm', 'Phụ trách chuyên môn về phát triển phần mềm, quy trình kiểm thử và kỹ thuật lập trình.', TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910002, 'Hệ thống thông tin', 'Phụ trách mảng phân tích thiết kế hệ thống, cơ sở dữ liệu, ERP và quản trị dữ liệu.', TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910003, 'Mạng máy tính & ATTT', 'Phụ trách hệ thống mạng, điện toán đám mây và an toàn bảo mật thông tin.', TIMESTAMPADD(MINUTE, -129600, v_day));

  -- users
  INSERT INTO `users` (`id`, `username`, `password_hash`, `full_name`, `email`, `enabled`, `created_at`) VALUES
    (910001, 'admin', v_password_hash, 'Quản trị Hệ thống', 'admin@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910002, 'dean_fit', v_password_hash, 'PGS.TS. Lê Trọng Khoa', 'khoalt@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910101, 'gv.minhan', v_password_hash, 'TS. Nguyễn Minh An', 'minhan@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910102, 'gv.thanhbinh', v_password_hash, 'ThS. Trần Thanh Bình', 'thanhbinht@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910103, 'gv.hoangchau', v_password_hash, 'TS. Lê Hoàng Châu', 'hoangchau@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910104, 'gv.ngocdung', v_password_hash, 'ThS. Phạm Ngọc Dung', 'ngocdungp@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910105, 'gv.thanhdat', v_password_hash, 'PGS.TS. Võ Thành Đạt', 'thanhdatv@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910106, 'gv.thihanh', v_password_hash, 'ThS. Đặng Thị Hạnh', 'hanhdt@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910107, 'gv.quockhanh', v_password_hash, 'TS. Bùi Quốc Khánh', 'quockhanh@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910108, 'gv.minhlam', v_password_hash, 'ThS. Đỗ Minh Lâm', 'minhlamd@hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910201, '22110001', v_password_hash, 'Nguyễn Minh Anh', '22110001@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910202, '22110002', v_password_hash, 'Trần Quốc Bảo', '22110002@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910203, '22110003', v_password_hash, 'Lê Ngọc Chi', '22110003@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910204, '22110004', v_password_hash, 'Phạm Tiến Dũng', '22110004@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910205, '22110005', v_password_hash, 'Võ Thùy Dương', '22110005@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910206, '22110006', v_password_hash, 'Đặng Minh Đức', '22110006@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910207, '22110007', v_password_hash, 'Bùi Gia Hân', '22110007@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910208, '22110008', v_password_hash, 'Đỗ Trung Hiếu', '22110008@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910209, '22110009', v_password_hash, 'Hồ Hoàng Khang', '22110009@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910210, '22110010', v_password_hash, 'Ngô Bảo Khánh', '22110010@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910211, '22110011', v_password_hash, 'Dương Ngọc Lan', '22110011@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910212, '22110012', v_password_hash, 'Lý Thành Long', '22110012@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910213, '22110013', v_password_hash, 'Mai Ngọc Mai', '22110013@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910214, '22110014', v_password_hash, 'Tạ Anh Minh', '22110014@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910215, '22110015', v_password_hash, 'Cao Phương Nam', '22110015@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910216, '22110016', v_password_hash, 'Huỳnh Thanh Ngân', '22110016@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910217, '22110017', v_password_hash, 'Phan Đức Phúc', '22110017@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910218, '22110018', v_password_hash, 'Trịnh Quỳnh Phương', '22110018@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910219, '22110019', v_password_hash, 'Vũ Minh Quân', '22110019@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910220, '22110020', v_password_hash, 'Tô Hoàng Sơn', '22110020@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910221, '22110021', v_password_hash, 'Đinh Thanh Tâm', '22110021@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910222, '22110022', v_password_hash, 'Đoàn Anh Thư', '22110022@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910223, '22110023', v_password_hash, 'Lâm Ngọc Trang', '22110023@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day)),
    (910224, '22110024', v_password_hash, 'Châu Quốc Việt', '22110024@student.hcmute.edu.vn', 1, TIMESTAMPADD(MINUTE, -129600, v_day));

  -- user_roles
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

  -- lecturers
  INSERT INTO `lecturers` (`user_id`, `lecturer_code`, `department_id`) VALUES
    (910101, 'GV_NMAN', 910001),
    (910102, 'GV_TTBINH', 910001),
    (910103, 'GV_LHCHAU', 910001),
    (910104, 'GV_PNDUNG', 910001),
    (910105, 'GV_VTDAT', 910002),
    (910106, 'GV_DTHANH', 910002),
    (910107, 'GV_BQKHANH', 910002),
    (910108, 'GV_DMLAM', 910003);

  -- students
  INSERT INTO `students` (`user_id`, `student_code`, `class_name`, `department_id`) VALUES
    (910201, '22110001', '22JSE1', 910001),
    (910202, '22110002', '22JSE1', 910001),
    (910203, '22110003', '22JSE1', 910001),
    (910204, '22110004', '22JSE1', 910001),
    (910205, '22110005', '22JSE2', 910001),
    (910206, '22110006', '22JSE2', 910001),
    (910207, '22110007', '22JSE2', 910001),
    (910208, '22110008', '22JSE2', 910001),
    (910209, '22110009', '22JSE3', 910001),
    (910210, '22110010', '22JSE3', 910001),
    (910211, '22110011', '22JSE3', 910001),
    (910212, '22110012', '22JSE3', 910001),
    (910213, '22110013', '22JSE1', 910001),
    (910214, '22110014', '22JSE1', 910001),
    (910215, '22110015', '22JSE1', 910001),
    (910216, '22110016', '22JSE1', 910001),
    (910217, '22110017', '22JSE2', 910001),
    (910218, '22110018', '22JSE2', 910001),
    (910219, '22110019', '22JSE2', 910001),
    (910220, '22110020', '22JSE2', 910001),
    (910221, '22110021', '22JSE3', 910001),
    (910222, '22110022', '22JSE3', 910001),
    (910223, '22110023', '22JSE3', 910001),
    (910224, '22110024', '22JSE3', 910001);

  -- registration_rounds
  INSERT INTO `registration_rounds` (`id`, `name`, `round_type`, `proposal_starts_at`, `proposal_ends_at`, `student_starts_at`, `student_ends_at`, `reviewer_due_at`, `council_date`, `report_due_at`, `group_min_size`, `group_max_size`, `status`, `created_by`, `created_at`) VALUES
    (910001, 'Đợt đăng ký Đồ án môn học - Học kỳ 1 (2026-2027)', 'COURSE', TIMESTAMPADD(MINUTE, -43200, v_day), TIMESTAMPADD(MINUTE, -14400, v_day), TIMESTAMPADD(MINUTE, -12960, v_day), TIMESTAMPADD(MINUTE, 20160, v_day), NULL, NULL, TIMESTAMPADD(MINUTE, 37439, v_day), 3, 3, 'OPEN', 910002, TIMESTAMPADD(MINUTE, -46080, v_day)),
    (910002, 'Đợt đăng ký đề tài Nghiên cứu khoa học 2026', 'RESEARCH', TIMESTAMPADD(MINUTE, -4320, v_day), TIMESTAMPADD(MINUTE, 20160, v_day), TIMESTAMPADD(MINUTE, 21600, v_day), TIMESTAMPADD(MINUTE, 43200, v_day), NULL, NULL, TIMESTAMPADD(MINUTE, 66239, v_day), 3, 3, 'OPEN', 910002, TIMESTAMPADD(MINUTE, -7200, v_day)),
    (910003, 'Đợt đánh giá Đồ án chuyên ngành - Học kỳ 2', 'SPECIALIZATION', TIMESTAMPADD(MINUTE, -72000, v_day), TIMESTAMPADD(MINUTE, -50400, v_day), TIMESTAMPADD(MINUTE, -48960, v_day), TIMESTAMPADD(MINUTE, -21600, v_day), TIMESTAMPADD(MINUTE, 11519, v_day), NULL, TIMESTAMPADD(MINUTE, 8639, v_day), 3, 3, 'CLOSED', 910002, TIMESTAMPADD(MINUTE, -74880, v_day)),
    (910004, 'Đợt bảo vệ Khóa luận tốt nghiệp K22', 'THESIS', TIMESTAMPADD(MINUTE, -115200, v_day), TIMESTAMPADD(MINUTE, -93600, v_day), TIMESTAMPADD(MINUTE, -92160, v_day), TIMESTAMPADD(MINUTE, -57600, v_day), TIMESTAMPADD(MINUTE, -1441, v_day), DATE(TIMESTAMPADD(DAY, -1, v_day)), TIMESTAMPADD(MINUTE, -12961, v_day), 3, 3, 'CLOSED', 910002, TIMESTAMPADD(MINUTE, -118080, v_day));

  -- topics
  INSERT INTO `topics` (`id`, `round_id`, `department_id`, `code`, `title`, `description`, `requirements`, `proposed_by`, `status`, `department_decision`, `department_reviewed_by`, `department_reviewed_at`, `department_comment`, `approved_by`, `approved_at`, `rejection_reason`, `published_at`, `created_at`) VALUES
    (910001, 910001, 910001, 'CNPM-T01', 'Quản lý thư viện trực tuyến bằng công nghệ Spring Boot', 'Xây dựng ứng dụng quản lý mượn trả sách, áp dụng mô hình MVC, Thymeleaf, Bootstrap 5 và cơ sở dữ liệu MySQL.', 'Sinh viên có kiến thức về Spring Framework, HTML/CSS cơ bản.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -33120, v_day), 'Đề tài phù hợp với đồ án môn học.', 910002, TIMESTAMPADD(MINUTE, -31680, v_day), NULL, TIMESTAMPADD(MINUTE, -15840, v_day), TIMESTAMPADD(MINUTE, -36000, v_day)),
    (910002, 910001, 910001, 'CNPM-T02', 'Hệ thống đặt lịch phòng thực hành Khoa CNTT', 'Phát triển ứng dụng Web cho phép giảng viên và sinh viên xem lịch trống, đặt mượn phòng Lab.', 'Kiểm tra chặt chẽ ràng buộc thời gian trùng lặp.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -33120, v_day), 'Phạm vi hợp lý, có tính thực tiễn.', 910002, TIMESTAMPADD(MINUTE, -31680, v_day), NULL, TIMESTAMPADD(MINUTE, -15840, v_day), TIMESTAMPADD(MINUTE, -36000, v_day)),
    (910003, 910001, 910001, 'CNPM-T03', 'Mạng xã hội thu nhỏ cho câu lạc bộ sinh viên', 'Tạo nền tảng tương tác, đăng tin, bình luận và quản lý sự kiện nội bộ cho các CLB.', 'Cần tích hợp gửi thông báo qua email hoặc Websocket.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -33120, v_day), 'Đề tài khá thú vị, đồng ý duyệt.', 910002, TIMESTAMPADD(MINUTE, -31680, v_day), NULL, TIMESTAMPADD(MINUTE, -15840, v_day), TIMESTAMPADD(MINUTE, -36000, v_day)),
    (910004, 910001, 910001, 'CNPM-T04', 'Cổng thông tin tuyển dụng thực tập', 'Ứng dụng kết nối doanh nghiệp và sinh viên, doanh nghiệp có thể đăng JD và sinh viên nộp CV.', 'Yêu cầu quản lý phân quyền chặt chẽ (Admin, Doanh nghiệp, Sinh viên).', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -33120, v_day), 'Nội dung thực tế cao, duyệt.', 910002, TIMESTAMPADD(MINUTE, -31680, v_day), NULL, TIMESTAMPADD(MINUTE, -15840, v_day), TIMESTAMPADD(MINUTE, -36000, v_day)),
    (910005, 910002, 910002, 'HTTT-T05', 'Phân tích cảm xúc từ phản hồi môn học của sinh viên', 'Thu thập và sử dụng thuật toán Machine Learning để phân loại cảm xúc (Tích cực, Tiêu cực, Trung tính) từ text.', 'Biết sử dụng Python và một số thư viện xử lý ngôn ngữ tự nhiên cơ bản.', 910105, 'DRAFT', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TIMESTAMPADD(MINUTE, -2400, v_day)),
    (910006, 910002, 910002, 'HTTT-T06', 'Dự báo nhu cầu mượn sách thư viện theo mùa vụ', 'Phân tích dữ liệu lịch sử mượn trả bằng Pandas/Scikit-Learn để đưa ra biểu đồ dự báo.', 'Sinh viên có tư duy tốt về Data Analysis.', 910105, 'SUBMITTED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TIMESTAMPADD(MINUTE, -2400, v_day)),
    (910007, 910002, 910002, 'HTTT-T07', 'Hệ thống gợi ý học phần tự chọn bằng Content-based', 'Xây dựng Recommendation System gợi ý môn tự chọn dựa trên lịch sử điểm các môn học trước đó.', 'Chịu khó nghiên cứu thuật toán Recommendation.', 910105, 'APPROVED', 'APPROVED', 910105, TIMESTAMPADD(MINUTE, -900, v_day), 'Đề tài mang tính ứng dụng tốt.', 910002, TIMESTAMPADD(MINUTE, -840, v_day), NULL, NULL, TIMESTAMPADD(MINUTE, -2400, v_day)),
    (910008, 910002, 910002, 'HTTT-T08', 'Nghiên cứu ứng dụng Blockchain trong quản lý giáo dục toàn diện', 'Mô phỏng toàn bộ hệ thống lưu trữ điểm số sinh viên bằng công nghệ Blockchain.', 'Không yêu cầu sinh viên năm 3.', 910105, 'REJECTED', 'REJECTED', 910105, TIMESTAMPADD(MINUTE, -900, v_day), 'Phạm vi đề tài quá rộng so với NCKH cấp cơ sở, cần thu hẹp.', NULL, NULL, 'Phạm vi quá rộng, cần thu hẹp chức năng.', NULL, TIMESTAMPADD(MINUTE, -2400, v_day)),
    (910009, 910003, 910001, 'ĐACN-T09', 'Ứng dụng quản lý điểm rèn luyện sinh viên', 'Triển khai quy trình xét duyệt điểm rèn luyện số hóa qua các cấp: Cá nhân, Lớp, Khoa.', 'Giao diện thân thiện trên thiết bị di động (Responsive).', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -61920, v_day), 'Phù hợp với đồ án chuyên ngành.', 910002, TIMESTAMPADD(MINUTE, -60480, v_day), NULL, TIMESTAMPADD(MINUTE, -51840, v_day), TIMESTAMPADD(MINUTE, -64800, v_day)),
    (910010, 910003, 910001, 'ĐACN-T10', 'Hệ thống hỗ trợ Cố vấn học tập', 'Cung cấp Dashboard trực quan cho CVHT theo dõi điểm số, cảnh báo học vụ của sinh viên.', 'Biết vẽ biểu đồ bằng thư viện Chart.js.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -61920, v_day), 'Đề tài giải quyết bài toán thực tế.', 910002, TIMESTAMPADD(MINUTE, -60480, v_day), NULL, TIMESTAMPADD(MINUTE, -51840, v_day), TIMESTAMPADD(MINUTE, -64800, v_day)),
    (910011, 910004, 910001, 'KLTN-T11', 'Kiến trúc Microservices trong hệ thống thương mại điện tử', 'Tái cấu trúc ứng dụng E-commerce nguyên khối (Monolithic) sang Microservices bằng Spring Cloud.', 'Khối lượng công việc lớn, yêu cầu nhóm sinh viên có kiến thức tốt về Docker, RabbitMQ.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -105120, v_day), 'Đủ hàm lượng khoa học cho KLTN.', 910002, TIMESTAMPADD(MINUTE, -103680, v_day), NULL, TIMESTAMPADD(MINUTE, -95040, v_day), TIMESTAMPADD(MINUTE, -108000, v_day)),
    (910012, 910004, 910001, 'KLTN-T12', 'Xây dựng Chatbot tư vấn tuyển sinh bằng LLM', 'Fine-tune mô hình ngôn ngữ lớn (Open-source LLM) kết hợp RAG để trả lời câu hỏi tuyển sinh đại học.', 'Sử dụng LangChain và Vector Database.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -105120, v_day), 'Hướng công nghệ mới, đồng ý.', 910002, TIMESTAMPADD(MINUTE, -103680, v_day), NULL, TIMESTAMPADD(MINUTE, -95040, v_day), TIMESTAMPADD(MINUTE, -108000, v_day)),
    (910013, 910004, 910001, 'KLTN-T13', 'Quản lý an toàn phòng thí nghiệm IoT', 'Theo dõi nhiệt độ, độ ẩm và nhận diện rủi ro cháy nổ tại phòng máy bằng cảm biến IoT và camera AI.', 'Nhóm cần biết cấu hình Raspberry Pi, giao thức MQTT.', 910101, 'PUBLISHED', 'APPROVED', 910101, TIMESTAMPADD(MINUTE, -105120, v_day), 'Rất phù hợp để đánh giá tích hợp phần cứng - phần mềm.', 910002, TIMESTAMPADD(MINUTE, -103680, v_day), NULL, TIMESTAMPADD(MINUTE, -95040, v_day), TIMESTAMPADD(MINUTE, -108000, v_day));

  -- topic_supervisors
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

  -- student_groups
  INSERT INTO `student_groups` (`id`, `round_id`, `name`, `status`, `created_at`, `closed_at`) VALUES
    (910001, 910001, 'Nhóm 01 - ĐAMH', 'FORMING', TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910002, 910001, 'Nhóm 02 - ĐAMH', 'FORMING', TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910003, 910001, 'Nhóm 03 - ĐAMH', 'FORMING', TIMESTAMPADD(MINUTE, -11520, v_day), NULL),
    (910004, 910003, 'Nhóm 04 - ĐACN', 'LOCKED', TIMESTAMPADD(MINUTE, -43200, v_day), NULL),
    (910005, 910003, 'Nhóm 05 - ĐACN', 'LOCKED', TIMESTAMPADD(MINUTE, -43200, v_day), NULL),
    (910006, 910004, 'Nhóm 06 - KLTN', 'LOCKED', TIMESTAMPADD(MINUTE, -86400, v_day), NULL),
    (910007, 910004, 'Nhóm 07 - KLTN', 'LOCKED', TIMESTAMPADD(MINUTE, -86400, v_day), NULL),
    (910008, 910004, 'Nhóm 08 - KLTN', 'COMPLETED', TIMESTAMPADD(MINUTE, -86400, v_day), TIMESTAMPADD(MINUTE, -480, v_day));

  -- group_members
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

  -- topic_registrations
  INSERT INTO `topic_registrations` (`id`, `group_id`, `topic_id`, `round_id`, `status`, `submitted_by`, `submitted_at`, `decided_by`, `decided_at`, `decision_note`) VALUES
    (910002, 910002, 910002, 910001, 'PENDING', 910204, TIMESTAMPADD(MINUTE, -8640, v_day), NULL, NULL, NULL),
    (910003, 910003, 910003, 910001, 'REJECTED', 910207, TIMESTAMPADD(MINUTE, -8640, v_day), 910002, TIMESTAMPADD(MINUTE, -7200, v_day), 'Cần bổ sung kế hoạch triển khai chi tiết hơn.'),
    (910004, 910004, 910009, 910003, 'APPROVED', 910210, TIMESTAMPADD(MINUTE, -40320, v_day), 910002, TIMESTAMPADD(MINUTE, -38880, v_day), 'Đồng ý giao đề tài cho nhóm.'),
    (910005, 910005, 910010, 910003, 'APPROVED', 910213, TIMESTAMPADD(MINUTE, -40320, v_day), 910002, TIMESTAMPADD(MINUTE, -38880, v_day), 'Đồng ý duyệt.'),
    (910006, 910006, 910011, 910004, 'APPROVED', 910216, TIMESTAMPADD(MINUTE, -83520, v_day), 910002, TIMESTAMPADD(MINUTE, -82080, v_day), 'Đồng ý duyệt.'),
    (910007, 910007, 910012, 910004, 'APPROVED', 910219, TIMESTAMPADD(MINUTE, -83520, v_day), 910002, TIMESTAMPADD(MINUTE, -82080, v_day), 'Đồng ý duyệt.'),
    (910008, 910008, 910013, 910004, 'APPROVED', 910222, TIMESTAMPADD(MINUTE, -83520, v_day), 910002, TIMESTAMPADD(MINUTE, -82080, v_day), 'Đồng ý duyệt.');

  -- registration_members
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

  -- reports
  INSERT INTO `reports` (`id`, `registration_id`, `version_no`, `storage_key`, `original_name`, `content_type`, `size_bytes`, `sha256`, `submitted_by`, `created_at`) VALUES
    (910004, 910004, 1, 'reports/DACN_Nhom04_BaoCao.pdf', 'DACN_Nhom04_BaoCao.pdf', 'application/pdf', 46732, '4240981d7980164a867f920b2b19094086fa74d4d522126569d676ecdbc9144d', 910210, TIMESTAMPADD(MINUTE, -5220, v_day)),
    (910005, 910005, 1, 'reports/DACN_Nhom05_BaoCao.pdf', 'DACN_Nhom05_BaoCao.pdf', 'application/pdf', 46732, '8838fc3bac9b8934047dce8f704c0e994ac3c490e47e46ca20291e9e08c6b5bd', 910213, TIMESTAMPADD(MINUTE, -5220, v_day)),
    (910006, 910006, 1, 'reports/KLTN_Nhom06_BaoCao.pdf', 'KLTN_Nhom06_BaoCao.pdf', 'application/pdf', 46732, '97fafef0a227d32de6ad84cbace9f833f8dd32bd89ab79cf6da1b57ae62d8a49', 910216, TIMESTAMPADD(MINUTE, -16740, v_day)),
    (910007, 910007, 1, 'reports/KLTN_Nhom07_BaoCao.pdf', 'KLTN_Nhom07_BaoCao.pdf', 'application/pdf', 46732, 'ee64e700117e2d8d720944f938f81381f330d08005fe6fa80c52eda7cb25331b', 910219, TIMESTAMPADD(MINUTE, -16740, v_day)),
    (910008, 910008, 1, 'reports/KLTN_Nhom08_BaoCao.pdf', 'KLTN_Nhom08_BaoCao.pdf', 'application/pdf', 46732, 'dd6a45306aa6ddf0b8819af724f6a6868cec45a0f176bc488085c76b77012adb', 910222, TIMESTAMPADD(MINUTE, -16740, v_day));

  -- councils
  INSERT INTO `councils` (`id`, `round_id`, `name`, `status`, `created_by`, `created_at`) VALUES
    (910001, 910003, 'Hội đồng 01 - Đánh giá Đồ án chuyên ngành', 'ACTIVE', 910002, TIMESTAMPADD(MINUTE, -7200, v_day)),
    (910002, 910004, 'Hội đồng 02 - Bảo vệ KLTN (Bộ môn CNPM)', 'ACTIVE', 910002, TIMESTAMPADD(MINUTE, -28800, v_day)),
    (910003, 910001, 'Hội đồng 03 - Dự kiến thành lập', 'DRAFT', 910002, TIMESTAMPADD(MINUTE, -7200, v_day));

  -- council_members
  INSERT INTO `council_members` (`council_id`, `lecturer_id`, `slot_no`) VALUES
    (910001, 910103, 1),
    (910001, 910104, 2),
    (910001, 910106, 3),
    (910002, 910103, 1),
    (910002, 910104, 2),
    (910002, 910106, 3),
    (910003, 910103, 1),
    (910003, 910104, 2);

  -- defenses
  INSERT INTO `defenses` (`id`, `registration_id`, `round_id`, `council_id`, `scheduled_at`, `location`, `status`) VALUES
    (910005, 910005, 910003, 910001, TIMESTAMPADD(MINUTE, 3420, v_day), 'Phòng A1-402, Tòa nhà Trung tâm', 'SCHEDULED'),
    (910006, 910006, 910004, 910002, TIMESTAMPADD(MINUTE, -960, v_day), 'Phòng A1-402, Tòa nhà Trung tâm', 'DONE'),
    (910007, 910007, 910004, 910002, TIMESTAMPADD(MINUTE, -840, v_day), 'Phòng A1-402, Tòa nhà Trung tâm', 'DONE'),
    (910008, 910008, 910004, 910002, TIMESTAMPADD(MINUTE, -720, v_day), 'Phòng A1-402, Tòa nhà Trung tâm', 'DONE');

  -- reviewer_assignments
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

  -- evaluations
  INSERT INTO `evaluations` (`id`, `assignment_id`, `score`, `comments`, `submitted_at`, `created_at`) VALUES
    (910051, 910051, 8.00, 'Sinh viên đáp ứng được yêu cầu cơ bản, báo cáo rõ ràng.', NULL, TIMESTAMPADD(MINUTE, -2340, v_day)),
    (910052, 910052, 7.50, 'Chức năng đầy đủ nhưng giao diện cần cải thiện thêm.', NULL, TIMESTAMPADD(MINUTE, -2340, v_day)),
    (910054, 910054, 8.00, 'Đồ án hoàn thành tốt, có đầu tư về kỹ thuật.', TIMESTAMPADD(MINUTE, -900, v_day), TIMESTAMPADD(MINUTE, -2340, v_day)),
    (910061, 910061, 8.00, 'Thuyết trình tự tin, trả lời được các câu hỏi phản biện.', TIMESTAMPADD(MINUTE, -900, v_day), TIMESTAMPADD(MINUTE, -900, v_day)),
    (910062, 910062, 7.00, 'Khối lượng công việc tương đối ít so với 3 thành viên.', TIMESTAMPADD(MINUTE, -900, v_day), TIMESTAMPADD(MINUTE, -900, v_day)),
    (910063, 910063, 9.00, 'Triển khai kiến trúc hệ thống chuẩn xác, chạy thực tế mượt mà.', TIMESTAMPADD(MINUTE, -900, v_day), TIMESTAMPADD(MINUTE, -900, v_day)),
    (910064, 910064, 6.00, 'Phần tối ưu cơ sở dữ liệu chưa tốt.', TIMESTAMPADD(MINUTE, -3720, v_day), TIMESTAMPADD(MINUTE, -5220, v_day)),
    (910071, 910071, 8.50, 'Đề tài có tính sáng tạo cao.', TIMESTAMPADD(MINUTE, -780, v_day), TIMESTAMPADD(MINUTE, -780, v_day)),
    (910072, 910072, 8.00, 'Chức năng cốt lõi hoạt động ổn định.', TIMESTAMPADD(MINUTE, -780, v_day), TIMESTAMPADD(MINUTE, -780, v_day)),
    (910073, 910073, 9.00, 'Kỹ năng làm việc nhóm và thiết kế kiến trúc tốt.', TIMESTAMPADD(MINUTE, -780, v_day), TIMESTAMPADD(MINUTE, -780, v_day)),
    (910074, 910074, 8.50, 'Đánh giá chung là xuất sắc, xứng đáng điểm cao.', TIMESTAMPADD(MINUTE, -3720, v_day), TIMESTAMPADD(MINUTE, -5220, v_day)),
    (910081, 910081, 9.00, 'Bảo vệ thành công, đồ án có khả năng ứng dụng thương mại.', TIMESTAMPADD(MINUTE, -660, v_day), TIMESTAMPADD(MINUTE, -660, v_day)),
    (910082, 910082, 8.00, 'Nắm vững kiến thức chuyên môn.', TIMESTAMPADD(MINUTE, -660, v_day), TIMESTAMPADD(MINUTE, -660, v_day)),
    (910083, 910083, 8.50, 'Hoàn thành tốt các mục tiêu đã đề ra.', TIMESTAMPADD(MINUTE, -660, v_day), TIMESTAMPADD(MINUTE, -660, v_day)),
    (910084, 910084, 8.50, 'Kết quả nghiên cứu thực nghiệm đáng tin cậy.', TIMESTAMPADD(MINUTE, -3720, v_day), TIMESTAMPADD(MINUTE, -5220, v_day));

  -- results
  INSERT INTO `results` (`id`, `registration_id`, `final_score`, `component_count`, `conclusion`, `confirmed_by`, `confirmed_at`, `published_by`, `published_at`) VALUES
    (910007, 910007, 8.50, 4, 'Hội đồng nhất trí đánh giá loại Giỏi, bảo vệ thành công.', 910103, TIMESTAMPADD(MINUTE, -750, v_day), NULL, NULL),
    (910008, 910008, 8.50, 4, 'Bảo vệ thành công xuất sắc, đề xuất đưa vào sử dụng thực tế.', 910103, TIMESTAMPADD(MINUTE, -630, v_day), 910002, TIMESTAMPADD(MINUTE, -540, v_day));

  -- announcements
  INSERT INTO `announcements` (`id`, `title`, `content`, `author_id`, `published_at`, `created_at`) VALUES
    (910001, 'Thông báo mở cổng đăng ký đề tài Đồ án môn học HK1', 'Phòng đào tạo thông báo sinh viên khóa 22 tiến hành đăng nhập và đăng ký đề tài Đồ án môn học theo đúng thời gian quy định.', 910001, TIMESTAMPADD(MINUTE, -2400, v_day), TIMESTAMPADD(MINUTE, -3840, v_day)),
    (910002, 'Hướng dẫn nộp quyển báo cáo Khóa luận tốt nghiệp', 'Các nhóm sinh viên lưu ý định dạng file nộp phải là PDF, dung lượng không quá 50MB và đặt tên theo chuẩn mã nhóm.', 910001, TIMESTAMPADD(MINUTE, -2340, v_day), TIMESTAMPADD(MINUTE, -3780, v_day)),
    (910003, 'Dự kiến danh sách hội đồng bảo vệ KLTN', 'Khoa CNTT thông báo dự thảo danh sách hội đồng bảo vệ KLTN đợt 1. Sinh viên phản hồi nếu có sai sót.', 910001, NULL, TIMESTAMPADD(MINUTE, -3720, v_day));

  -- audit_logs
  INSERT INTO `audit_logs` (`id`, `actor_id`, `action`, `entity_type`, `entity_id`, `details`, `created_at`) VALUES
    (910001, 910001, 'SYSTEM_INIT', 'SYSTEM', 910000, 'Hệ thống tự động khởi tạo dữ liệu mẫu ban đầu.', TIMESTAMPADD(MINUTE, 0, v_day)),
    (910002, 910103, 'RESULT_CONFIRMED', 'RESULT', 910007, 'Xác nhận điểm hội đồng cho nhóm sinh viên bảo vệ.', TIMESTAMPADD(MINUTE, -750, v_day)),
    (910003, 910103, 'RESULT_CONFIRMED', 'RESULT', 910008, 'Xác nhận điểm hội đồng cho nhóm sinh viên bảo vệ.', TIMESTAMPADD(MINUTE, -630, v_day)),
    (910004, 910002, 'RESULT_PUBLISHED', 'RESULT', 910008, 'Trưởng khoa duyệt và công bố điểm chính thức.', TIMESTAMPADD(MINUTE, -540, v_day));

  -- email_outbox
  INSERT INTO `email_outbox` (`id`, `result_id`, `student_id`, `recipient_email`, `subject`, `body`, `status`, `attempts`, `next_attempt_at`, `locked_at`, `sent_at`, `last_error`, `created_at`) VALUES
    (910022, 910008, 910222, '22110022@student.hcmute.edu.vn', '[Phòng Đào Tạo] Kết quả bảo vệ Khóa luận tốt nghiệp', 'Chào bạn, Khoa CNTT thông báo kết quả bảo vệ Khóa luận tốt nghiệp của bạn đã được cập nhật lên hệ thống. Vui lòng đăng nhập để xem chi tiết.', 'FAILED', 5, NULL, NULL, NULL, 'Mail server connection timeout.', TIMESTAMPADD(MINUTE, -540, v_day)),
    (910023, 910008, 910223, '22110023@student.hcmute.edu.vn', '[Phòng Đào Tạo] Kết quả bảo vệ Khóa luận tốt nghiệp', 'Chào bạn, Khoa CNTT thông báo kết quả bảo vệ Khóa luận tốt nghiệp của bạn đã được cập nhật lên hệ thống. Vui lòng đăng nhập để xem chi tiết.', 'FAILED', 5, NULL, NULL, NULL, 'Mail server connection timeout.', TIMESTAMPADD(MINUTE, -540, v_day)),
    (910024, 910008, 910224, '22110024@student.hcmute.edu.vn', '[Phòng Đào Tạo] Kết quả bảo vệ Khóa luận tốt nghiệp', 'Chào bạn, Khoa CNTT thông báo kết quả bảo vệ Khóa luận tốt nghiệp của bạn đã được cập nhật lên hệ thống. Vui lòng đăng nhập để xem chi tiết.', 'FAILED', 5, NULL, NULL, NULL, 'Mail server connection timeout.', TIMESTAMPADD(MINUTE, -540, v_day));

  -- Kiểm chứng hậu điều kiện quan trọng trước commit.
  IF EXISTS (
    SELECT 1 FROM reviewer_assignments a
    JOIN topic_registrations tr ON tr.id=a.registration_id
    JOIN topic_supervisors s ON s.topic_id=tr.topic_id AND s.lecturer_id=a.lecturer_id
    WHERE a.id BETWEEN 910000 AND 910999
  ) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loi phan cong: GVHD bi phan cong cham cheo.';
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
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loi du lieu: Diem ket qua khong khop voi phieu cham.';
  END IF;
  
  COMMIT;
  
  SELECT 'KHOI TAO DU LIEU THANH CONG' AS ket_qua,
         DATABASE() AS database_name, @@session.time_zone AS time_zone,
         v_day AS ngay_hien_hanh;
END$$
DELIMITER ;

CALL `seed_hcmute_20261021`();
DROP PROCEDURE `seed_hcmute_20261021`;

-- Kiểm tra danh sách tài khoản
SELECT u.username, u.full_name, u.email,
       GROUP_CONCAT(r.code ORDER BY r.code SEPARATOR ', ') AS roles
FROM users u JOIN user_roles ur ON ur.user_id=u.id JOIN roles r ON r.id=ur.role_id
WHERE u.id BETWEEN 910000 AND 910999
GROUP BY u.id, u.username, u.full_name, u.email ORDER BY u.id;

-- Kiểm tra danh sách đăng ký đề tài
SELECT g.name AS ten_nhom, tr.id AS ma_dang_ky, tr.status AS trang_thai_dang_ky,
       t.title AS ten_de_tai, r.final_score AS diem_tong_ket, r.published_at AS thoi_gian_cong_bo
FROM topic_registrations tr JOIN student_groups g ON g.id=tr.group_id
JOIN topics t ON t.id=tr.topic_id LEFT JOIN results r ON r.registration_id=tr.id
WHERE tr.id BETWEEN 910000 AND 910999 ORDER BY tr.id;
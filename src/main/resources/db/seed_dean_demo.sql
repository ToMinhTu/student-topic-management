-- Local development/test data only. Run after schema_mysql.sql.
-- Login: dean.demo
-- Temporary password: DeanTest-2026-Temp!
-- Change the password after first login; do not use this account in production.

USE student_topic_management;

START TRANSACTION;

INSERT INTO roles (code)
SELECT 'DEAN'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE code = 'DEAN'
);

INSERT INTO departments (name, description)
SELECT 'Bộ môn thử nghiệm DEAN', 'Bộ môn mẫu cho tài khoản kiểm thử DEAN'
WHERE NOT EXISTS (
    SELECT 1 FROM departments WHERE name = 'Bộ môn thử nghiệm DEAN'
);

INSERT INTO users (username, password_hash, full_name, email, enabled)
SELECT
    'dean.demo',
    '$2a$12$T741.9iTOPP/sS.himUY3untROT1ct8cpGOfUsOE5AnUGFvY.6rwq',
    'Tài khoản DEAN thử nghiệm',
    'dean.demo@example.test',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM users
    WHERE username = 'dean.demo' OR email = 'dean.demo@example.test'
);

INSERT INTO lecturers (user_id, lecturer_code, department_id)
SELECT u.id, 'DEAN-DEMO-2026-001', d.id
FROM users u
JOIN departments d
    ON d.name = 'Bộ môn thử nghiệm DEAN'
WHERE u.username = 'dean.demo'
  AND u.email = 'dean.demo@example.test'
  AND u.password_hash = '$2a$12$T741.9iTOPP/sS.himUY3untROT1ct8cpGOfUsOE5AnUGFvY.6rwq'
  AND NOT EXISTS (
      SELECT 1 FROM lecturers WHERE user_id = u.id
  )
  AND NOT EXISTS (
      SELECT 1 FROM lecturers WHERE lecturer_code = 'DEAN-DEMO-2026-001'
  );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'DEAN'
WHERE u.username = 'dean.demo'
  AND u.email = 'dean.demo@example.test'
  AND u.password_hash = '$2a$12$T741.9iTOPP/sS.himUY3untROT1ct8cpGOfUsOE5AnUGFvY.6rwq'
  AND EXISTS (
      SELECT 1 FROM lecturers l WHERE l.user_id = u.id
  )
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

COMMIT;

SELECT
    u.username,
    r.code AS role,
    d.name AS department,
    CASE
        WHEN u.password_hash = '$2a$12$T741.9iTOPP/sS.himUY3untROT1ct8cpGOfUsOE5AnUGFvY.6rwq'
          AND r.code = 'DEAN'
          AND l.user_id IS NOT NULL
            THEN 'READY'
        ELSE 'CHECK_EXISTING_DATA_OR_SETUP'
    END AS seed_status
FROM users u
LEFT JOIN user_roles ur ON ur.user_id = u.id
LEFT JOIN roles r ON r.id = ur.role_id AND r.code = 'DEAN'
LEFT JOIN lecturers l ON l.user_id = u.id
LEFT JOIN departments d ON d.id = l.department_id
WHERE u.username = 'dean.demo';

-- 员工表恢复校区字段
USE study_room;

ALTER TABLE employee ADD COLUMN campus_id BIGINT NULL AFTER phone;
UPDATE employee SET campus_id = (SELECT id FROM campus ORDER BY id LIMIT 1) WHERE campus_id IS NULL;
ALTER TABLE employee MODIFY campus_id BIGINT NOT NULL;
ALTER TABLE employee ADD INDEX idx_employee_campus (campus_id);

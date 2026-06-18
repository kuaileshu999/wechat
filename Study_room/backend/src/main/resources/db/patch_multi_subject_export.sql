-- 课程多学科、订单多主讲老师、消课学科、导出权限（可重复执行）
SET @db = DATABASE();

-- 1. 课程-学科关联表
CREATE TABLE IF NOT EXISTS course_subject (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_course_subject (course_id, subject_id),
    KEY idx_course_subject_course (course_id)
);

-- 从 course.subject_id 迁移
INSERT IGNORE INTO course_subject (course_id, subject_id)
SELECT c.id, c.subject_id FROM course c WHERE c.subject_id IS NOT NULL;

-- 2. 订单-主讲老师关联表
CREATE TABLE IF NOT EXISTS order_teacher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_teacher (order_id, teacher_id),
    KEY idx_order_teacher_order (order_id)
);

INSERT IGNORE INTO order_teacher (order_id, teacher_id)
SELECT o.id, o.teacher_id FROM orders o WHERE o.teacher_id IS NOT NULL;

-- 3. 消课记录增加学科
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'consumption_record' AND COLUMN_NAME = 'subject_id') = 0,
    'ALTER TABLE consumption_record ADD COLUMN subject_id BIGINT NULL AFTER course_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 已有消课记录默认取课程第一个学科
UPDATE consumption_record cr
JOIN course_subject cs ON cs.course_id = cr.course_id
SET cr.subject_id = cs.subject_id
WHERE cr.subject_id IS NULL;

UPDATE consumption_record cr
JOIN course c ON c.id = cr.course_id
SET cr.subject_id = c.subject_id
WHERE cr.subject_id IS NULL AND c.subject_id IS NOT NULL;

-- 4. 导出权限
INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '导出订单', 'order:export', 2, NULL, 3 FROM sys_permission p WHERE p.code = 'order' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '导出财务', 'finance:export', 2, NULL, 1 FROM sys_permission p WHERE p.code = 'finance' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '导出消课', 'consumption:export', 2, NULL, 1 FROM sys_permission p WHERE p.code = 'consumption' LIMIT 1;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE code IN ('order:export', 'finance:export', 'consumption:export');

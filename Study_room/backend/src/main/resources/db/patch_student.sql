-- 学员手机号唯一约束与编辑权限（可重复执行）
SET @db = DATABASE();

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '编辑学员', 'student:update', 2, NULL, 3
FROM sys_permission p WHERE p.code = 'student' LIMIT 1;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE code = 'student:update';

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'student' AND INDEX_NAME = 'uk_student_phone') = 0,
    'ALTER TABLE student ADD UNIQUE INDEX uk_student_phone (phone)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 员工表校区字段（可重复执行）
SET @db = DATABASE();

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'employee' AND COLUMN_NAME = 'campus_id') = 0,
    'ALTER TABLE employee ADD COLUMN campus_id BIGINT NULL AFTER phone',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE employee SET campus_id = (SELECT id FROM campus ORDER BY id LIMIT 1) WHERE campus_id IS NULL;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'employee' AND COLUMN_NAME = 'campus_id'
       AND IS_NULLABLE = 'YES') > 0,
    'ALTER TABLE employee MODIFY campus_id BIGINT NOT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'employee' AND INDEX_NAME = 'idx_employee_campus') = 0,
    'ALTER TABLE employee ADD INDEX idx_employee_campus (campus_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

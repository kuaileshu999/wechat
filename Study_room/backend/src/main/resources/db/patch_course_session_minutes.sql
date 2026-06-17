-- 课程每次消课时长（分钟），可重复执行
SET @db = DATABASE();

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'course' AND COLUMN_NAME = 'session_minutes') = 0,
    'ALTER TABLE course ADD COLUMN session_minutes INT NOT NULL DEFAULT 60 COMMENT ''每次消课时长(分钟)'' AFTER unit_hours',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 排课课程类型改为关联 course_type 表（可重复执行）
USE study_room;

-- 若已是新结构则跳过
SET @has_course_type_id := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'study_room'
      AND TABLE_NAME = 'lesson_schedule'
      AND COLUMN_NAME = 'course_type_id'
);

SET @has_schedule_type := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'study_room'
      AND TABLE_NAME = 'lesson_schedule'
      AND COLUMN_NAME = 'schedule_type'
);

SET @sql := IF(@has_course_type_id = 0 AND @has_schedule_type = 1,
    'DELETE FROM lesson_schedule',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_course_type_id = 0 AND @has_schedule_type = 1,
    'ALTER TABLE lesson_schedule ADD COLUMN course_type_id BIGINT NULL AFTER teacher_id',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_schedule_type = 1,
    'ALTER TABLE lesson_schedule DROP COLUMN schedule_type',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_course_type_id = 0 OR @has_schedule_type = 1,
    'ALTER TABLE lesson_schedule MODIFY course_type_id BIGINT NOT NULL COMMENT ''课程类型''',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_index := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'study_room'
      AND TABLE_NAME = 'lesson_schedule'
      AND INDEX_NAME = 'idx_schedule_course_type'
);

SET @sql := IF(@has_index = 0,
    'ALTER TABLE lesson_schedule ADD INDEX idx_schedule_course_type (course_type_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 订单上课老师 + 消课上课时间与老师（可重复执行）

SET @db = DATABASE();

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'teacher_id') = 0,
    'ALTER TABLE orders ADD COLUMN teacher_id BIGINT NULL COMMENT ''上课老师'' AFTER salesperson_id',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'consumption_record' AND COLUMN_NAME = 'teacher_id') = 0,
    'ALTER TABLE consumption_record ADD COLUMN teacher_id BIGINT NULL COMMENT ''上课老师'' AFTER course_id',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'consumption_record' AND COLUMN_NAME = 'class_time') = 0,
    'ALTER TABLE consumption_record ADD COLUMN class_time DATETIME NULL COMMENT ''上课时间'' AFTER teacher_id',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE consumption_record SET class_time = created_at WHERE class_time IS NULL;

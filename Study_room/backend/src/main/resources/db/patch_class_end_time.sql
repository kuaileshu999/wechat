-- 消课结课时间，可重复执行
SET @db = DATABASE();

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'consumption_record' AND COLUMN_NAME = 'class_end_time') = 0,
    'ALTER TABLE consumption_record ADD COLUMN class_end_time DATETIME NULL COMMENT ''结课时间'' AFTER class_time',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

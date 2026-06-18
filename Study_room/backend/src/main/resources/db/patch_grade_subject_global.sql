-- 年级/学科改为全校区共用（可重复执行）
SET @db = DATABASE();

-- ========== 学科：合并重复并去掉 campus_id ==========
SET @has_subject_campus = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'subject_dict' AND COLUMN_NAME = 'campus_id'
);

SET @sql = IF(@has_subject_campus > 0,
    'UPDATE course c
     JOIN subject_dict sd ON c.subject_id = sd.id
     JOIN (
         SELECT name, MIN(id) AS keep_id
         FROM subject_dict
         GROUP BY name
     ) k ON k.name = sd.name
     SET c.subject_id = k.keep_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@has_subject_campus > 0,
    'DELETE sd FROM subject_dict sd
     LEFT JOIN (
         SELECT name, MIN(id) AS keep_id
         FROM subject_dict
         GROUP BY name
     ) k ON sd.id = k.keep_id
     WHERE k.keep_id IS NULL OR sd.id <> k.keep_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@has_subject_campus > 0,
    'ALTER TABLE subject_dict DROP INDEX uk_subject_dict_campus_name',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@has_subject_campus > 0,
    'ALTER TABLE subject_dict DROP INDEX idx_subject_dict_campus',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@has_subject_campus > 0,
    'ALTER TABLE subject_dict DROP COLUMN campus_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'subject_dict' AND INDEX_NAME = 'uk_subject_dict_name') = 0,
    'ALTER TABLE subject_dict ADD UNIQUE KEY uk_subject_dict_name (name)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== 年级：合并重复并去掉 campus_id ==========
SET @has_grade_campus = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'grade' AND COLUMN_NAME = 'campus_id'
);

SET @sql = IF(@has_grade_campus > 0,
    'UPDATE course c
     JOIN grade g ON c.grade_id = g.id
     JOIN (
         SELECT name, MIN(id) AS keep_id
         FROM grade
         GROUP BY name
     ) k ON k.name = g.name
     SET c.grade_id = k.keep_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@has_grade_campus > 0,
    'DELETE g FROM grade g
     LEFT JOIN (
         SELECT name, MIN(id) AS keep_id
         FROM grade
         GROUP BY name
     ) k ON g.id = k.keep_id
     WHERE k.keep_id IS NULL OR g.id <> k.keep_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@has_grade_campus > 0,
    'ALTER TABLE grade DROP INDEX uk_grade_campus_name',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@has_grade_campus > 0,
    'ALTER TABLE grade DROP INDEX idx_grade_campus',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@has_grade_campus > 0,
    'ALTER TABLE grade DROP COLUMN campus_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'grade' AND INDEX_NAME = 'uk_grade_name') = 0,
    'ALTER TABLE grade ADD UNIQUE KEY uk_grade_name (name)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

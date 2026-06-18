-- 订单银联、年级/学科管理、课程字段迁移（可重复执行）
SET @db = DATABASE();

-- 1. 订单：银联订单号
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'union_pay_order_no') = 0,
    'ALTER TABLE orders ADD COLUMN union_pay_order_no VARCHAR(64) NULL AFTER payment_date',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 年级表
CREATE TABLE IF NOT EXISTS grade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_grade_name (name)
);

-- 3. 学科表
CREATE TABLE IF NOT EXISTS subject_dict (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_subject_dict_name (name)
);

-- 4. 初始化默认学科（全校区共用）
INSERT IGNORE INTO subject_dict (name, status) VALUES
('语文', 1), ('数学', 1), ('英语', 1), ('物理', 1), ('历史', 1), ('地理', 1);

-- 5. 初始化默认年级（全校区共用）
INSERT IGNORE INTO grade (name, status) VALUES
('一年级', 1), ('二年级', 1), ('三年级', 1), ('四年级', 1), ('五年级', 1), ('六年级', 1),
('初一', 1), ('初二', 1), ('初三', 1), ('高一', 1), ('高二', 1), ('高三', 1);

-- 6. 课程表增加 subject_id / grade_id
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'course' AND COLUMN_NAME = 'subject_id') = 0,
    'ALTER TABLE course ADD COLUMN subject_id BIGINT NULL AFTER name, ADD COLUMN grade_id BIGINT NULL AFTER subject_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 7. 从旧 subject 枚举迁移到 subject_id（仅当旧列仍存在时）
SET @has_subject_col = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'course' AND COLUMN_NAME = 'subject'
);
SET @sql = IF(@has_subject_col > 0,
    'UPDATE course c
     JOIN subject_dict sd ON sd.campus_id = c.campus_id AND sd.name = CASE c.subject
         WHEN ''CHINESE'' THEN ''语文''
         WHEN ''MATH'' THEN ''数学''
         WHEN ''ENGLISH'' THEN ''英语''
         WHEN ''PHYSICS'' THEN ''物理''
         WHEN ''HISTORY'' THEN ''历史''
         WHEN ''GEOGRAPHY'' THEN ''地理''
         ELSE c.subject
     END
     SET c.subject_id = sd.id
     WHERE c.subject_id IS NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 8. 未匹配到的课程默认取第一个学科
UPDATE course c
SET c.subject_id = (SELECT MIN(id) FROM subject_dict)
WHERE c.subject_id IS NULL
  AND EXISTS (SELECT 1 FROM subject_dict);

-- 9. 默认年级取「一年级」或第一个年级
UPDATE course c
JOIN grade g ON g.name = '一年级'
SET c.grade_id = g.id
WHERE c.grade_id IS NULL;

UPDATE course c
SET c.grade_id = (SELECT MIN(id) FROM grade)
WHERE c.grade_id IS NULL
  AND EXISTS (SELECT 1 FROM grade);

-- 10. 删除旧 subject 列并设 NOT NULL
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'course' AND COLUMN_NAME = 'subject') > 0,
    'ALTER TABLE course DROP COLUMN subject',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'course' AND COLUMN_NAME = 'subject_id'
       AND IS_NULLABLE = 'YES') > 0,
    'ALTER TABLE course MODIFY subject_id BIGINT NOT NULL, MODIFY grade_id BIGINT NOT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 11. 菜单权限：学科管理、年级管理
INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order) VALUES
(0, '学科管理', 'subject', 1, '/subject', 4);

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建学科', 'subject:create', 2, NULL, 1 FROM sys_permission p WHERE p.code = 'subject' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order) VALUES
(0, '年级管理', 'grade', 1, '/grade', 5);

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建年级', 'grade:create', 2, NULL, 1 FROM sys_permission p WHERE p.code = 'grade' LIMIT 1;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE code IN ('subject', 'subject:create', 'grade', 'grade:create');

UPDATE sys_permission SET sort_order = 4 WHERE code = 'subject';
UPDATE sys_permission SET sort_order = 5 WHERE code = 'grade';
UPDATE sys_permission SET sort_order = 6 WHERE code = 'course-type';
UPDATE sys_permission SET sort_order = 7 WHERE code = 'course';
UPDATE sys_permission SET sort_order = 8 WHERE code = 'order';
UPDATE sys_permission SET sort_order = 9 WHERE code = 'consumption';
UPDATE sys_permission SET sort_order = 10 WHERE code = 'finance';
UPDATE sys_permission SET sort_order = 11 WHERE code = 'schedule';

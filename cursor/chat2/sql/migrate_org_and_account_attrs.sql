USE message_takeover;

CREATE TABLE IF NOT EXISTS org_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NULL,
    name VARCHAR(64) NOT NULL,
    dept_type VARCHAR(16) NOT NULL COMMENT 'COMPANY/DIVISION/GRADE/SUBJECT',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB;

ALTER TABLE tutor ADD COLUMN org_department_id BIGINT NULL AFTER phone;
ALTER TABLE wecom_account ADD COLUMN grade_level VARCHAR(32) NULL COMMENT '年级' AFTER wecom_user_id;
ALTER TABLE wecom_account ADD COLUMN subject VARCHAR(32) NULL COMMENT '学科' AFTER grade_level;

-- 组织架构（若已存在则跳过插入）
INSERT IGNORE INTO org_department (id, parent_id, name, dept_type, sort_order) VALUES
(1, NULL, '教育科技集团', 'COMPANY', 1),
(2, 1, '小学部', 'DIVISION', 1),
(3, 1, '初中部', 'DIVISION', 2),
(4, 1, '高中部', 'DIVISION', 3),
(5, 2, '一年级', 'GRADE', 1),
(6, 2, '二年级', 'GRADE', 2),
(7, 3, '初一', 'GRADE', 1),
(8, 3, '初二', 'GRADE', 2),
(9, 4, '高一', 'GRADE', 1),
(10, 4, '高二', 'GRADE', 2),
(11, 5, '语文', 'SUBJECT', 1),
(12, 5, '数学', 'SUBJECT', 2),
(13, 6, '语文', 'SUBJECT', 1),
(14, 6, '数学', 'SUBJECT', 2),
(15, 7, '语文', 'SUBJECT', 1),
(16, 7, '数学', 'SUBJECT', 2),
(17, 8, '语文', 'SUBJECT', 1),
(18, 8, '数学', 'SUBJECT', 2),
(19, 9, '语文', 'SUBJECT', 1),
(20, 9, '数学', 'SUBJECT', 2),
(21, 10, '语文', 'SUBJECT', 1),
(22, 10, '数学', 'SUBJECT', 2);

-- 辅导归属组织（非接管辅导 1-45）
UPDATE tutor SET org_department_id = 11 + ((id - 1) % 12) WHERE is_takeover_role = 0 AND id <= 45;

-- 账号年级学科：A账号偏语文年级，B账号偏数学年级
UPDATE wecom_account wa
JOIN tutor t ON wa.tutor_id = t.id
SET
  wa.grade_level = CASE
    WHEN t.org_department_id IN (5, 11, 12) THEN '一年级'
    WHEN t.org_department_id IN (6, 13, 14) THEN '二年级'
    WHEN t.org_department_id IN (7, 15, 16) THEN '初一'
    WHEN t.org_department_id IN (8, 17, 18) THEN '初二'
    WHEN t.org_department_id IN (9, 19, 20) THEN '高一'
    WHEN t.org_department_id IN (10, 21, 22) THEN '高二'
    ELSE '一年级'
  END,
  wa.subject = CASE WHEN wa.account_name LIKE '%-企微A' OR wa.id % 2 = 1 THEN '语文' ELSE '数学' END
WHERE wa.grade_level IS NULL OR wa.grade_level = '';

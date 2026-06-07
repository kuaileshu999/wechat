-- 账号级托管分配迁移（在已有 schema 上执行，可重复运行）
USE wechat;

CREATE TABLE IF NOT EXISTS hosting_config_account (
  id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  hosting_config_id BIGINT       NOT NULL COMMENT '配置ID',
  tutor_id          BIGINT       NOT NULL COMMENT '辅导老师ID',
  tutor_account_id  BIGINT       NOT NULL COMMENT '企微账号ID',
  skip_reason       VARCHAR(128) DEFAULT NULL COMMENT '跳过原因',
  status            TINYINT      NOT NULL DEFAULT 1 COMMENT '0-跳过 1-待生效 2-已生效',
  PRIMARY KEY (id),
  UNIQUE KEY uk_config_account (hosting_config_id, tutor_account_id),
  KEY idx_account (tutor_account_id),
  CONSTRAINT fk_hca_config FOREIGN KEY (hosting_config_id) REFERENCES hosting_config (id),
  CONSTRAINT fk_hca_tutor FOREIGN KEY (tutor_id) REFERENCES tutor (id),
  CONSTRAINT fk_hca_account FOREIGN KEY (tutor_account_id) REFERENCES tutor_wechat_account (id)
) ENGINE=InnoDB COMMENT='接管配置-企微账号关联';

-- 添加 tutor_account_id 列（若不存在）
SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hosting_assignment' AND COLUMN_NAME = 'tutor_account_id'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE hosting_assignment ADD COLUMN tutor_account_id BIGINT DEFAULT NULL COMMENT ''企微账号ID'' AFTER tutor_id',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加索引（若不存在）
SET @idx_exists = (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hosting_assignment' AND INDEX_NAME = 'idx_account_status'
);
SET @sql = IF(@idx_exists = 0,
  'ALTER TABLE hosting_assignment ADD KEY idx_account_status (tutor_account_id, status)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加外键（若不存在）
SET @fk_exists = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hosting_assignment' AND CONSTRAINT_NAME = 'fk_assign_account'
);
SET @sql = IF(@fk_exists = 0,
  'ALTER TABLE hosting_assignment ADD CONSTRAINT fk_assign_account FOREIGN KEY (tutor_account_id) REFERENCES tutor_wechat_account (id)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 将历史 tutor 级托管回填为首个企微账号
UPDATE hosting_assignment ha
INNER JOIN (
  SELECT tutor_id, MIN(id) AS first_account_id
  FROM tutor_wechat_account
  WHERE status = 1
  GROUP BY tutor_id
) fa ON fa.tutor_id = ha.tutor_id
SET ha.tutor_account_id = fa.first_account_id
WHERE ha.tutor_account_id IS NULL;

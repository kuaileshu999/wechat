CREATE DATABASE IF NOT EXISTS message_takeover DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE message_takeover;

DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS conversation;
DROP TABLE IF EXISTS takeover_assignment;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS wecom_account;
DROP TABLE IF EXISTS tutor;
DROP TABLE IF EXISTS org_department;

CREATE TABLE org_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NULL,
    name VARCHAR(64) NOT NULL,
    dept_type VARCHAR(16) NOT NULL COMMENT 'COMPANY/DIVISION/GRADE/SUBJECT',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB;

CREATE TABLE tutor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    phone VARCHAR(32),
    org_department_id BIGINT NULL,
    is_takeover_role TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为接管辅导',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE wecom_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tutor_id BIGINT NOT NULL,
    account_name VARCHAR(128) NOT NULL,
    wecom_user_id VARCHAR(64),
    grade_level VARCHAR(32) NULL COMMENT '年级',
    subject VARCHAR(32) NULL COMMENT '学科',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tutor (tutor_id),
    INDEX idx_grade_subject (grade_level, subject),
    CONSTRAINT fk_account_tutor FOREIGN KEY (tutor_id) REFERENCES tutor(id)
) ENGINE=InnoDB;

CREATE TABLE student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wecom_account_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    external_user_id VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_account (wecom_account_id),
    CONSTRAINT fk_student_account FOREIGN KEY (wecom_account_id) REFERENCES wecom_account(id)
) ENGINE=InnoDB;

CREATE TABLE takeover_assignment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_tutor_id BIGINT NOT NULL COMMENT '被接管的辅导',
    wecom_account_id BIGINT NULL COMMENT 'NULL表示该辅导全部企微账号',
    takeover_tutor_id BIGINT NOT NULL COMMENT '接管人',
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_source (source_tutor_id),
    INDEX idx_takeover (takeover_tutor_id),
    CONSTRAINT fk_assign_source FOREIGN KEY (source_tutor_id) REFERENCES tutor(id),
    CONSTRAINT fk_assign_account FOREIGN KEY (wecom_account_id) REFERENCES wecom_account(id),
    CONSTRAINT fk_assign_takeover FOREIGN KEY (takeover_tutor_id) REFERENCES tutor(id)
) ENGINE=InnoDB;

CREATE TABLE conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wecom_account_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    chat_type VARCHAR(16) NOT NULL COMMENT 'PRIVATE/GROUP',
    group_name VARCHAR(128),
    category VARCHAR(32) NOT NULL COMMENT 'UNDERTAKING/CONVERSION',
    last_message_at DATETIME,
    last_message_preview VARCHAR(512),
    unread_count INT NOT NULL DEFAULT 0,
    pending_reply TINYINT(1) NOT NULL DEFAULT 0 COMMENT '最后一条为学生消息待回复',
    pending_reply_count INT NOT NULL DEFAULT 0 COMMENT '待回复消息条数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_last_msg (last_message_at),
    INDEX idx_filter (chat_type, category, last_message_at),
    INDEX idx_pending (category, chat_type, pending_reply),
    CONSTRAINT fk_conv_account FOREIGN KEY (wecom_account_id) REFERENCES wecom_account(id),
    CONSTRAINT fk_conv_student FOREIGN KEY (student_id) REFERENCES student(id)
) ENGINE=InnoDB;

CREATE TABLE chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    direction VARCHAR(16) NOT NULL COMMENT 'INCOMING/OUTGOING',
    sender_name VARCHAR(64),
    content TEXT NOT NULL,
    sent_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conv_time (conversation_id, sent_at),
    CONSTRAINT fk_msg_conv FOREIGN KEY (conversation_id) REFERENCES conversation(id)
) ENGINE=InnoDB;

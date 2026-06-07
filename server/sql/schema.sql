-- ============================================================
-- 消息托管系统 MySQL 数据库脚本（已确认）
-- 字符集: utf8mb4 | 引擎: InnoDB
-- ============================================================

CREATE DATABASE IF NOT EXISTS wechat
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE wechat;

-- ------------------------------------------------------------
-- 1. 教研组
-- ------------------------------------------------------------
CREATE TABLE teaching_group (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  name          VARCHAR(64)  NOT NULL COMMENT '教研组名称',
  sort_order    INT          NOT NULL DEFAULT 0 COMMENT '排序',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_name (name)
) ENGINE=InnoDB COMMENT='教研组';

-- ------------------------------------------------------------
-- 2. 系统用户（辅导老师、接管者、管理员统一登录）
-- ------------------------------------------------------------
CREATE TABLE sys_user (
  id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  username      VARCHAR(64)   NOT NULL COMMENT '登录名',
  password_hash VARCHAR(128)  NOT NULL COMMENT '密码哈希',
  real_name     VARCHAR(32)   NOT NULL COMMENT '真实姓名',
  phone         VARCHAR(20)   DEFAULT NULL COMMENT '手机号',
  avatar_url    VARCHAR(512)  DEFAULT NULL COMMENT '头像URL',
  role          TINYINT       NOT NULL DEFAULT 2 COMMENT '1-管理员 2-辅导老师 3-接管者',
  status        TINYINT       NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
  last_login_at DATETIME      DEFAULT NULL COMMENT '最后登录时间',
  created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  KEY idx_phone (phone),
  KEY idx_role_status (role, status)
) ENGINE=InnoDB COMMENT='系统用户';

-- ------------------------------------------------------------
-- 3. 辅导老师
-- ------------------------------------------------------------
CREATE TABLE tutor (
  id                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id           BIGINT      NOT NULL COMMENT '关联 sys_user.id',
  teaching_group_id BIGINT      NOT NULL COMMENT '所属教研组',
  employee_no       VARCHAR(32) DEFAULT NULL COMMENT '工号',
  status            TINYINT     NOT NULL DEFAULT 1 COMMENT '0-离职 1-在职',
  created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_id (user_id),
  KEY idx_teaching_group (teaching_group_id),
  CONSTRAINT fk_tutor_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_tutor_group FOREIGN KEY (teaching_group_id) REFERENCES teaching_group (id)
) ENGINE=InnoDB COMMENT='辅导老师';

-- ------------------------------------------------------------
-- 4. 接管者
-- ------------------------------------------------------------
CREATE TABLE takeover_manager (
  id              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id         BIGINT   NOT NULL COMMENT '关联 sys_user.id',
  max_tutor_count INT      NOT NULL DEFAULT 10 COMMENT '最大可接管辅导老师数',
  status          TINYINT  NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_id (user_id),
  CONSTRAINT fk_manager_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB COMMENT='接管者';

-- ------------------------------------------------------------
-- 5. 辅导企微账号（随辅导老师一并托管）
-- ------------------------------------------------------------
CREATE TABLE tutor_wechat_account (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  tutor_id      BIGINT       NOT NULL COMMENT '所属辅导老师',
  account_name  VARCHAR(64)  NOT NULL COMMENT '账号展示名',
  subject       VARCHAR(32)  DEFAULT NULL COMMENT '科目',
  grade         VARCHAR(32)  DEFAULT NULL COMMENT '年级',
  wechat_userid VARCHAR(64)  NOT NULL COMMENT '企微成员userid',
  corp_id       VARCHAR(64)  NOT NULL COMMENT '企业ID',
  agent_id      VARCHAR(64)  DEFAULT NULL COMMENT '应用agentId',
  student_count INT          NOT NULL DEFAULT 0 COMMENT '学生数(冗余)',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '0-停用 1-正常',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_wechat_userid (wechat_userid),
  KEY idx_tutor_id (tutor_id),
  KEY idx_subject_grade (subject, grade),
  CONSTRAINT fk_account_tutor FOREIGN KEY (tutor_id) REFERENCES tutor (id)
) ENGINE=InnoDB COMMENT='辅导企微账号';

-- ------------------------------------------------------------
-- 6. 学生
-- ------------------------------------------------------------
CREATE TABLE student (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  nickname        VARCHAR(64)  NOT NULL COMMENT '昵称',
  avatar_url      VARCHAR(512) DEFAULT NULL COMMENT '头像',
  external_userid VARCHAR(64)  NOT NULL COMMENT '企微外部联系人ID',
  grade           VARCHAR(32)  DEFAULT NULL COMMENT '年级',
  status          TINYINT      NOT NULL DEFAULT 1 COMMENT '0-无效 1-正常',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_external_userid (external_userid)
) ENGINE=InnoDB COMMENT='学生';

-- ------------------------------------------------------------
-- 7. 学生-辅导账号关系
-- 阶段: 1-转化期 2-承接期 3-已结课
-- ------------------------------------------------------------
CREATE TABLE student_tutor_relation (
  id               BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  student_id       BIGINT      NOT NULL COMMENT '学生ID',
  tutor_account_id BIGINT      NOT NULL COMMENT '辅导企微账号ID',
  subject          VARCHAR(32) DEFAULT NULL COMMENT '科目',
  grade            VARCHAR(32) DEFAULT NULL COMMENT '年级',
  stage            TINYINT     NOT NULL DEFAULT 1 COMMENT '1-转化期 2-承接期 3-已结课',
  status           TINYINT     NOT NULL DEFAULT 1 COMMENT '0-结束 1-进行中',
  assigned_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_student_account (student_id, tutor_account_id),
  KEY idx_account_stage (tutor_account_id, stage),
  KEY idx_stage_status (stage, status),
  CONSTRAINT fk_str_student FOREIGN KEY (student_id) REFERENCES student (id),
  CONSTRAINT fk_str_account FOREIGN KEY (tutor_account_id) REFERENCES tutor_wechat_account (id)
) ENGINE=InnoDB COMMENT='学生辅导关系';

-- ------------------------------------------------------------
-- 8. 群聊
-- ------------------------------------------------------------
CREATE TABLE chat_group (
  id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  tutor_account_id BIGINT       NOT NULL COMMENT '所属辅导账号',
  group_name       VARCHAR(128) NOT NULL COMMENT '群名称',
  wechat_chat_id   VARCHAR(64)  NOT NULL COMMENT '企微群chat_id',
  member_count     INT          NOT NULL DEFAULT 0 COMMENT '成员数',
  status           TINYINT      NOT NULL DEFAULT 1 COMMENT '0-解散 1-正常',
  created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_wechat_chat_id (wechat_chat_id),
  KEY idx_account_id (tutor_account_id),
  CONSTRAINT fk_group_account FOREIGN KEY (tutor_account_id) REFERENCES tutor_wechat_account (id)
) ENGINE=InnoDB COMMENT='群聊';

-- ------------------------------------------------------------
-- 9. 群成员
-- ------------------------------------------------------------
CREATE TABLE chat_group_member (
  id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  group_id   BIGINT   NOT NULL COMMENT '群ID',
  student_id BIGINT   NOT NULL COMMENT '学生ID',
  joined_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_group_student (group_id, student_id),
  KEY idx_student_id (student_id),
  CONSTRAINT fk_member_group FOREIGN KEY (group_id) REFERENCES chat_group (id),
  CONSTRAINT fk_member_student FOREIGN KEY (student_id) REFERENCES student (id)
) ENGINE=InnoDB COMMENT='群成员';

-- ------------------------------------------------------------
-- 10. 会话（私聊 + 群聊，随辅导老师一并托管）
-- ------------------------------------------------------------
CREATE TABLE conversation (
  id                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  tutor_account_id     BIGINT       NOT NULL COMMENT '所属辅导账号',
  conv_type            TINYINT      NOT NULL COMMENT '1-私聊 2-群聊',
  student_id           BIGINT       DEFAULT NULL COMMENT '私聊学生(群聊为空)',
  group_id             BIGINT       DEFAULT NULL COMMENT '群聊(私聊为空)',
  last_message_id      BIGINT       DEFAULT NULL COMMENT '最后消息ID',
  last_message_at      DATETIME     DEFAULT NULL COMMENT '最后消息时间',
  last_message_preview VARCHAR(256) DEFAULT NULL COMMENT '列表摘要',
  stage                TINYINT      DEFAULT NULL COMMENT '1-转化期 2-承接期 3-已结课',
  status               TINYINT      NOT NULL DEFAULT 1 COMMENT '0-归档 1-活跃',
  created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_account_last_msg (tutor_account_id, last_message_at),
  KEY idx_account_stage (tutor_account_id, stage),
  KEY idx_student_id (student_id),
  KEY idx_group_id (group_id),
  CONSTRAINT fk_conv_account FOREIGN KEY (tutor_account_id) REFERENCES tutor_wechat_account (id),
  CONSTRAINT fk_conv_student FOREIGN KEY (student_id) REFERENCES student (id),
  CONSTRAINT fk_conv_group FOREIGN KEY (group_id) REFERENCES chat_group (id)
) ENGINE=InnoDB COMMENT='会话';

-- ------------------------------------------------------------
-- 11. 会话当前处理人（托管默认分配 + 单聊转接）
-- ------------------------------------------------------------
CREATE TABLE conversation_handler (
  id                    BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  conversation_id       BIGINT   NOT NULL COMMENT '会话ID',
  handler_user_id       BIGINT   NOT NULL COMMENT '当前处理人(sys_user.id)',
  handler_type          TINYINT  NOT NULL DEFAULT 1 COMMENT '1-接管者 2-原辅导老师',
  hosting_assignment_id BIGINT   DEFAULT NULL COMMENT '关联托管关系(转接后可为空)',
  assigned_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配/转接时间',
  created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_conversation (conversation_id),
  KEY idx_handler_user (handler_user_id),
  KEY idx_assignment (hosting_assignment_id),
  CONSTRAINT fk_handler_conv FOREIGN KEY (conversation_id) REFERENCES conversation (id),
  CONSTRAINT fk_handler_user FOREIGN KEY (handler_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB COMMENT='会话当前处理人';

-- ------------------------------------------------------------
-- 12. 消息（保留 1 个月，见 cleanup_message.sql）
-- ------------------------------------------------------------
CREATE TABLE message (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  conversation_id BIGINT       NOT NULL COMMENT '会话ID',
  msg_id          VARCHAR(64)  NOT NULL COMMENT '企微消息ID',
  sender_type     TINYINT      NOT NULL COMMENT '1-学生 2-老师/接管者 3-系统',
  sender_id       BIGINT       DEFAULT NULL COMMENT '发送者ID',
  content_type    TINYINT      NOT NULL DEFAULT 1 COMMENT '1-文本 2-图片 3-文件 4-语音',
  content         TEXT         COMMENT '文本或JSON元数据',
  media_url       VARCHAR(512) DEFAULT NULL COMMENT '媒体地址',
  sent_at         DATETIME     NOT NULL COMMENT '发送时间(清理依据)',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_msg_id (msg_id),
  KEY idx_conv_sent (conversation_id, sent_at),
  KEY idx_sent_at (sent_at),
  CONSTRAINT fk_msg_conv FOREIGN KEY (conversation_id) REFERENCES conversation (id)
) ENGINE=InnoDB COMMENT='消息';

-- ------------------------------------------------------------
-- 13. 消息已读状态（按处理人）
-- ------------------------------------------------------------
CREATE TABLE message_read_status (
  id                   BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  conversation_id      BIGINT   NOT NULL COMMENT '会话ID',
  reader_user_id       BIGINT   NOT NULL COMMENT '阅读者(sys_user.id)',
  last_read_message_id BIGINT   DEFAULT NULL COMMENT '已读到的最新消息',
  unread_count         INT      NOT NULL DEFAULT 0 COMMENT '未读数',
  updated_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_conv_reader (conversation_id, reader_user_id),
  KEY idx_reader_unread (reader_user_id, unread_count),
  CONSTRAINT fk_read_conv FOREIGN KEY (conversation_id) REFERENCES conversation (id),
  CONSTRAINT fk_read_user FOREIGN KEY (reader_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB COMMENT='消息已读状态';

-- ------------------------------------------------------------
-- 14. 接管配置（立即生效 / 定时生效，无自动结束）
-- ------------------------------------------------------------
CREATE TABLE hosting_config (
  id                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  takeover_manager_id  BIGINT       NOT NULL COMMENT '接管者ID',
  effective_type       TINYINT      NOT NULL DEFAULT 1 COMMENT '1-立即生效 2-定时生效',
  scheduled_start_at   DATETIME     DEFAULT NULL COMMENT '定时生效时间',
  description          VARCHAR(512) DEFAULT NULL COMMENT '接管说明',
  status               TINYINT      NOT NULL DEFAULT 0 COMMENT '0-草稿 1-待生效 2-生效中 3-已结束 4-已取消',
  created_by           BIGINT       NOT NULL COMMENT '创建人',
  created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_manager_status (takeover_manager_id, status),
  KEY idx_scheduled (scheduled_start_at, status),
  CONSTRAINT fk_config_manager FOREIGN KEY (takeover_manager_id) REFERENCES takeover_manager (id),
  CONSTRAINT fk_config_creator FOREIGN KEY (created_by) REFERENCES sys_user (id)
) ENGINE=InnoDB COMMENT='接管配置';

-- ------------------------------------------------------------
-- 15. 配置所选辅导老师（整位老师托管，含其全部企微账号）
-- ------------------------------------------------------------
CREATE TABLE hosting_config_tutor (
  id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  hosting_config_id BIGINT       NOT NULL COMMENT '配置ID',
  tutor_id          BIGINT       NOT NULL COMMENT '辅导老师ID',
  skip_reason       VARCHAR(128) DEFAULT NULL COMMENT '跳过原因(如已被他人托管)',
  status            TINYINT      NOT NULL DEFAULT 1 COMMENT '0-跳过 1-待生效 2-已生效',
  PRIMARY KEY (id),
  UNIQUE KEY uk_config_tutor (hosting_config_id, tutor_id),
  KEY idx_tutor_id (tutor_id),
  CONSTRAINT fk_hct_config FOREIGN KEY (hosting_config_id) REFERENCES hosting_config (id),
  CONSTRAINT fk_hct_tutor FOREIGN KEY (tutor_id) REFERENCES tutor (id)
) ENGINE=InnoDB COMMENT='接管配置-辅导老师关联';

-- ------------------------------------------------------------
-- 15b. 配置所选企微账号（账号级托管）
-- ------------------------------------------------------------
CREATE TABLE hosting_config_account (
  id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  hosting_config_id BIGINT       NOT NULL COMMENT '配置ID',
  tutor_id          BIGINT       NOT NULL COMMENT '辅导老师ID',
  tutor_account_id  BIGINT       NOT NULL COMMENT '企微账号ID',
  skip_reason       VARCHAR(128) DEFAULT NULL COMMENT '跳过原因(如已被他人托管)',
  status            TINYINT      NOT NULL DEFAULT 1 COMMENT '0-跳过 1-待生效 2-已生效',
  PRIMARY KEY (id),
  UNIQUE KEY uk_config_account (hosting_config_id, tutor_account_id),
  KEY idx_account (tutor_account_id),
  CONSTRAINT fk_hca_config FOREIGN KEY (hosting_config_id) REFERENCES hosting_config (id),
  CONSTRAINT fk_hca_tutor FOREIGN KEY (tutor_id) REFERENCES tutor (id),
  CONSTRAINT fk_hca_account FOREIGN KEY (tutor_account_id) REFERENCES tutor_wechat_account (id)
) ENGINE=InnoDB COMMENT='接管配置-企微账号关联';

-- ------------------------------------------------------------
-- 16. 当前生效托管关系（一企微账号同时仅一条 status=1）
-- ------------------------------------------------------------
CREATE TABLE hosting_assignment (
  id                  BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  hosting_config_id   BIGINT   NOT NULL COMMENT '来源配置',
  tutor_id            BIGINT   NOT NULL COMMENT '被托管辅导老师',
  tutor_account_id    BIGINT   DEFAULT NULL COMMENT '被托管企微账号',
  takeover_manager_id BIGINT   NOT NULL COMMENT '接管者',
  started_at          DATETIME NOT NULL COMMENT '生效开始',
  ended_at            DATETIME DEFAULT NULL COMMENT '手动结束时间',
  status              TINYINT  NOT NULL DEFAULT 1 COMMENT '1-进行中 2-已结束',
  created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_manager_status (takeover_manager_id, status),
  KEY idx_tutor_status (tutor_id, status),
  KEY idx_account_status (tutor_account_id, status),
  CONSTRAINT fk_assign_config FOREIGN KEY (hosting_config_id) REFERENCES hosting_config (id),
  CONSTRAINT fk_assign_tutor FOREIGN KEY (tutor_id) REFERENCES tutor (id),
  CONSTRAINT fk_assign_account FOREIGN KEY (tutor_account_id) REFERENCES tutor_wechat_account (id),
  CONSTRAINT fk_assign_manager FOREIGN KEY (takeover_manager_id) REFERENCES takeover_manager (id)
) ENGINE=InnoDB COMMENT='当前托管关系';

-- 业务层保证 tutor_account_id 在 status=1 时唯一

-- ------------------------------------------------------------
-- 17. 转接记录
-- action_type: 1-开始托管 2-结束托管 3-转接聊天
-- ------------------------------------------------------------
CREATE TABLE hosting_transfer_log (
  id                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  tutor_id             BIGINT       NOT NULL COMMENT '所属辅导老师',
  conversation_id      BIGINT       DEFAULT NULL COMMENT '被转接的会话',
  from_handler_user_id BIGINT       DEFAULT NULL COMMENT '原处理人',
  to_handler_user_id   BIGINT       DEFAULT NULL COMMENT '新处理人',
  action_type          TINYINT      NOT NULL COMMENT '1-开始托管 2-结束托管 3-转接聊天',
  operator_id          BIGINT       NOT NULL COMMENT '操作人',
  remark               VARCHAR(512) DEFAULT NULL COMMENT '备注',
  created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_tutor_time (tutor_id, created_at),
  KEY idx_conv_time (conversation_id, created_at),
  CONSTRAINT fk_log_tutor FOREIGN KEY (tutor_id) REFERENCES tutor (id),
  CONSTRAINT fk_log_conv FOREIGN KEY (conversation_id) REFERENCES conversation (id),
  CONSTRAINT fk_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user (id)
) ENGINE=InnoDB COMMENT='转接记录';

-- 延迟添加 conversation_handler 对 hosting_assignment 的外键
ALTER TABLE conversation_handler
  ADD CONSTRAINT fk_handler_assignment
  FOREIGN KEY (hosting_assignment_id) REFERENCES hosting_assignment (id);

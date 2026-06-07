-- ============================================================
-- 消息托管系统 - 演示数据
-- 对应 Java: DataInitializer + LitingWorkbenchSeeder
-- 使用前请先执行 schema.sql 建表
-- 所有用户密码均为: 123456
-- ============================================================

USE wechat;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 若需清空后重导，取消下面注释
-- TRUNCATE hosting_transfer_log;
-- TRUNCATE message_read_status;
-- TRUNCATE message;
-- TRUNCATE conversation_handler;
-- TRUNCATE hosting_config_account;
-- TRUNCATE hosting_assignment;
-- TRUNCATE hosting_config_tutor;
-- TRUNCATE hosting_config;
-- TRUNCATE conversation;
-- TRUNCATE chat_group_member;
-- TRUNCATE chat_group;
-- TRUNCATE student_tutor_relation;
-- TRUNCATE student;
-- TRUNCATE tutor_wechat_account;
-- TRUNCATE takeover_manager;
-- TRUNCATE tutor;
-- TRUNCATE sys_user;
-- TRUNCATE teaching_group;

SET FOREIGN_KEY_CHECKS = 1;

-- BCrypt(123456), 与 Spring BCryptPasswordEncoder 兼容
SET @pwd = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi';

-- ------------------------------------------------------------
-- 1. 教研组
-- ------------------------------------------------------------
INSERT INTO teaching_group (id, name, sort_order, status) VALUES
  (1, '数学教研组', 1, 1),
  (2, '物理教研组', 2, 1);

-- ------------------------------------------------------------
-- 2. 系统用户 (role: 1-管理员 2-辅导老师 3-接管者)
-- ------------------------------------------------------------
INSERT INTO sys_user (id, username, password_hash, real_name, phone, role, status) VALUES
  (1, 'admin',  @pwd, '管理员',   '13800000000', 1, 1),
  (2, 'liting',  @pwd, '李婷',     '13800000001', 3, 1),
  (3, 'zhang',   @pwd, '张明远',   '13800000002', 2, 1),
  (4, 'wang',    @pwd, '王慧',     '13800000003', 2, 1),
  (5, 'liu',     @pwd, '刘洋',     '13800000004', 2, 1);

-- ------------------------------------------------------------
-- 3. 辅导老师
-- ------------------------------------------------------------
INSERT INTO tutor (id, user_id, teaching_group_id, employee_no, status) VALUES
  (1, 3, 1, 'T001', 1),
  (2, 4, 1, 'T002', 1),
  (3, 5, 2, 'T003', 1);

-- ------------------------------------------------------------
-- 4. 接管者
-- ------------------------------------------------------------
INSERT INTO takeover_manager (id, user_id, max_tutor_count, status) VALUES
  (1, 2, 10, 1);

-- ------------------------------------------------------------
-- 5. 辅导企微账号
-- ------------------------------------------------------------
INSERT INTO tutor_wechat_account (id, tutor_id, account_name, subject, grade, wechat_userid, corp_id, agent_id, student_count, status) VALUES
  (1, 1, '张老师-数学辅导', '数学', '高一', 'wx_zhang_1', 'corp_demo', 'agent_demo', 3, 1),
  (2, 1, '张老师-数学答疑', '数学', '高一', 'wx_zhang_2', 'corp_demo', 'agent_demo', 2, 1),
  (3, 2, '王老师-数学辅导', '数学', '高二', 'wx_wang_1',  'corp_demo', 'agent_demo', 2, 1),
  (4, 3, '刘老师-物理辅导', '物理', '高二', 'wx_liu_1',   'corp_demo', 'agent_demo', 2, 1);

-- ------------------------------------------------------------
-- 6. 学生 (DataInitializer 基础数据)
-- ------------------------------------------------------------
INSERT INTO student (id, nickname, external_userid, grade, status) VALUES
  (1, '李明', 'ext_001', '高一', 1),
  (2, '王芳', 'ext_002', '高一', 1),
  (3, '赵强', 'ext_003', '高二', 1),
  (4, '陈静', 'ext_004', '高二', 1);

-- ------------------------------------------------------------
-- 7. 群聊
-- ------------------------------------------------------------
INSERT INTO chat_group (id, tutor_account_id, group_name, wechat_chat_id, member_count, status) VALUES
  (1, 4, '物理冲刺群', 'chat_001', 2, 1);

-- ------------------------------------------------------------
-- 8. 会话 (DataInitializer: id 1-4)
-- conv_type: 1-私聊 2-群聊 | stage: 1-转化期 2-承接期 3-已结课
-- ------------------------------------------------------------
INSERT INTO conversation (id, tutor_account_id, conv_type, student_id, group_id, last_message_at, last_message_preview, stage, status) VALUES
  (1, 1, 1, 1, NULL, DATE_SUB(NOW(), INTERVAL 10 MINUTE),  '老师，这道题我还不太懂',     1, 1),
  (2, 1, 1, 2, NULL, DATE_SUB(NOW(), INTERVAL 45 MINUTE), '好的，我今晚完成作业',       2, 1),
  (3, 3, 1, 3, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR),    '请问课程资料在哪里下载？',   1, 1),
  (4, 4, 2, NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR),    '今晚7点准时上课',            2, 1);

-- ------------------------------------------------------------
-- 9. 托管配置 (李婷工作台)
-- hosting_config.status: 2-生效中
-- ------------------------------------------------------------
INSERT INTO hosting_config (id, takeover_manager_id, effective_type, description, status, created_by) VALUES
  (1, 1, 1, '李婷工作台演示数据', 2, 1);

INSERT INTO hosting_config_tutor (id, hosting_config_id, tutor_id, status) VALUES
  (1, 1, 1, 2),
  (2, 1, 2, 2),
  (3, 1, 3, 2);

INSERT INTO hosting_config_account (id, hosting_config_id, tutor_id, tutor_account_id, status) VALUES
  (1, 1, 1, 1, 2),
  (2, 1, 2, 3, 2),
  (3, 1, 3, 4, 2);

INSERT INTO hosting_assignment (id, hosting_config_id, tutor_id, tutor_account_id, takeover_manager_id, started_at, status) VALUES
  (1, 1, 1, 1, 1, NOW(), 1),
  (2, 1, 2, 3, 1, NOW(), 1),
  (3, 1, 3, 4, 1, NOW(), 1);

-- ------------------------------------------------------------
-- 10. 李婷工作台演示学生 (id 5-14)
-- ------------------------------------------------------------
INSERT INTO student (id, nickname, external_userid, grade, status) VALUES
  (5,  '李明', 'ext_demo_001', '高一', 1),
  (6,  '王芳', 'ext_demo_002', '高一', 1),
  (7,  '孙磊', 'ext_demo_003', '高一', 1),
  (8,  '周婷', 'ext_demo_004', '高一', 1),
  (9,  '褚伟', 'ext_demo_005', '高一', 1),
  (10, '赵强', 'ext_demo_006', '高二', 1),
  (11, '吴昊', 'ext_demo_007', '高二', 1),
  (12, '钱琳', 'ext_demo_008', '高二', 1),
  (13, '郑悦', 'ext_demo_009', '高二', 1),
  (14, '冯杰', 'ext_demo_010', '高二', 1);

-- ------------------------------------------------------------
-- 11. 李婷工作台会话 (id 5-14, 私聊)
-- tutor_account: 张明远(数学高一)=1, 王慧(数学高二)=3, 刘洋(物理高二)=4
-- ------------------------------------------------------------
INSERT INTO conversation (id, tutor_account_id, conv_type, student_id, group_id, last_message_at, last_message_preview, stage, status) VALUES
  (5,  1, 1, 5,  NULL, DATE_SUB(NOW(), INTERVAL 5 MINUTE),   '老师，这道题我还不太懂',   1, 1),
  (6,  1, 1, 6,  NULL, DATE_SUB(NOW(), INTERVAL 20 MINUTE),  '好的，我今晚完成作业',     1, 1),
  (7,  1, 1, 7,  NULL, DATE_SUB(NOW(), INTERVAL 35 MINUTE),  '函数那块能再讲一遍吗',     1, 1),
  (8,  1, 1, 8,  NULL, DATE_SUB(NOW(), INTERVAL 60 MINUTE),  '作业已提交',               2, 1),
  (9,  1, 1, 9,  NULL, DATE_SUB(NOW(), INTERVAL 90 MINUTE),  '下周考试范围是什么',       2, 1),
  (10, 3, 1, 10, NULL, DATE_SUB(NOW(), INTERVAL 15 MINUTE),  '请问课程资料在哪里下载',   1, 1),
  (11, 3, 1, 11, NULL, DATE_SUB(NOW(), INTERVAL 40 MINUTE),  '圆锥曲线不太会',           1, 1),
  (12, 3, 1, 12, NULL, DATE_SUB(NOW(), INTERVAL 120 MINUTE), '谢谢老师讲解',             2, 1),
  (13, 4, 1, 13, NULL, DATE_SUB(NOW(), INTERVAL 25 MINUTE),  '电磁感应那题求思路',       1, 1),
  (14, 4, 1, 14, NULL, DATE_SUB(NOW(), INTERVAL 55 MINUTE),  '实验报告怎么写',           2, 1);

-- ------------------------------------------------------------
-- 12. 会话处理人 (李婷 user_id=2)
-- ------------------------------------------------------------
INSERT INTO conversation_handler (id, conversation_id, handler_user_id, handler_type, hosting_assignment_id, assigned_at) VALUES
  (1,  5,  2, 1, 1, NOW()),
  (2,  6,  2, 1, 1, NOW()),
  (3,  7,  2, 1, 1, NOW()),
  (4,  8,  2, 1, 1, NOW()),
  (5,  9,  2, 1, 1, NOW()),
  (6,  10, 2, 1, 2, NOW()),
  (7,  11, 2, 1, 2, NOW()),
  (8,  12, 2, 1, 2, NOW()),
  (9,  13, 2, 1, 3, NOW()),
  (10, 14, 2, 1, 3, NOW());

-- ------------------------------------------------------------
-- 13. 未读数 (unread_count > 0 的会话)
-- ------------------------------------------------------------
INSERT INTO message_read_status (id, conversation_id, reader_user_id, unread_count) VALUES
  (1, 5,  2, 3),
  (2, 6,  2, 1),
  (3, 7,  2, 2),
  (4, 9,  2, 4),
  (5, 10, 2, 2),
  (6, 11, 2, 1),
  (7, 13, 2, 3),
  (8, 14, 2, 2);

-- ------------------------------------------------------------
-- 重置自增 ID
-- ------------------------------------------------------------
ALTER TABLE teaching_group AUTO_INCREMENT = 100;
ALTER TABLE sys_user AUTO_INCREMENT = 100;
ALTER TABLE tutor AUTO_INCREMENT = 100;
ALTER TABLE takeover_manager AUTO_INCREMENT = 100;
ALTER TABLE tutor_wechat_account AUTO_INCREMENT = 100;
ALTER TABLE student AUTO_INCREMENT = 100;
ALTER TABLE chat_group AUTO_INCREMENT = 100;
ALTER TABLE conversation AUTO_INCREMENT = 100;
ALTER TABLE hosting_config AUTO_INCREMENT = 100;
ALTER TABLE hosting_config_tutor AUTO_INCREMENT = 100;
ALTER TABLE hosting_config_account AUTO_INCREMENT = 100;
ALTER TABLE hosting_assignment AUTO_INCREMENT = 100;
ALTER TABLE conversation_handler AUTO_INCREMENT = 100;
ALTER TABLE message_read_status AUTO_INCREMENT = 100;

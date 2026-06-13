-- 排课模块
USE study_room;

CREATE TABLE IF NOT EXISTS lesson_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    campus_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL COMMENT '授课老师(员工)',
    schedule_type VARCHAR(20) NOT NULL COMMENT 'ONE_ON_ONE/SMALL_CLASS/EVENING_CARE/OTHER',
    student_id BIGINT NULL,
    title VARCHAR(100) NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    classroom VARCHAR(100) NULL,
    remark VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING待上课 COMPLETED已上课',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_schedule_campus (campus_id),
    INDEX idx_schedule_teacher (teacher_id),
    INDEX idx_schedule_start (start_time),
    INDEX idx_schedule_status (status)
);

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order) VALUES
(0, '排课管理', 'schedule', 1, '/schedule', 9);

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建排课', 'schedule:create', 2, NULL, 1 FROM sys_permission p WHERE p.code = 'schedule' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '编辑排课', 'schedule:update', 2, NULL, 2 FROM sys_permission p WHERE p.code = 'schedule' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '删除排课', 'schedule:delete', 2, NULL, 3 FROM sys_permission p WHERE p.code = 'schedule' LIMIT 1;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE code IN ('schedule', 'schedule:create', 'schedule:update', 'schedule:delete');

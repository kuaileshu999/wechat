CREATE DATABASE IF NOT EXISTS study_room DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE study_room;

-- 校区
CREATE TABLE IF NOT EXISTS campus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 系统用户
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用所有权限',
    locked TINYINT NOT NULL DEFAULT 0 COMMENT '1锁定 0正常',
    login_fail_count INT NOT NULL DEFAULT 0,
    employee_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 权限（菜单/按钮）
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    type TINYINT NOT NULL COMMENT '1菜单 2按钮',
    path VARCHAR(200),
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

-- 用户校区授权
CREATE TABLE IF NOT EXISTS sys_user_campus (
    user_id BIGINT NOT NULL,
    campus_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, campus_id)
);

-- 员工
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    campus_id BIGINT NOT NULL,
    employment_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE在职 RESIGNED离职',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_employee_campus (campus_id)
);

-- 学员
CREATE TABLE IF NOT EXISTS student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    campus_id BIGINT NOT NULL,
    remark VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_phone (phone),
    INDEX idx_student_campus (campus_id),
    INDEX idx_student_name (name)
);

-- 课程类型
CREATE TABLE IF NOT EXISTS course_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    campus_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course_type_campus (campus_id)
);

-- 课程
CREATE TABLE IF NOT EXISTS course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    campus_id BIGINT NOT NULL,
    course_type_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    subject VARCHAR(20) NOT NULL COMMENT '语文/数学/英语/物理/历史/地理',
    consumption_mode VARCHAR(20) NOT NULL COMMENT 'AMOUNT每次扣金额 HOURS每次扣课时',
    unit_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '每课时金额或每次扣金额',
    unit_hours DECIMAL(10,2) NOT NULL DEFAULT 1 COMMENT '每次扣几课时',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course_campus (campus_id),
    INDEX idx_course_type (course_type_id)
);

-- 订单
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    campus_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    total_hours INT NOT NULL,
    paid_amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL COMMENT 'ALIPAY/WECHAT/CASH',
    payment_date DATE NOT NULL,
    salesperson_id BIGINT NOT NULL,
    remark VARCHAR(500),
    consumed_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    consumed_hours DECIMAL(10,2) NOT NULL DEFAULT 0,
    refunded_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/REFUNDED/PARTIAL_REFUND',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_orders_campus (campus_id),
    INDEX idx_orders_student (student_id),
    INDEX idx_orders_payment_date (payment_date)
);

-- 退费记录
CREATE TABLE IF NOT EXISTS order_refund (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    refund_amount DECIMAL(12,2) NOT NULL,
    refund_reason VARCHAR(500) NOT NULL,
    refund_method VARCHAR(20) NOT NULL COMMENT 'ALIPAY/WECHAT/CASH',
    remark VARCHAR(200),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_refund_order (order_id)
);

-- 消课记录
CREATE TABLE IF NOT EXISTS consumption_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    campus_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    consumption_mode VARCHAR(20) NOT NULL COMMENT 'AMOUNT/HOURS',
    consumed_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    consumed_hours DECIMAL(10,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' COMMENT 'COMPLETED/PENDING/CANCELLED',
    batch_no VARCHAR(32),
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_consumption_order (order_id),
    INDEX idx_consumption_status (status),
    INDEX idx_consumption_campus (campus_id)
);

-- 审计日志
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL COMMENT 'CREATE/UPDATE/DELETE',
    content TEXT NOT NULL,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_entity (entity_type, entity_id)
);

-- 排课
CREATE TABLE IF NOT EXISTS lesson_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    campus_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL COMMENT '授课老师(员工)',
    course_type_id BIGINT NOT NULL COMMENT '课程类型',
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

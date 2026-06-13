USE study_room;

INSERT IGNORE INTO campus (name) VALUES
('总校区'), ('一分校'), ('二分校'), ('三分校'), ('四分校'),
('五分校'), ('六分校'), ('七分校'), ('八分校'), ('九分校');

INSERT IGNORE INTO sys_role (name, code, description) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有全部权限'),
('财务', 'FINANCE', '查看全部校区财务数据'),
('校区管理员', 'CAMPUS_ADMIN', '管理指定校区业务');

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order) VALUES
(0, '系统管理', 'system', 1, '/system', 1);

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '校区管理', 'system:campus', 1, '/system/campus', 0 FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建校区', 'system:campus:create', 2, NULL, 0 FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '角色管理', 'system:role', 1, '/system/role', 1 FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '用户管理', 'system:permission', 1, '/system/user', 2 FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建角色', 'system:role:create', 2, NULL, 3 FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建用户', 'system:user:create', 2, NULL, 4 FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order) VALUES
(0, '员工管理', 'employee', 1, '/employee', 2);

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建员工', 'employee:create', 2, NULL, 1 FROM sys_permission p WHERE p.code = 'employee' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order) VALUES
(0, '学员管理', 'student', 1, '/student', 3);

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建学员', 'student:create', 2, NULL, 1 FROM sys_permission p WHERE p.code = 'student' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '批量导入', 'student:import', 2, NULL, 2 FROM sys_permission p WHERE p.code = 'student' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '编辑学员', 'student:update', 2, NULL, 3 FROM sys_permission p WHERE p.code = 'student' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order) VALUES
(0, '课程类型', 'course-type', 1, '/course-type', 4);

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建课程类型', 'course-type:create', 2, NULL, 1 FROM sys_permission p WHERE p.code = 'course-type' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order) VALUES
(0, '课程管理', 'course', 1, '/course', 5);

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建课程', 'course:create', 2, NULL, 1 FROM sys_permission p WHERE p.code = 'course' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order) VALUES
(0, '订单管理', 'order', 1, '/order', 6);

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建订单', 'order:create', 2, NULL, 1 FROM sys_permission p WHERE p.code = 'order' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '发起退费', 'order:refund', 2, NULL, 2 FROM sys_permission p WHERE p.code = 'order' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order) VALUES
(0, '消课管理', 'consumption', 1, '/consumption', 7);

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '执行消课', 'consumption:execute', 2, NULL, 1 FROM sys_permission p WHERE p.code = 'consumption' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order) VALUES
(0, '财务管理', 'finance', 1, '/finance', 8);

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order) VALUES
(0, '排课管理', 'schedule', 1, '/schedule', 9);

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建排课', 'schedule:create', 2, NULL, 1 FROM sys_permission p WHERE p.code = 'schedule' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '编辑排课', 'schedule:update', 2, NULL, 2 FROM sys_permission p WHERE p.code = 'schedule' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '删除排课', 'schedule:delete', 2, NULL, 3 FROM sys_permission p WHERE p.code = 'schedule' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '导入排课', 'schedule:import', 2, NULL, 4 FROM sys_permission p WHERE p.code = 'schedule' LIMIT 1;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

INSERT IGNORE INTO sys_user (username, password, real_name, enabled) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1);

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.code = 'SUPER_ADMIN';

INSERT IGNORE INTO sys_user_campus (user_id, campus_id)
SELECT u.id, c.id FROM sys_user u, campus c
WHERE u.username = 'admin';

-- 增加校区管理权限
USE study_room;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '校区管理', 'system:campus', 1, '/system/campus', 0
FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建校区', 'system:campus:create', 2, NULL, 0
FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE code IN ('system:campus', 'system:campus:create');

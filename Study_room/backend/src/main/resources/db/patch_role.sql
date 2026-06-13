-- 已有数据库升级脚本：增加角色管理权限，修复系统管理菜单
USE study_room;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '角色管理', 'system:role', 1, '/system/role', 1
FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

UPDATE sys_permission SET name = '用户管理', path = '/system/user', sort_order = 2
WHERE code = 'system:permission';

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建角色', 'system:role:create', 2, NULL, 3
FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE code IN ('system:role', 'system:role:create');

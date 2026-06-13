-- 排课导入权限
USE study_room;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '导入排课', 'schedule:import', 2, NULL, 4
FROM sys_permission p WHERE p.code = 'schedule' LIMIT 1;

UPDATE sys_permission SET name = '导入排课' WHERE code = 'schedule:import';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE code = 'schedule:import';

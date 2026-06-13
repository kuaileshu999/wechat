-- 修复权限数据：补全角色管理、用户管理路径、中文名称
USE study_room;

UPDATE sys_permission SET name = '用户管理', path = '/system/user', sort_order = 2
WHERE code = 'system:permission';

UPDATE sys_permission SET name = '新建用户' WHERE code = 'system:user:create';

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '角色管理', 'system:role', 1, '/system/role', 1
FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建角色', 'system:role:create', 2, NULL, 3
FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '编辑学员', 'student:update', 2, NULL, 3
FROM sys_permission p WHERE p.code = 'student' LIMIT 1;

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建用户', 'system:user:create', 2, NULL, 4
FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

UPDATE sys_permission child
JOIN sys_permission parent ON parent.code = 'course-type'
SET child.parent_id = parent.id
WHERE child.code = 'course-type:create';

UPDATE sys_permission child
JOIN sys_permission parent ON parent.code = 'course'
SET child.parent_id = parent.id
WHERE child.code = 'course:create';

UPDATE sys_permission child
JOIN sys_permission parent ON parent.code = 'order'
SET child.parent_id = parent.id
WHERE child.code IN ('order:create', 'order:refund');

UPDATE sys_permission child
JOIN sys_permission parent ON parent.code = 'consumption'
SET child.parent_id = parent.id
WHERE child.code = 'consumption:execute';

UPDATE sys_permission child
JOIN sys_permission parent ON parent.code = 'employee'
SET child.parent_id = parent.id
WHERE child.code = 'employee:create';

UPDATE sys_permission child
JOIN sys_permission parent ON parent.code = 'student'
SET child.parent_id = parent.id
WHERE child.code IN ('student:create', 'student:import', 'student:update');

UPDATE sys_permission child
JOIN sys_permission parent ON parent.code = 'system'
SET child.parent_id = parent.id
WHERE child.code IN ('system:campus', 'system:campus:create', 'system:role', 'system:role:create',
                     'system:permission', 'system:user:create');

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

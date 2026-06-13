-- 修正权限树 parent_id 关系
USE study_room;

UPDATE sys_permission SET name = '用户管理' WHERE code = 'system:permission';

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

INSERT IGNORE INTO sys_permission (parent_id, name, code, type, path, sort_order)
SELECT p.id, '新建用户', 'system:user:create', 2, NULL, 4
FROM sys_permission p WHERE p.code = 'system' LIMIT 1;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE code = 'system:user:create';

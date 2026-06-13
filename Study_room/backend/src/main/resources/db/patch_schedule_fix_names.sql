-- 修复排课权限中文名称乱码
USE study_room;

UPDATE sys_permission SET name = '排课管理' WHERE code = 'schedule';
UPDATE sys_permission SET name = '新建排课' WHERE code = 'schedule:create';
UPDATE sys_permission SET name = '编辑排课' WHERE code = 'schedule:update';
UPDATE sys_permission SET name = '删除排课' WHERE code = 'schedule:delete';

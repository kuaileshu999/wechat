-- 员工表移除校区字段（兼容 MySQL 5.7+）
USE study_room;

-- 删除 campus_id 列（关联索引会一并删除）
ALTER TABLE employee DROP COLUMN campus_id;

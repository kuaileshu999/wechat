-- 修复权限名称乱码、导出权限排序（可重复执行）
UPDATE sys_permission SET name = '订单管理' WHERE code = 'order';
UPDATE sys_permission SET name = '新建订单' WHERE code = 'order:create';
UPDATE sys_permission SET name = '发起退费' WHERE code = 'order:refund';
UPDATE sys_permission SET name = '导出订单' WHERE code = 'order:export';
UPDATE sys_permission SET sort_order = 3 WHERE code = 'order:export';

UPDATE sys_permission SET name = '消课管理' WHERE code = 'consumption';
UPDATE sys_permission SET name = '执行消课' WHERE code = 'consumption:execute';
UPDATE sys_permission SET name = '导出消课' WHERE code = 'consumption:export';
UPDATE sys_permission SET sort_order = 1 WHERE code = 'consumption:execute';
UPDATE sys_permission SET sort_order = 2 WHERE code = 'consumption:export';

UPDATE sys_permission SET name = '财务管理' WHERE code = 'finance';
UPDATE sys_permission SET name = '导出财务' WHERE code = 'finance:export';
UPDATE sys_permission SET sort_order = 1 WHERE code = 'finance:export';

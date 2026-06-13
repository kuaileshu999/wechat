-- 退款记录增加备注字段
USE study_room;

ALTER TABLE order_refund ADD COLUMN remark VARCHAR(200) NULL AFTER refund_method;

-- 消课取消：记录取消原因，保留在消课列表中展示（首次执行）
ALTER TABLE consumption_record
    ADD COLUMN cancel_reason VARCHAR(500) NULL COMMENT '取消原因' AFTER remark;

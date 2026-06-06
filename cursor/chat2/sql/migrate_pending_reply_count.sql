USE message_takeover;

ALTER TABLE conversation
    ADD COLUMN pending_reply_count INT NOT NULL DEFAULT 0 COMMENT '待回复消息条数' AFTER pending_reply;

UPDATE conversation
SET pending_reply_count = IF(pending_reply = 1, (id % 6) + 1, 0);

USE message_takeover;

ALTER TABLE conversation
    ADD COLUMN pending_reply TINYINT(1) NOT NULL DEFAULT 0 COMMENT '最后一条为学生消息待回复' AFTER unread_count;

ALTER TABLE conversation ADD INDEX idx_pending (category, chat_type, pending_reply);

UPDATE conversation c
JOIN (
    SELECT m.conversation_id, m.direction
    FROM chat_message m
    INNER JOIN (
        SELECT conversation_id, MAX(sent_at) AS max_sent
        FROM chat_message
        GROUP BY conversation_id
    ) t ON m.conversation_id = t.conversation_id AND m.sent_at = t.max_sent
) last_msg ON c.id = last_msg.conversation_id
SET c.pending_reply = IF(last_msg.direction = 'INCOMING', 1, 0);

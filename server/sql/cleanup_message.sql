-- 消息保留策略：仅保留最近 1 个月
-- 建议由 Spring @Scheduled 定时任务调用，分批执行避免长事务

-- 示例：删除 1 个月前的消息（每次最多 5000 条）
DELETE FROM message
WHERE sent_at < DATE_SUB(NOW(), INTERVAL 1 MONTH)
LIMIT 5000;

-- 可选：清理已无消息的会话摘要（应用层维护 last_message 字段时需同步更新）

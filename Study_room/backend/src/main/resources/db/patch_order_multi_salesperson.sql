-- 订单多销售人（可重复执行）
CREATE TABLE IF NOT EXISTS order_salesperson (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    salesperson_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_salesperson (order_id, salesperson_id),
    KEY idx_order_salesperson_order (order_id)
);

INSERT IGNORE INTO order_salesperson (order_id, salesperson_id)
SELECT o.id, o.salesperson_id FROM orders o WHERE o.salesperson_id IS NOT NULL;

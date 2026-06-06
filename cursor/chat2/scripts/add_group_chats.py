#!/usr/bin/env python3
"""
在现有数据库上为每个企微账号补充 5 个群及群聊记录（不重建全库）。
会先删除已有 GROUP 类型会话及其消息，再重新插入。
"""

from __future__ import annotations

import sys
import time
from pathlib import Path

try:
    import pymysql
except ImportError:
    print("请先安装: pip install -r scripts/requirements.txt", file=sys.stderr)
    sys.exit(1)

# 复用生成脚本中的常量与模板
sys.path.insert(0, str(Path(__file__).resolve().parent))
from generate_bulk_data import (  # noqa: E402
    BATCH_SIZE,
    GROUPS_PER_ACCOUNT,
    GROUP_INCOMING_TEMPLATES,
    MESSAGES_PER_GROUP,
    OUTGOING_TEMPLATES,
    account_display_name,
    chunked,
    generate_messages_for_conv,
)
import random
from datetime import datetime, timedelta

GROUP_LABELS = ["春季班", "暑假班", "冲刺班", "转化班", "精品小班"]


def main() -> None:
    conn = pymysql.connect(
        host="127.0.0.1",
        user="root",
        password="123456",
        database="message_takeover",
        charset="utf8mb4",
        autocommit=False,
    )
    t0 = time.time()
    try:
        with conn.cursor() as cur:
            cur.execute("SET FOREIGN_KEY_CHECKS=0")
            cur.execute(
                "DELETE m FROM chat_message m "
                "INNER JOIN conversation c ON m.conversation_id = c.id "
                "WHERE c.chat_type = 'GROUP'"
            )
            cur.execute("DELETE FROM conversation WHERE chat_type = 'GROUP'")
            cur.execute("SELECT COUNT(*) FROM wecom_account")
            account_count = cur.fetchone()[0]
            cur.execute(
                "SELECT id, account_name FROM wecom_account ORDER BY id"
            )
            accounts = cur.fetchall()

        print(f"✓ 已清理旧群聊数据，共 {account_count} 个企微账号")

        base_time = datetime(2026, 5, 1, 8, 0, 0)
        group_specs = []
        for acc_id, acc_name in accounts:
            base_student_id = (acc_id - 1) * 100 + 1
            for g in range(1, GROUPS_PER_ACCOUNT + 1):
                student_id = base_student_id + (g - 1)
                group_name = f"{acc_name}-{GROUP_LABELS[g - 1]}"
                category = "UNDERTAKING" if g <= 3 else "CONVERSION"
                unread = random.randint(1, 8) if (acc_id + g) % 3 == 0 else 0
                group_specs.append((acc_id, student_id, group_name, category, unread))

        with conn.cursor() as cur:
            cur.execute("SELECT COALESCE(MAX(id), 0) FROM conversation")
            max_conv_before = cur.fetchone()[0]

        conversations = []
        for idx, (acc_id, student_id, group_name, category, unread) in enumerate(group_specs, start=1):
            last_at = base_time + timedelta(days=(max_conv_before + idx) % 30, hours=idx % 12)
            preview = random.choice(GROUP_INCOMING_TEMPLATES)
            conversations.append(
                (acc_id, student_id, "GROUP", group_name, category,
                 last_at.strftime("%Y-%m-%d %H:%M:%S"), preview[:200], unread)
            )

        print(f"→ 插入 {len(conversations)} 个群会话 ...")
        for batch in chunked(conversations, BATCH_SIZE):
            with conn.cursor() as cur:
                cur.executemany(
                    "INSERT INTO conversation (wecom_account_id, student_id, chat_type, group_name, "
                    "category, last_message_at, last_message_preview, unread_count) "
                    "VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
                    batch,
                )
            conn.commit()

        with conn.cursor() as cur:
            cur.execute(
                "SELECT id, wecom_account_id, group_name FROM conversation "
                "WHERE chat_type = 'GROUP' ORDER BY id"
            )
            group_convs = cur.fetchall()

        print(f"→ 写入群消息（每群 {MESSAGES_PER_GROUP} 条）...")
        msg_batch = []
        conv_updates = []
        inserted = 0
        for idx, (conv_id, acc_id, group_name) in enumerate(group_convs):
            seed_unread = group_specs[idx][4] if idx < len(group_specs) else 0
            conv_base = base_time + timedelta(days=conv_id % 25)
            force_pending = (acc_id + conv_id) % 2 == 1
            batch_msgs, conv_update = generate_messages_for_conv(
                conv_id,
                conv_base,
                GROUP_INCOMING_TEMPLATES,
                group_name,
                MESSAGES_PER_GROUP,
                force_pending,
                initial_unread=seed_unread,
            )
            msg_batch.extend(batch_msgs)
            conv_updates.append(conv_update)
            if len(msg_batch) >= BATCH_SIZE:
                with conn.cursor() as cur:
                    cur.executemany(
                        "INSERT INTO chat_message (conversation_id, direction, sender_name, content, sent_at) "
                        "VALUES (%s, %s, %s, %s, %s)",
                        msg_batch,
                    )
                conn.commit()
                inserted += len(msg_batch)
                msg_batch.clear()

        if msg_batch:
            with conn.cursor() as cur:
                cur.executemany(
                    "INSERT INTO chat_message (conversation_id, direction, sender_name, content, sent_at) "
                    "VALUES (%s, %s, %s, %s, %s)",
                    msg_batch,
                )
            conn.commit()
            inserted += len(msg_batch)

        for batch in chunked(conv_updates, BATCH_SIZE):
            with conn.cursor() as cur:
                cur.executemany(
                    "UPDATE conversation SET last_message_preview=%s, last_message_at=%s, "
                    "pending_reply=%s, pending_reply_count=%s, unread_count=%s WHERE id=%s",
                    batch,
                )
            conn.commit()

        with conn.cursor() as cur:
            cur.execute("SET FOREIGN_KEY_CHECKS=1")
            cur.execute(
                "SELECT chat_type, COUNT(*) FROM conversation GROUP BY chat_type"
            )
            stats = cur.fetchall()
        conn.commit()

        print(f"✓ 群消息 {inserted:,} 条")
        print("✓ 会话统计:", dict(stats))
        print(f"完成，耗时 {time.time() - t0:.1f} 秒")

    finally:
        conn.close()


if __name__ == "__main__":
    main()

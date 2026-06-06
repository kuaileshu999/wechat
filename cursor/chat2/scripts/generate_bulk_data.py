#!/usr/bin/env python3
"""
批量生成演示数据：
- 50 辅导、100 企微账号、10000 学生（每账号 100 人）
- 每学生 1 个私聊会话 + 100 条消息
- 每账号 5 个群 + 每群 50 条群聊消息
"""

from __future__ import annotations

import argparse
import random
import sys
import time
from datetime import datetime, timedelta
from pathlib import Path

try:
    import pymysql
except ImportError:
    print("请先安装依赖: pip install -r scripts/requirements.txt", file=sys.stderr)
    sys.exit(1)

ROOT = Path(__file__).resolve().parents[1]
SCHEMA_FILE = ROOT / "sql" / "schema.sql"

TUTOR_COUNT = 50
ACCOUNT_COUNT = 100
STUDENTS_PER_ACCOUNT = 100
GROUPS_PER_ACCOUNT = 5
MESSAGES_PER_STUDENT = 100
MESSAGES_PER_GROUP = 50
TAKEOVER_TUTOR_COUNT = 5
BATCH_SIZE = 3000

INCOMING_TEMPLATES = [
    "老师您好，想咨询一下课程安排",
    "请问本周还有试听课吗？",
    "孩子基础一般，适合跟哪个班？",
    "最近作业有点多，直播能回放吗？",
    "报名优惠截止到什么时候？",
    "想了解一下转化班的学习节奏",
    "群里发的资料在哪里下载？",
    "今晚直播几点开始？",
    "已付款，麻烦帮忙确认一下",
    "孩子最近进步不错，谢谢老师",
]

GROUP_INCOMING_TEMPLATES = [
    "[群] 今晚直播几点开始？",
    "[群] 作业打卡链接在哪？",
    "[群] 资料已下载，请查收",
    "[群] 孩子请假一天可以吗？",
    "[群] 转化活动还有名额吗？",
    "[群] 群公告里的课程表打不开",
    "[群] 已付款，请老师确认",
]

OUTGOING_TEMPLATES = [
    "家长您好，已收到，我稍后详细回复您",
    "试听课链接发您了，请注意查收",
    "建议先跟基础班，循序渐进会更稳",
    "回放链接在群公告里，可随时观看",
    "优惠活动本月底结束，建议您尽快决定",
    "转化班每周三次直播+作业批改",
    "资料已上传到学习平台，请登录查看",
    "今晚 20:00 准时开播，请提前 5 分钟进入",
    "已帮您登记，班主任会主动联系您",
    "感谢认可，我们继续加油",
]


def load_schema(conn: pymysql.Connection) -> None:
    sql = SCHEMA_FILE.read_text(encoding="utf-8")
    statements = []
    buf = []
    for line in sql.splitlines():
        stripped = line.strip()
        if stripped.startswith("--") or not stripped:
            continue
        buf.append(line)
        if stripped.endswith(";"):
            statements.append("\n".join(buf))
            buf = []
    with conn.cursor() as cur:
        for stmt in statements:
            cur.execute(stmt)
    conn.commit()
    print("✓ 数据库表结构已重建")


def chunked(rows: list, size: int):
    for i in range(0, len(rows), size):
        yield rows[i : i + size]


def account_display_name(acc_id: int) -> str:
    tutor_id = (acc_id - 1) // 2 + 1
    suffix = "A" if acc_id % 2 == 1 else "B"
    return f"辅导{tutor_id:02d}-企微{suffix}"


def generate_messages_for_conv(
    conv_id: int,
    conv_base: datetime,
    incoming_templates: list[str],
    sender_incoming: str,
    message_count: int,
    force_pending: bool,
    initial_unread: int = 0,
) -> tuple[list[tuple], tuple]:
    msg_batch: list[tuple] = []
    last_preview = ""
    last_at = ""
    unreplied_streak = 0

    for msg_idx in range(1, message_count + 1):
        is_incoming = msg_idx % 2 == 1
        if msg_idx == message_count and force_pending:
            is_incoming = True
        if is_incoming:
            direction = "INCOMING"
            sender = sender_incoming
            content = random.choice(incoming_templates)
        else:
            direction = "OUTGOING"
            sender = "接管辅导"
            content = random.choice(OUTGOING_TEMPLATES)

        sent_at = (conv_base + timedelta(minutes=msg_idx * 3)).strftime("%Y-%m-%d %H:%M:%S")
        last_preview = content[:512]
        last_at = sent_at
        if is_incoming:
            unreplied_streak += 1
        else:
            unreplied_streak = 0
        msg_batch.append((conv_id, direction, sender, content, sent_at))

    pending_reply = 1 if unreplied_streak > 0 else 0
    pending_reply_count = unreplied_streak
    unread_count = max(pending_reply_count, initial_unread)
    conv_update = (last_preview, last_at, pending_reply, pending_reply_count, unread_count, conv_id)
    return msg_batch, conv_update


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=3306)
    parser.add_argument("--user", default="root")
    parser.add_argument("--password", default="123456")
    args = parser.parse_args()

    total_students = ACCOUNT_COUNT * STUDENTS_PER_ACCOUNT
    total_groups = ACCOUNT_COUNT * GROUPS_PER_ACCOUNT
    total_private_convs = total_students
    total_group_convs = total_groups
    total_convs = total_private_convs + total_group_convs
    total_messages = total_students * MESSAGES_PER_STUDENT + total_groups * MESSAGES_PER_GROUP

    print("=" * 56)
    print("企微消息接管系统 - 批量数据生成")
    print(f"辅导: {TUTOR_COUNT} | 账号: {ACCOUNT_COUNT} | 学生: {total_students:,}")
    print(f"私聊会话: {total_private_convs:,} | 群聊: {total_group_convs:,} (每账号 {GROUPS_PER_ACCOUNT} 个群)")
    print(f"消息总数: {total_messages:,}")
    print("=" * 56)

    t0 = time.time()
    conn = pymysql.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        charset="utf8mb4",
        autocommit=False,
    )

    try:
        load_schema(conn)
        conn.select_db("message_takeover")

        with conn.cursor() as cur:
            cur.execute("SET FOREIGN_KEY_CHECKS=0")
            cur.execute("SET UNIQUE_CHECKS=0")

        # ---------- 辅导 ----------
        tutors = []
        for i in range(1, TUTOR_COUNT + 1):
            is_takeover = 1 if i > TUTOR_COUNT - TAKEOVER_TUTOR_COUNT else 0
            tutors.append((f"辅导{i:02d}", f"138{10000000 + i:08d}"[-11:], is_takeover))
        with conn.cursor() as cur:
            cur.executemany(
                "INSERT INTO tutor (name, phone, is_takeover_role) VALUES (%s, %s, %s)",
                tutors,
            )
        conn.commit()
        print(f"✓ 辅导 {TUTOR_COUNT} 条")

        # ---------- 企微账号 ----------
        accounts = []
        for acc_id in range(1, ACCOUNT_COUNT + 1):
            tutor_id = (acc_id - 1) // 2 + 1
            suffix = "A" if acc_id % 2 == 1 else "B"
            accounts.append(
                (tutor_id, f"辅导{tutor_id:02d}-企微{suffix}", f"wework_{tutor_id:02d}_{suffix.lower()}")
            )
        with conn.cursor() as cur:
            cur.executemany(
                "INSERT INTO wecom_account (tutor_id, account_name, wecom_user_id) VALUES (%s, %s, %s)",
                accounts,
            )
        conn.commit()
        print(f"✓ 企微账号 {ACCOUNT_COUNT} 条")

        # ---------- 接管配置 ----------
        takeover_tutor_ids = list(range(TUTOR_COUNT - TAKEOVER_TUTOR_COUNT + 1, TUTOR_COUNT + 1))
        source_tutor_ids = list(range(1, TUTOR_COUNT - TAKEOVER_TUTOR_COUNT + 1))
        assignments = [
            (src, None, takeover_tutor_ids[(src - 1) % len(takeover_tutor_ids)], 1)
            for src in source_tutor_ids
        ]
        with conn.cursor() as cur:
            cur.executemany(
                "INSERT INTO takeover_assignment (source_tutor_id, wecom_account_id, takeover_tutor_id, enabled) "
                "VALUES (%s, %s, %s, %s)",
                assignments,
            )
        conn.commit()
        print(f"✓ 接管配置 {len(assignments)} 条")

        # ---------- 学生 ----------
        students = []
        global_stu_idx = 0
        for acc_id in range(1, ACCOUNT_COUNT + 1):
            for seq in range(1, STUDENTS_PER_ACCOUNT + 1):
                global_stu_idx += 1
                students.append((acc_id, f"学生{global_stu_idx:05d}", f"ext_{acc_id:03d}_{seq:03d}"))
        print(f"→ 正在写入学生 {len(students):,} 条 ...")
        for batch in chunked(students, BATCH_SIZE):
            with conn.cursor() as cur:
                cur.executemany(
                    "INSERT INTO student (wecom_account_id, name, external_user_id) VALUES (%s, %s, %s)",
                    batch,
                )
            conn.commit()
        print(f"✓ 学生 {len(students):,} 条")

        base_time = datetime(2026, 5, 1, 8, 0, 0)

        # ---------- 会话：私聊 + 群聊 ----------
        # conv_spec: (acc_id, student_id, chat_type, group_name, category, unread)
        private_specs = []
        for acc_id in range(1, ACCOUNT_COUNT + 1):
            base_student_id = (acc_id - 1) * STUDENTS_PER_ACCOUNT + 1
            for offset in range(STUDENTS_PER_ACCOUNT):
                student_id = base_student_id + offset
                category = "UNDERTAKING" if student_id % 2 == 1 else "CONVERSION"
                unread = random.randint(0, 5) if student_id % 7 == 0 else 0
                private_specs.append((acc_id, student_id, "PRIVATE", None, category, unread))

        group_specs = []
        group_labels = ["春季班", "暑假班", "冲刺班", "转化班", "精品小班"]
        for acc_id in range(1, ACCOUNT_COUNT + 1):
            base_student_id = (acc_id - 1) * STUDENTS_PER_ACCOUNT + 1
            acc_name = account_display_name(acc_id)
            for g in range(1, GROUPS_PER_ACCOUNT + 1):
                student_id = base_student_id + (g - 1)
                group_name = f"{acc_name}-{group_labels[g - 1]}"
                category = "UNDERTAKING" if g <= 3 else "CONVERSION"
                unread = random.randint(1, 8) if (acc_id + g) % 3 == 0 else 0
                group_specs.append((acc_id, student_id, "GROUP", group_name, category, unread))

        all_specs = private_specs + group_specs
        conversations = []
        for idx, (acc_id, student_id, chat_type, group_name, category, unread) in enumerate(all_specs, start=1):
            last_at = base_time + timedelta(
                days=idx % 30,
                hours=idx % 12,
                minutes=idx % 60,
            )
            preview = random.choice(GROUP_INCOMING_TEMPLATES if chat_type == "GROUP" else INCOMING_TEMPLATES)
            conversations.append(
                (
                    acc_id,
                    student_id,
                    chat_type,
                    group_name,
                    category,
                    last_at.strftime("%Y-%m-%d %H:%M:%S"),
                    preview[:200],
                    unread,
                )
            )

        print(f"→ 正在写入会话 {len(conversations):,} 条（私聊 {len(private_specs):,} + 群 {len(group_specs):,}）...")
        for batch in chunked(conversations, BATCH_SIZE):
            with conn.cursor() as cur:
                cur.executemany(
                    "INSERT INTO conversation (wecom_account_id, student_id, chat_type, group_name, "
                    "category, last_message_at, last_message_preview, unread_count) "
                    "VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
                    batch,
                )
            conn.commit()
        print(f"✓ 会话 {len(conversations):,} 条")

        # ---------- 消息 ----------
        print(f"→ 正在写入消息 {total_messages:,} 条 ...")
        msg_batch: list[tuple] = []
        conv_updates: list[tuple] = []
        inserted_msgs = 0

        for conv_id, spec in enumerate(all_specs, start=1):
            acc_id, student_id, chat_type, group_name, category, seed_unread = spec
            is_group = chat_type == "GROUP"
            msg_count = MESSAGES_PER_GROUP if is_group else MESSAGES_PER_STUDENT
            conv_base = base_time + timedelta(days=conv_id % 25)

            if is_group:
                sender = group_name or f"群{conv_id}"
                templates = GROUP_INCOMING_TEMPLATES
                force_pending = (acc_id + conv_id) % 2 == 1
            else:
                sender = f"学生{student_id:05d}"
                templates = INCOMING_TEMPLATES
                force_pending = conv_id % 2 == 1

            batch_msgs, conv_update = generate_messages_for_conv(
                conv_id,
                conv_base,
                templates,
                sender,
                msg_count,
                force_pending,
                initial_unread=seed_unread if is_group else 0,
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
                inserted_msgs += len(msg_batch)
                msg_batch.clear()
                if inserted_msgs % 100000 == 0:
                    print(f"  ... 已写入消息 {inserted_msgs:,} / {total_messages:,}")

        if msg_batch:
            with conn.cursor() as cur:
                cur.executemany(
                    "INSERT INTO chat_message (conversation_id, direction, sender_name, content, sent_at) "
                    "VALUES (%s, %s, %s, %s, %s)",
                    msg_batch,
                )
            conn.commit()
            inserted_msgs += len(msg_batch)

        print(f"✓ 消息 {inserted_msgs:,} 条")

        print("→ 同步会话最新消息摘要 ...")
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
            cur.execute("SET UNIQUE_CHECKS=1")
        conn.commit()
        print("✓ 会话摘要已同步")

        elapsed = time.time() - t0
        print("=" * 56)
        print(f"全部完成，耗时 {elapsed:.1f} 秒")
        print(f"每账号 {GROUPS_PER_ACCOUNT} 个群，每群 {MESSAGES_PER_GROUP} 条消息")
        print("数据库: message_takeover")
        print("=" * 56)

    finally:
        conn.close()


if __name__ == "__main__":
    main()

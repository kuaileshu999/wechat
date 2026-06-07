# 消息托管系统 API 文档

Base URL: `http://localhost:8080`

统一响应格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

失败时 `code = -1`，`message` 为错误说明。

---

## 1. 认证

### POST /api/auth/login

登录系统。

**请求体**

```json
{
  "username": "liting",
  "password": "123456"
}
```

**响应 data**

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户 ID |
| username | String | 登录名 |
| realName | String | 姓名 |
| role | Integer | 1-管理员 2-辅导老师 3-接管者 |
| avatarUrl | String | 头像 |

**测试账号（密码均为 `123456`）**

| username | 角色 | 姓名 |
|----------|------|------|
| admin | 管理员 | 管理员 |
| liting | 接管者 | 李婷 |
| zhang | 辅导老师 | 张明远 |
| wang | 辅导老师 | 王慧 |
| liu | 辅导老师 | 刘洋 |

---

## 2. 教研组

### GET /api/teaching-groups

获取启用的教研组列表。

---

## 3. 辅导老师

### GET /api/tutors

获取辅导老师列表（含企微账号）。

**Query 参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| teachingGroupId | Long | 否 | 按教研组筛选 |

### GET /api/tutors/{id}

获取单个辅导老师详情。

---

## 4. 接管者

### GET /api/takeover-managers

获取接管者列表，含当前托管中的辅导老师数量。

---

## 5. 托管配置

### GET /api/hosting-configs

获取全部托管配置。

### GET /api/hosting-configs/{id}

获取单个托管配置详情。

### POST /api/hosting-configs

创建托管配置。

**请求体**

```json
{
  "takeoverManagerId": 1,
  "effectiveType": 1,
  "scheduledStartAt": null,
  "description": "张老师外出培训，消息由李婷接管",
  "createdBy": 1,
  "accountIds": [1, 3]
}
```

| 字段 | 说明 |
|------|------|
| effectiveType | 1-立即生效 2-定时生效 |
| scheduledStartAt | 定时生效时间（effectiveType=2 时必填） |
| accountIds | 被托管的企微账号 ID 列表（推荐） |
| tutorIds | 兼容：整位老师 ID，将展开为其全部企微账号 |

**业务规则**

- 已被他人托管的企微账号会自动跳过，并写入 `skipReason`
- `effectiveType=1` 时创建后立即生效

### GET /api/hosting-assignments

分页查询账号级托管记录。

**Query：** `keyword`、`status`（1-接管中 2-已解除）、`accountId`、`page`、`pageSize`

### GET /api/hosting-assignments/stats

列表页统计卡片数据。

### POST /api/hosting-assignments/{id}/release

解除单个账号接管。请求体：`{ "operatorId": 1 }`

### POST /api/hosting-assignments/{id}/reactivate

重新接管已解除账号。请求体：`{ "operatorId": 1 }`

### POST /api/hosting-configs/{id}/activate

手动激活待生效配置。

**请求体**

```json
{ "operatorId": 1 }
```

### POST /api/hosting-configs/{id}/end

手动结束托管。

**请求体**

```json
{ "operatorId": 1 }
```

---

## 6. 会话（工作台）

### GET /api/conversations

分页查询会话列表。

**Query 参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| handlerUserId | Long | 当前处理人（接管者工作台必传） |
| stage | Integer | 1-转化期 2-承接期 3-已结课 |
| convType | Integer | 1-私聊 2-群聊 |
| tutorId | Long | 按辅导老师筛选 |
| keyword | String | 搜索学生昵称/群名称 |
| page | Integer | 页码，默认 1 |
| pageSize | Integer | 每页条数，默认 20，最大 100 |

**响应 data**

```json
{
  "list": [],
  "total": 0,
  "page": 1,
  "pageSize": 20
}
```

### GET /api/conversations/stats

工作台统计（顶部「接管中 · N 个账号 · M 条未读」）。

**Query 参数**

| 参数 | 类型 | 必填 |
|------|------|------|
| handlerUserId | Long | 是 |

**响应 data**

| 字段 | 说明 |
|------|------|
| hostingTutorCount | 托管中的辅导老师数 |
| totalUnread | 未读消息总数 |
| conversionCount | 转化期会话数 |
| undertakingCount | 承接期会话数 |
| completedCount | 已结课会话数 |

### GET /api/conversations/{id}

获取会话详情，并标记已读。

**Query 参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| readerUserId | Long | 当前阅读者 |

### POST /api/conversations/{id}/transfer

将聊天转接给其他老师处理。

**请求体**

```json
{
  "toHandlerUserId": 3,
  "operatorId": 2,
  "remark": "该学生问题较复杂，转给王老师"
}
```

---

## 7. 消息

### GET /api/messages

获取会话最近 50 条消息（仅保留 1 个月内数据）。

**Query 参数**

| 参数 | 类型 | 必填 |
|------|------|------|
| conversationId | Long | 是 |

### POST /api/messages/reply

发送回复（仅当前会话处理人可操作）。

**请求体**

```json
{
  "conversationId": 1,
  "senderUserId": 2,
  "content": "好的，我来帮你讲解这道题"
}
```

**权限规则**

- 托管期间：仅 `conversation_handler` 中的处理人可回复
- 未托管：原辅导老师可回复

---

## 8. 转接记录

### GET /api/transfer-logs

获取转接记录。

**Query 参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| tutorId | Long | 可选，按辅导老师筛选 |

**actionType 说明**

| 值 | 说明 |
|----|------|
| 1 | 开始托管 |
| 2 | 结束托管 |
| 3 | 转接聊天 |

---

## 9. 定时任务

| 任务 | 周期 | 说明 |
|------|------|------|
| 消息清理 | 每天 03:30 | 删除 1 个月前的消息 |
| 定时生效 | 每 5 分钟 | 激活到期的托管配置 |

---

## 10. 快速验证流程

```bash
# 1. 启动服务
mvn spring-boot:run

# 2. 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"liting","password":"123456"}'

# 3. 查看辅导老师
curl http://localhost:8080/api/tutors

# 4. 创建托管（立即生效）
curl -X POST http://localhost:8080/api/hosting-configs \
  -H 'Content-Type: application/json' \
  -d '{"takeoverManagerId":1,"effectiveType":1,"description":"测试托管","createdBy":1,"accountIds":[1]}'

# 5. 查看工作台会话
curl "http://localhost:8080/api/conversations?handlerUserId=2"

# 6. 回复消息
curl -X POST http://localhost:8080/api/messages/reply \
  -H 'Content-Type: application/json' \
  -d '{"conversationId":1,"senderUserId":2,"content":"你好，我来帮你"}'
```

---

## 11. 配置说明

`src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/wechat
    username: root
    password: 123456
server:
  port: 8080
```

数据库初始化脚本：`sql/schema.sql`  
演示数据脚本：`sql/seed.sql`（与 `DataInitializer` + `LitingWorkbenchSeeder` 一致）  
也可在服务首次启动时自动写入（`DataInitializer`）。

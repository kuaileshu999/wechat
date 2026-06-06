# 教育辅导工作台（IM）

基于截图还原的教育辅导即时通讯界面：**Java + Spring Boot** 后端，**Vue 3 + Vite** 前端。

## 功能概览

- 左侧：客服资料、转化期/承接期、私聊/群聊、搜索、老师标签筛选、会话列表
- 右侧：会话头（身份切换、转化期、转接）、消息气泡（含 KaTeX 公式）、输入发送
- 实时：STOMP + SockJS WebSocket 推送新消息

## 项目结构

```
chat/
├── backend/     # Spring Boot 3.2 + JPA + H2 + WebSocket
└── frontend/    # Vue 3 + Vite + Pinia + KaTeX
```

## 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+

## 启动方式

### 1. 后端（端口 8080）

```bash
cd backend
mvn spring-boot:run
```

### 2. 前端（端口 5173）

```bash
cd frontend
npm install
npm run dev
```

浏览器打开：http://localhost:5173

## API 摘要

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/profile` | 当前客服资料 |
| GET | `/api/tags` | 筛选标签 |
| GET | `/api/conversations` | 会话列表（phase, type, tagId, keyword） |
| GET | `/api/conversations/{id}/messages` | 消息历史 |
| POST | `/api/messages` | 发送消息 |
| PUT | `/api/conversations/{id}/read` | 标记已读 |
| PUT | `/api/conversations/{id}/transfer` | 转接老师 |
| WS | `/ws` | STOMP：`/topic/conversation/{id}`、`/topic/conversations` |

## 演示数据

启动后自动写入：客服「李婷」、学生「李明」等会话，含三角函数 LaTeX 消息，与 UI 原型一致。

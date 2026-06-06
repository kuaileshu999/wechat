# 消息托管系统 - 前端

Vue 3 + Vite + Element Plus，对接 `server` 目录下的 Spring Boot 后端。

## 启动

```bash
# 1. 启动后端（server 目录）
cd ../server && mvn spring-boot:run

# 2. 安装依赖并启动前端
cd cursor/wechat-hosting
npm install
npm run dev
```

浏览器访问：http://localhost:5173

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员（全部菜单） |
| liting | 123456 | 接管者（工作台、转接记录） |
| zhang | 123456 | 辅导老师 |

## 功能页面

| 页面 | 路径 | 说明 |
|------|------|------|
| 消息工作台 | /workbench | 会话列表、聊天、回复、转接 |
| 接管配置 | /hosting-config | 创建/激活/结束托管（管理员） |
| 辅导老师管理 | /tutors | 查看老师及企微账号 |
| 接管者管理 | /takeover-managers | 查看接管者负载 |
| 转接记录 | /transfer-logs | 托管与转接历史 |

## 构建

```bash
npm run build
```

产物输出到 `dist/`，可部署到 Nginx 等静态服务器，需反向代理 `/api` 到后端 8080 端口。

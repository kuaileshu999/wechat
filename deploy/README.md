# 宝塔面板部署指南

服务器公网 IP 示例：`120.26.194.111`

## 一、首次部署（按顺序执行）

### 1. 本地推送代码

```bash
git add .
git commit -m "deploy config"
git push origin main
```

### 2. SSH 登录服务器

```bash
ssh root@120.26.194.111
```

### 3. 拉取代码

```bash
cd /root
git clone https://github.com/kuaileshu999/wechat.git
# 已有目录则：
cd /root/wechat && git pull origin main
```

### 4. 安装宝塔软件（软件商店）

- Nginx
- MySQL 8.0
- Node.js 版本管理器（18+）
- Java（JDK 17+，或用 Java 项目管理器）

### 5. 宝塔创建数据库

**数据库** → **添加数据库**

| 项 | 值 |
|----|-----|
| 数据库名 | wechat |
| 用户名 | wechat |
| 密码 | 自行设置 |

### 6. 配置环境变量

```bash
cd /root/wechat
cp deploy/.env.example deploy/.env
vi deploy/.env   # 修改 DB_PASSWORD、CORS_ORIGINS
```

`deploy/.env` 示例：

```env
DEPLOY_HOST=120.26.194.111
PROJECT_ROOT=/root/wechat
DB_USER=wechat
DB_PASSWORD=你的宝塔数据库密码
CORS_ORIGINS=http://120.26.194.111
```

### 7. 一键部署

```bash
cd /root/wechat
chmod +x deploy/*.sh
bash deploy/server-deploy.sh
```

脚本会自动：初始化数据库（若无表）→ 打包后端 → 构建前端 → 启动后端。

### 8. 宝塔配置网站（必须手动）

同一 IP 部署两个项目：

| 项目 | 访问地址 | 后端端口 | 前端目录 |
|------|----------|----------|----------|
| **自习室** | http://120.26.194.111/ | 8080 | `/root/wechat/Study_room/frontend/dist` |
| **消息托管** | http://120.26.194.111/wechat/ | 8090 | `/root/wechat/cursor/wechat-hosting/dist` |

1. **网站** → 站点设置 → **网站目录** 改为：
   ```
   /root/wechat/Study_room/frontend/dist
   ```

2. **网站** → **设置** → **配置文件**

   将 `deploy/baota-nginx.conf` 的内容复制到 `server { }` 块内（或 include）。

3. 保存并重载 Nginx。

### 9. 部署自习室

```bash
cd /root/wechat
cp Study_room/deploy/.env.example Study_room/deploy/.env
vi Study_room/deploy/.env   # 填写 DB_PASSWORD
chmod +x Study_room/deploy/*.sh deploy/*.sh
bash Study_room/deploy/server-deploy.sh
```

### 10. 部署消息托管（改用 8090 端口）

```bash
cd /root/wechat
vi deploy/.env   # 确认 SERVER_PORT=8090
bash deploy/server-deploy.sh
```

### 11. 阿里云安全组

ECS → 安全组 → 入方向放行 **80** 端口。

### 12. 访问

| 系统 | 地址 | 账号 |
|------|------|------|
| 自习室 | http://120.26.194.111/ | admin / Admin@123 |
| 消息托管 | http://120.26.194.111/wechat/ | liting / 123456 |

---

## 二、日常更新（改代码后）

```bash
cd /root/wechat
git pull origin main

# 自习室
bash Study_room/deploy/server-deploy.sh

# 消息托管
bash deploy/server-deploy.sh
```

详细说明见：**Study_room/deploy/README.md**

---

## 三、常用命令

```bash
# 消息托管 - 仅构建前端
bash deploy/build-frontend.sh

# 消息托管 - 仅打包后端
bash deploy/build-backend.sh

# 消息托管 - 重启后端
bash deploy/restart-backend.sh
tail -f deploy/app.log

# 自习室 - 一键部署
bash Study_room/deploy/server-deploy.sh
tail -f Study_room/deploy/app.log
```

---

## 四、目录说明

| 文件 | 说明 |
|------|------|
| `deploy/.env.example` | 环境变量模板 |
| `deploy/.env` | 服务器本地配置（勿提交 Git） |
| `deploy/baota-nginx.conf` | 宝塔 Nginx 配置片段 |
| `deploy/server-deploy.sh` | 一键部署脚本 |
| `deploy/init-db.sh` | 导入 `server/sql/schema.sql` |
| `server/src/main/resources/application-prod.yml` | 生产环境 Spring 配置 |

---

## 五、故障排查

| 现象 | 处理 |
|------|------|
| 打开 IP 显示错误项目 | 网站根目录应为 `Study_room/frontend/dist`；合并 `baota-nginx.conf` |
| 自习室登录失败 | `curl 127.0.0.1:8080/api/auth/login`；查看 `Study_room/deploy/app.log` |
| 消息托管页面 404 | 访问 `/wechat/`；重新执行 `bash deploy/server-deploy.sh` |
| 消息托管 API 失败 | `curl 127.0.0.1:8090/api/teaching-groups`；确认 `SERVER_PORT=8090` |
| 跨域错误 | `CORS_ORIGINS=http://120.26.194.111`，重启消息托管后端 |
| `JAVA_HOME environment variable is not defined` | 安装 JDK 17，或设置 `JAVA_HOME=/usr/lib/jvm/java-17-openjdk` |
| `.env: -Xmx512m: command not found` | `JAVA_OPTS="-Xms256m -Xmx512m"`（须加引号） |

完整排查见 **Study_room/deploy/README.md**

---

## 使用宝塔 Java 项目管理器（可选）

| 项目 | jar 路径 | 端口 |
|------|----------|------|
| 自习室 | `/root/wechat/Study_room/backend/target/study-room-backend-1.0.0.jar` | 8080 |
| 消息托管 | `/root/wechat/server/target/message-hosting-1.0.0.jar` | 8090 |

消息托管启动参数：`-Dspring.profiles.active=prod`

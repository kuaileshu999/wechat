# 阿里云双项目部署说明

服务器 IP：`120.26.194.111`  
代码目录：`/root/wechat`

同一台服务器运行两个系统，通过 **Nginx 路径** 和 **不同后端端口** 区分。

---

## 一、访问地址与端口

| 系统 | 浏览器地址 | 后端端口 | Nginx 路由 | 前端目录 |
|------|------------|----------|------------|----------|
| **自习室** | http://120.26.194.111/ | **8080** | `/` + `/api/` | `/root/wechat/Study_room/frontend/dist` |
| **消息托管** | http://120.26.194.111/wechat/ | **8090** | `/wechat/` + `/wechat/api/` | `/root/wechat/cursor/wechat-hosting/dist` |

### 默认账号

| 系统 | 用户名 | 密码 |
|------|--------|------|
| 自习室 | admin | Admin@123 |
| 消息托管 | liting | 123456 |

### 数据库

| 系统 | 数据库名 |
|------|----------|
| 自习室 | `study_room` |
| 消息托管 | `wechat` |

---

## 二、架构示意

```
浏览器
  │
  ├─ http://120.26.194.111/          → Nginx 根目录（自习室前端）
  ├─ http://120.26.194.111/api/      → 127.0.0.1:8080（自习室后端）
  │
  ├─ http://120.26.194.111/wechat/       → 消息托管前端
  └─ http://120.26.194.111/wechat/api/   → 127.0.0.1:8090（消息托管后端）
```

---

## 三、首次部署（完整步骤）

### 1. 登录服务器并拉代码

```bash
ssh root@120.26.194.111
cd /root/wechat
git pull origin main
```

### 2. 安装依赖（宝塔软件商店）

- Nginx
- MySQL 8.0
- Node.js 18+
- JDK 17+

设置 Java 环境（若 `mvn` 报 JAVA_HOME 错误）：

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
```

### 3. 初始化自习室数据库

```bash
cd /root/wechat

# 建库建表（不要带 study_room 参数，schema.sql 会自动 CREATE DATABASE）
mysql -uroot -p'你的MySQL密码' < Study_room/backend/src/main/resources/db/schema.sql

# 初始数据
mysql -uroot -p'你的MySQL密码' study_room < Study_room/backend/src/main/resources/db/data.sql
```

> 密码含 `!` 等特殊字符时，用单引号：`mysql -uroot -p'!Xxx0818kls999' ...`

### 4. 部署自习室

```bash
cd /root/wechat
cp Study_room/deploy/.env.example Study_room/deploy/.env
vi Study_room/deploy/.env
```

`Study_room/deploy/.env` 示例：

```env
DEPLOY_HOST=120.26.194.111
PROJECT_ROOT=/root/wechat/Study_room
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=study_room
DB_USER=root
DB_PASSWORD=你的MySQL密码
SERVER_PORT=8080
```

执行部署：

```bash
chmod +x Study_room/deploy/*.sh deploy/*.sh
bash Study_room/deploy/server-deploy.sh
```

### 5. 部署消息托管

```bash
cd /root/wechat
cp deploy/.env.example deploy/.env   # 若尚未创建
vi deploy/.env
```

确认 `deploy/.env` 中：

```env
SERVER_PORT=8090
DB_PASSWORD=消息托管数据库密码
CORS_ORIGINS=http://120.26.194.111
```

执行部署：

```bash
bash deploy/server-deploy.sh
```

### 6. 配置宝塔 Nginx（必做）

1. **网站** → 选中 `120.26.194.111` → **网站目录** 改为：
   ```
   /root/wechat/Study_room/frontend/dist
   ```

2. **设置** → **配置文件**，在 `server { }` 内合并 `deploy/baota-nginx.conf` 全部内容。

3. 保存并重载 Nginx。

### 7. 阿里云安全组

ECS 控制台 → 安全组 → 入方向放行 **80** 端口。

### 8. 验证

```bash
# 自习室后端
curl -s http://127.0.0.1:8080/api/auth/login \
  -X POST -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin@123"}'

# 消息托管后端
curl -s http://127.0.0.1:8090/api/teaching-groups
```

浏览器访问：

- http://120.26.194.111/ （自习室）
- http://120.26.194.111/wechat/ （消息托管）

---

## 四、日常更新

### 只更新自习室

```bash
cd /root/wechat
git pull origin main
bash Study_room/deploy/server-deploy.sh
```

### 只更新消息托管

```bash
cd /root/wechat
git pull origin main
bash deploy/server-deploy.sh
```

### 两个都更新

```bash
cd /root/wechat
git pull origin main
bash Study_room/deploy/server-deploy.sh
bash deploy/server-deploy.sh
```

Nginx 配置一般无需改动；改完代码后浏览器 **Ctrl+F5** 强刷。

---

## 五、常用命令

```bash
# 查看端口占用
ss -tlnp | grep -E '8080|8090'

# 自习室日志
tail -f /root/wechat/Study_room/deploy/app.log

# 消息托管日志
tail -f /root/wechat/deploy/app.log

# 手动重启自习室后端
bash /root/wechat/Study_room/deploy/restart-backend.sh

# 手动重启消息托管后端
bash /root/wechat/deploy/restart-backend.sh
```

---

## 六、故障排查

| 现象 | 原因 / 处理 |
|------|-------------|
| 打开 IP 仍是消息托管 | 宝塔网站根目录未改为 `Study_room/frontend/dist`；Nginx 未合并新配置 |
| `JAVA_HOME environment variable is not defined` | 安装 JDK 17 并设置 `JAVA_HOME`（见第三节） |
| `Unknown database 'study_room'` | 先执行 `schema.sql`（不带库名参数） |
| `Access denied` / `using password: NO` | MySQL 密码错误或含 `!` 未加引号 |
| 自习室登录 502 | 8080 后端未启动：`tail Study_room/deploy/app.log` |
| 消息托管 404 | 前端未用 `/wechat/` 路径重新构建；执行 `bash deploy/server-deploy.sh` |
| 消息托管 API 失败 | 8090 未启动；检查 `deploy/.env` 中 `SERVER_PORT=8090` |
| 两个项目抢 8080 端口 | 消息托管必须用 **8090**，自习室用 **8080** |

---

## 七、相关文件

| 路径 | 说明 |
|------|------|
| `Study_room/deploy/server-deploy.sh` | 自习室一键部署 |
| `Study_room/deploy/.env.example` | 自习室环境变量模板 |
| `Study_room/backend/src/main/resources/db/` | 自习室 SQL 脚本 |
| `deploy/server-deploy.sh` | 消息托管一键部署 |
| `deploy/.env.example` | 消息托管环境变量模板 |
| `deploy/baota-nginx.conf` | Nginx 双项目配置片段 |

---

## 八、本地开发（参考）

| 项目 | 前端 | 后端 |
|------|------|------|
| 自习室 | `cd Study_room/frontend && npm run dev` → :5173 | `cd Study_room/backend && mvn spring-boot:run` → :8080 |
| 消息托管 | `cd cursor/wechat-hosting && npm run dev` → :5173 | `cd server && mvn spring-boot:run` → :8080 |

本地开发时两个项目仍各自独立；仅生产环境共用同一 IP，通过 Nginx 分流。

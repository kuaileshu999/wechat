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

1. **网站** → **添加站点**
   - 域名：`120.26.194.111`
   - 根目录：`/root/wechat/cursor/wechat-hosting/dist`
   - PHP：纯静态

2. **网站** → **设置** → **配置文件**

   将 `deploy/baota-nginx.conf` 的内容复制到 `server { }` 块内（或 include）。

3. 保存并重载 Nginx。

### 9. 阿里云安全组

ECS → 安全组 → 入方向放行 **80** 端口。

### 10. 访问

浏览器打开：**http://120.26.194.111**

| 用户名 | 密码 |
|--------|------|
| liting | 123456 |
| admin  | 123456 |

---

## 二、日常更新（改代码后）

```bash
cd /root/wechat
git pull origin main
bash deploy/server-deploy.sh
```

宝塔网站根目录无需改动。

---

## 三、常用命令

```bash
# 仅初始化数据库
bash deploy/init-db.sh

# 仅构建前端
bash deploy/build-frontend.sh

# 仅打包后端
bash deploy/build-backend.sh

# 重启后端
bash deploy/restart-backend.sh

# 查看后端日志
tail -f deploy/app.log
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
| 「没有找到站点」 | 检查网站根目录是否指向 `cursor/wechat-hosting/dist` |
| 页面能开，登录失败 | 检查 Nginx `/api/` 反向代理；`curl 127.0.0.1:8080/api/teaching-groups` |
| 跨域错误 | 确认 `deploy/.env` 中 `CORS_ORIGINS` 含 `http://120.26.194.111`，重启后端 |
| 后端启动失败 | `tail -f deploy/app.log`，检查 MySQL 账号密码 |
| `.env: -Xmx512m: command not found` | `JAVA_OPTS` 须加引号：`JAVA_OPTS="-Xms256m -Xmx512m"` |

---

## 六、使用宝塔 Java 项目管理器（可选）

若不想用 `restart-backend.sh`，可在宝塔 **Java 项目管理器** 中：

- jar 路径：`/root/wechat/server/target/message-hosting-1.0.0.jar`
- 端口：`8080`
- 启动参数：`-Dspring.profiles.active=prod`

环境变量在 Java 项目管理器中配置 `DB_PASSWORD`、`CORS_ORIGINS` 等，与 `deploy/.env` 保持一致。

# 自习室管理系统

面向线下学科培训机构的收费与消课管理系统，支持 10 家分店、100 人并发录入。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17+、Spring Boot 3.4、Spring Security、JWT、JPA |
| 数据库 | MySQL 8 |
| 前端 | Vue 3、Vite、Element Plus、Pinia、Axios |

## 功能模块

- **登录**：用户名密码登录；连续错误 10 次锁定；管理员重置密码并解锁
- **权限管理**：校区授权、菜单/按钮权限、停用全部权限、密码规则校验
- **员工管理**：列表、新建、在职/离职（离职不可登录）
- **学员管理**：列表、新建、Excel 批量导入
- **课程类型 / 课程管理**：按校区隔离，启用/停用
- **订单管理**：新建订单、退费、修改审计日志
- **消课管理**：单个/批量消课、按金额或课时、修改留痕
- **财务管理**：按天/月/校区统计收款、已消课、待消课金额

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
mysql -u root -p < backend/src/main/resources/db/data.sql
```

修改 `backend/src/main/resources/application.yml` 中的数据库账号密码。

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

服务地址：`http://localhost:8080`

首次启动会自动校正管理员密码。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问：`http://localhost:5173`

### 默认账号

- 用户名：`admin`
- 密码：`Admin@123`

## 项目结构

```
Study_room/
├── backend/                 # Spring Boot 后端
│   └── src/main/resources/db/
│       ├── schema.sql       # 表结构
│       └── data.sql         # 初始数据（10 校区、角色、权限）
└── frontend/                # Vue3 H5 管理端
    └── src/views/           # 各业务页面
```

## 学员批量导入格式

Excel 第一行为表头，从第二行起：

| 列 | 内容 |
|----|------|
| A | 学员姓名 |
| B | 手机号 |
| C | 备注（可选） |

## API 概览

| 模块 | 路径前缀 |
|------|----------|
| 认证 | `/api/auth` |
| 员工 | `/api/employees` |
| 学员 | `/api/students` |
| 课程类型 | `/api/course-types` |
| 课程 | `/api/courses` |
| 订单 | `/api/orders` |
| 消课 | `/api/consumptions` |
| 财务 | `/api/finance` |
| 审计 | `/api/audit-logs` |

## 注意事项

- 建议使用 **Java 17–21** 运行后端（当前环境若为 Java 24+，需确保 Lombok 版本兼容）
- 生产环境请修改 `jwt.secret` 和数据库密码
- 前端开发模式已配置 `/api` 代理到 `8080`

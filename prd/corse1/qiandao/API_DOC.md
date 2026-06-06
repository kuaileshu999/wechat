# 签到系统后端接口文档

本后端基于 Node.js + Express + SQLite 构建。

## 基础信息
- **Base URL**: `http://localhost:3000/api`
- **认证方式**: Bearer Token (JWT)

---

## 1. 用户认证

### 1.1 登录/注册 (自动注册)
如果用户不存在，则自动创建一个新账号；如果用户存在，则尝试登录。

- **URL**: `/auth/login`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "username": "your_username",
    "password": "your_password"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "message": "Login successful",
    "token": "eyJhbGciOiJIUzI1Ni...",
    "user": {
      "id": 1,
      "username": "your_username"
    }
  }
  ```
- **Error Response (401 Unauthorized)**:
  ```json
  {
    "message": "Invalid password"
  }
  ```

---

## 2. 签到功能
所有签到接口均需要在 Header 中携带 `Authorization: Bearer <token>`。

### 2.1 每日签到
记录当天的签到。同一天只能签到一次。

- **URL**: `/checkin`
- **Method**: `POST`
- **Headers**:
  - `Authorization: Bearer <your_token>`
- **Success Response (200 OK)**:
  ```json
  {
    "message": "Check-in successful",
    "checkIn": {
      "id": 1,
      "userId": 1,
      "checkInDate": "2024-05-27",
      "createdAt": "...",
      "updatedAt": "..."
    }
  }
  ```
- **Error Response (400 Bad Request)**:
  ```json
  {
    "message": "Already checked in today"
  }
  ```

### 2.2 获取签到历史
获取当前用户的所有签到记录。

- **URL**: `/checkin/history`
- **Method**: `GET`
- **Headers**:
  - `Authorization: Bearer <your_token>`
- **Success Response (200 OK)**:
  ```json
  {
    "message": "History fetched successfully",
    "history": [
      {
        "id": 1,
        "userId": 1,
        "checkInDate": "2024-05-27"
      }
    ]
  }
  ```

### 2.3 检查今日是否已签到
用于 H5 页面加载时判断按钮状态。

- **URL**: `/checkin/today`
- **Method**: `GET`
- **Headers**:
  - `Authorization: Bearer <your_token>`
- **Success Response (200 OK)**:
  ```json
  {
    "checkedIn": true
  }
  ```

---

## 如何运行
1. 安装依赖: `npm install`
2. 启动服务: `npm start`
3. 数据库会自动创建在项目根目录的 `database.sqlite` 文件中。

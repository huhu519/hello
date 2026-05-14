# 私人轻量化聊天 + 小型文件传输系统

## 项目简介

专为个人私密小圈子设计的轻量级聊天和文件传输系统，部署在个人云服务器上，外网可访问。

## 功能特性

- **严格准入**: 手机号唯一绑定、邀请码注册、最多15人限制
- **聊天功能**: 一对一私聊、公共聊天室、群聊
- **文件传输**: 支持图片和任意文件上传下载
- **容量限制**: 单会话30MB、总容量1GB
- **表情禁止**: 前后端双重过滤
- **WebSocket实时**: 消息即时推送

## 技术栈

### 后端
- Spring Boot 3.2
- MyBatis Plus
- MySQL 8.0
- WebSocket (STOMP)

### 前端
- Vue 3 + Vite
- Element Plus
- Axios
- SockJS + STOMP.js

## 部署步骤

### 1. 数据库准备
执行 `backend/src/main/resources/schema.sql` 初始化数据库

### 2. 后端启动
```bash
cd backend
# 修改 application.yml 中的数据库配置
mvn clean package
java -jar target/private-chat-system-1.0.0.jar
```

### 3. 前端部署
```bash
cd frontend
npm install
npm run build
# 将 dist 目录部署到 Nginx
```

### 4. Nginx配置
参考 `deploy/nginx.conf`

## 约束规则（代码硬编码）

1. 最大用户数：15人
2. 用户ID：8位数字，首位非0
3. 单会话存储：30MB
4. 总存储容量：1GB
5. 禁止发送表情
6. 必须手机号+邀请码注册

## 使用说明

1. 管理员通过接口创建邀请码
2. 用户使用邀请码注册账号
3. 登录后可以添加好友、聊天、传文件

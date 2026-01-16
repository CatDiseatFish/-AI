# Docker 一键启动指南（轻量级版本）

本项目支持完整的 Docker 打包，可以一键启动整个应用系统。

## 📋 前置要求

- **Docker**: [下载安装](https://www.docker.com/products/docker-desktop)
- **Docker Compose**: 通常随 Docker Desktop 一起安装

验证安装：
```bash
docker --version
docker-compose --version
```

## 🎯 为什么这个方案更好？

✅ **只需要 2 个基础镜像**（而不是 5 个）
- `eclipse-temurin:17-jre` （~450MB）
- `nginx:alpine` （~30MB）

✅ **极速构建**（几秒钟）
✅ **超稳定**（依赖少，变量少）
✅ **极低网络风险**

---

## 🚀 快速启动（3 步）

### 步骤 1️⃣：本地构建前端（一次性）

```bash
cd frontend/user-web

# 安装依赖
npm install

# 构建
npm run build

# 验证生成了 dist 文件夹
cd ../../
```

**预期结果**：生成 `frontend/user-web/dist/` 文件夹

### 步骤 2️⃣：构建 Docker 镜像

```bash
# 在项目根目录运行
docker compose build

# 查看构建进度
# 大约 30-60 秒完成
```

**只会拉取 2 个基础镜像**

### 步骤 3️⃣：启动服务

```bash
docker compose up -d

# 查看运行状态
docker compose ps
```

**等待 5-10 秒，服务完全启动**

---

## 🌐 访问应用

| 服务 | URL |
|------|-----|
| 前端应用 | http://localhost |
| 后端 API | http://localhost:8080 |

---

## 📊 查看日志

```bash
# 查看所有日志
docker compose logs -f

# 查看特定服务
docker compose logs -f backend
docker compose logs -f frontend
```

---

## 🛑 停止服务

```bash
# 停止
docker compose down

# 停止并删除所有数据
docker compose down -v
```

---

## 🔧 文件结构（Docker 需要）

```
项目根目录/
├── backend/
│   ├── target/
│   │   └── ai-story-studio-server-0.0.1-SNAPSHOT.jar  ✅ 必需
│   └── Dockerfile.backend
├── frontend/
│   └── user-web/
│       ├── dist/                                       ✅ 必需
│       └── Dockerfile.frontend
├── Dockerfile.frontend
├── docker-compose.yml
└── nginx.conf
```

**关键点**：
- `backend/target/*.jar` 必须存在（需要先 `mvn clean package`）
- `frontend/user-web/dist/` 必须存在（需要先 `npm run build`）

---

## 📝 前端 Build 详细步骤

如果 npm install 出错，尝试：

```bash
cd frontend/user-web

# 清除缓存
rm -r node_modules package-lock.json

# 重新安装
npm install

# 构建
npm run build

# 验证
dir dist  # Windows
ls dist   # Linux/Mac
```

---

## 🐛 常见问题

### ❌ 前端找不到 dist 文件夹

**原因**：没有运行 `npm run build`

**解决**：
```bash
cd frontend/user-web
npm install
npm run build
cd ../../
```

### ❌ 后端找不到 JAR 文件

**原因**：没有编译后端代码

**解决**：
```bash
cd backend
mvn clean package -DskipTests
cd ..
```

### ❌ Docker build 失败

**症状**：`COPY dist failed: file not found`

**解决**：
```bash
# 确认 dist 文件夹存在
ls frontend/user-web/dist

# 确认 JAR 文件存在
ls backend/target/*.jar

# 然后重新构建
docker compose build --no-cache
```

### ❌ 容器启动后立即退出

**查看日志**：
```bash
docker compose logs backend
docker compose logs frontend
```

---

## 🌍 终极方案：完全离线启动

如果需要在无网络环境启动，可以提前保存镜像。

### 准备阶段（有网络时）

在你的机器上：
```bash
# 拉取基础镜像
docker pull eclipse-temurin:17-jre
docker pull nginx:alpine

# 保存为文件
docker save eclipse-temurin:17-jre nginx:alpine -o base-images.tar

# 验证
ls -lh base-images.tar
```

### 交付给别人

1. 提供以下文件：
   - 项目源码（包括 frontend/user-web/dist/）
   - base-images.tar

2. 别人运行：
```bash
# 加载镜像
docker load -i base-images.tar

# 构建和启动
docker compose build
docker compose up -d
```

---

## 📊 构建耗时对比

| 方案 | 镜像数 | 总大小 | 构建时间 | 稳定性 |
|------|--------|--------|---------|--------|
| 旧方案（Maven+多服务） | 5+ | 2GB+ | 10+ 分钟 | 一般 |
| **新方案（轻量级）** | **2** | **500MB** | **1 分钟** | **优秀** |

---

## 🎯 下一步

- 如果需要数据库/Redis/RabbitMQ，查看 [BACKEND_DEPLOYMENT.md](./BACKEND_DEPLOYMENT.md)
- 生产部署建议见末尾

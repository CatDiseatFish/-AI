# 后端项目打包和部署指南

## 📋 项目技术栈

- **框架**: Spring Boot 3.2.5
- **ORM**: MyBatis-Plus 3.5.12
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **数据迁移**: Flyway (自动执行 SQL 脚本)
- **认证**: JWT
- **存储**: 阿里云 OSS

## 🎯 打包选项

### 选项 1️⃣：完整的 Docker 方案（推荐）

使用已创建的 Docker 配置，一键启动所有依赖。

```bash
docker-compose up -d
```

这会自动处理：
- ✅ MySQL 数据库初始化
- ✅ Flyway 自动执行数据库脚本
- ✅ Redis 配置
- ✅ RabbitMQ 配置
- ✅ 后端服务启动

详见 [DOCKER_SETUP.md](./DOCKER_SETUP.md)

---

### 选项 2️⃣：独立后端 JAR 包

打包后端为可执行 JAR，适合已有基础设施的情况。

#### 📦 打包步骤

```bash
# 1. 进入后端目录
cd backend

# 2. 清理并构建
mvn clean package -DskipTests

# 3. 生成的 JAR 文件位置
# target/ai-story-studio-server-0.0.1-SNAPSHOT.jar
```

#### 🚀 启动 JAR

**前提条件**：确保以下服务已启动
- MySQL 8.0（端口 3306）
- Redis（端口 6379）
- RabbitMQ（端口 5672）

```bash
# 基础启动
java -jar ai-story-studio-server-0.0.1-SNAPSHOT.jar

# 或使用环境变量配置（推荐）
java -Xmx512m -Xms256m \
  -Dspring.datasource.url="jdbc:mysql://localhost:3306/ai_story_studio" \
  -Dspring.datasource.username="root" \
  -Dspring.datasource.password="1234" \
  -Dspring.redis.host="localhost" \
  -Dspring.rabbitmq.host="localhost" \
  -jar ai-story-studio-server-0.0.1-SNAPSHOT.jar
```

---

## 🗄️ 数据库配置

### 自动初始化（Flyway）

项目使用 **Flyway** 自动执行数据库迁移脚本，无需手动导入。

**自动执行的 SQL 脚本**（按顺序）：
1. `V1__init.sql` - 初始化基础表结构
2. `V2__add_new_features.sql` - 新功能表
3. `V3__create_ai_models_table.sql` - AI 模型表
4. `V4__fix_foreign_keys_and_constraints.sql` - 修复约束
5. `V5__update_wallet_default_balance.sql` - 钱包配置
6. `V6__add_job_result_fields.sql` - 任务结果字段
7. `V7__add_gpt4o_image_vip_model.sql` - 图像 VIP 模型
8. `V8__add_gpt4o_language_model.sql` - 语言模型
9. `V9__add_character_library_thumbnail.sql` - 角色库缩略图
10. `V10__add_prop_tables.sql` - 道具表
11. `V11__add_pprop_to_shot_bindings.sql` - 镜头绑定

**配置参数**（application.yml）：
```yaml
spring:
  flyway:
    enabled: true                    # 启用 Flyway
    baseline-on-migrate: true        # 首次运行时创建基线
    locations: classpath:db/migration  # SQL 脚本位置
```

---

## 🔧 核心配置说明

### 1️⃣ 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_story_studio
    username: root
    password: 1234
```

**重要**：生产环境使用环境变量：
```bash
-Dspring.datasource.url="jdbc:mysql://host:3306/db"
-Dspring.datasource.username="user"
-Dspring.datasource.password="pass"
```

### 2️⃣ MyBatis-Plus 配置

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # 自动转换下划线到驼峰
```

**特性**：
- 自动 CRUD（不需要手写 SQL）
- 支持 Lambda 表达式查询
- 分页、分组等高级功能

### 3️⃣ Redis 配置

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      database: 4
```

**用途**：
- Session 管理
- 缓存数据
- 分布式锁

### 4️⃣ RabbitMQ 配置

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:admin}
    password: ${RABBITMQ_PASSWORD:admin123}
```

**用途**：
- 异步任务处理
- 视频生成队列
- 消息解耦

### 5️⃣ JWT 认证配置

```yaml
jwt:
  secret: "your-secret-key"           # 修改此密钥！
  expiration: 604800000               # 7 天（毫秒）
  header: Authorization
  prefix: "Bearer "
```

### 6️⃣ 阿里云 OSS 存储

```yaml
storage:
  provider: oss
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    bucket: yuanmeng-logo
    access-key-id: ${OSS_ACCESS_KEY_ID}
    access-key-secret: ${OSS_ACCESS_KEY_SECRET}
```

**获取 OSS 密钥**：
1. 登录 [阿里云控制台](https://console.aliyun.com)
2. 创建 OSS Bucket
3. 在 RAM 中创建访问密钥
4. 配置到环境变量

### 7️⃣ AI 模型配置

```yaml
ai:
  vectorengine:
    base-url: https://api.vectorengine.ai
    api-key: ${AI_API_KEY}           # 替换为实际 API 密钥
  image:
    default-model: gemini-3-pro-image-preview
```

---

## 📦 完整启动脚本

### Windows PowerShell

```powershell
# 1. 构建
cd backend
mvn clean package -DskipTests

# 2. 启动所有依赖（使用 Docker）
cd ..
docker-compose up -d

# 3. 运行后端（等待 15 秒让数据库初始化）
Start-Sleep -Seconds 15
java -Xmx512m -Xms256m -jar backend/target/ai-story-studio-server-0.0.1-SNAPSHOT.jar
```

### Linux/Mac

```bash
# 1. 构建
cd backend
mvn clean package -DskipTests

# 2. 启动所有依赖
cd ..
docker-compose up -d

# 3. 运行后端
sleep 15
java -Xmx512m -Xms256m -jar backend/target/ai-story-studio-server-0.0.1-SNAPSHOT.jar
```

---

## 🌍 环境变量配置（生产环境）

创建 `.env` 文件或在系统中设置：

```bash
# 数据库
SPRING_DATASOURCE_URL=jdbc:mysql://prod-db.example.com:3306/ai_story_studio
SPRING_DATASOURCE_USERNAME=prod_user
SPRING_DATASOURCE_PASSWORD=prod_password

# Redis
REDIS_HOST=prod-redis.example.com
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOST=prod-rabbitmq.example.com
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=prod_user
RABBITMQ_PASSWORD=prod_password

# JWT（修改密钥！）
JWT_SECRET=your-super-secret-key-change-in-production

# OSS
OSS_ACCESS_KEY_ID=your_access_key_id
OSS_ACCESS_KEY_SECRET=your_access_key_secret

# AI API
AI_API_KEY=your_ai_api_key
```

运行时加载：
```bash
source .env  # Linux/Mac
# 或在 Windows PowerShell 中手动设置
java -jar app.jar
```

---

## 🔍 验证部署

```bash
# 检查健康状态
curl http://localhost:8080/actuator/health

# 检查指标
curl http://localhost:8080/actuator/metrics

# 检查应用信息
curl http://localhost:8080/actuator/info
```

---

## 🚨 常见问题

### 1️⃣ Flyway 执行失败

**症状**：`Flyway migration failed`

**解决**：
```bash
# 检查数据库是否存在
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS ai_story_studio;"

# 清除 Flyway 元数据并重新初始化
# 登录 MySQL 后删除 flyway_schema_history 表
DROP TABLE flyway_schema_history;
```

### 2️⃣ 无法连接 Redis/RabbitMQ

**症状**：`Connection refused`

**解决**：
```bash
# 检查服务是否运行
docker-compose ps

# 重启服务
docker-compose down
docker-compose up -d
```

### 3️⃣ OSS 认证失败

**症状**：`Access Denied`

**解决**：
- 确认 Access Key ID/Secret 正确
- 检查 OSS Bucket 权限设置
- 验证 Endpoint 与 Region 对应

### 4️⃣ JWT Token 过期

**症状**：`401 Unauthorized`

**解决**：
- 刷新 Token
- 或修改 `jwt.expiration` 增加有效期

---

## 📊 打包大小

| 组件 | 大小 |
|------|------|
| JAR 文件 | ~60-80 MB |
| Docker 镜像 | ~400-500 MB |
| 完整 Docker Compose | ~2 GB（含数据库）|

---

## 🔐 安全建议

1. **修改所有默认密码**
   - MySQL 密码
   - Redis 密码
   - RabbitMQ 密码
   - JWT 密钥

2. **使用环境变量** 而不是硬编码敏感信息

3. **启用 HTTPS** 在生产环境

4. **限制数据库访问** 只允许特定 IP

5. **定期备份** MySQL 数据

6. **监控和日志** 使用 ELK Stack 或类似工具

---

## 📞 技术支持

- 查看 Spring Boot 官方文档：https://spring.io/projects/spring-boot
- MyBatis-Plus 文档：https://baomidou.com/
- Flyway 文档：https://flywaydb.org/
- 阿里云 OSS 文档：https://help.aliyun.com/product/31815.html

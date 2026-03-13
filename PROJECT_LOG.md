# DudaNexus 项目运行日志

> **最后更新时间**: 2026-03-13 06:22
> **状态**: ✅ 所有服务正常运行

---

## 📋 项目概述

**项目名称**: DudaNexus (都达云台)
**项目类型**: 微服务架构 (Spring Cloud Alibaba)
**开发语言**: Java 17
**核心框架**: Spring Boot 3.2.0 + Dubbo 3.3.2

---

## 🏗️ 服务清单

### 已启动服务 (4个)

| 服务名 | Group | HTTP端口 | Dubbo端口 | 容器名 | 镜像 | 状态 |
|--------|-------|----------|-----------|--------|------|------|
| duda-id-generator | INFRA_GROUP | 9090 | 20880 | duda-id-generator | duda/id-generator-provider:1.0.0 | ✅ 健康 |
| duda-msg-provider | MSG_GROUP | 9091 | 自动分配 | duda-msg-provider | duda/msg-provider:1.0.0 | ✅ 运行中 |
| duda-user-provider | USER_GROUP | 8082 | 20880 | duda-user-provider | duda/user-provider:1.0.0 | ✅ 健康 |
| duda-user-api | USER_GROUP | 8083 | - | duda-user-api | duda/user-api:1.0.0 | ✅ 健康 |

### 服务依赖关系

```
duda-id-generator (基础设施)
    ↓
duda-user-provider (业务服务 - 依赖 ID生成器)
    ↓
duda-user-api (API层 - 依赖 Provider)

duda-msg-provider (独立服务 - 消息服务)
```

---

## 🚀 快速启动指南

### 方式一：Docker Compose (推荐)

```bash
# 进入项目目录
cd /Volumes/DudaDate/DudaNexus

# 启动所有服务（按依赖顺序）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f [服务名]

# 停止所有服务
docker-compose down
```

### 方式二：手动启动

```bash
# 创建网络
docker network create duda-network

# 启动 ID 生成器
docker run -d --name duda-id-generator \
  --network duda-network \
  -p 9090:9090 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SNOWFLAKE_MACHINE_ID=1 \
  duda/id-generator-provider:1.0.0

# 启动消息服务
docker run -d --name duda-msg-provider \
  --network duda-network \
  -p 9091:9091 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SPRING_CLOUD_NACOS_CONFIG_IMPORT_CHECK_ENABLED=false \
  duda/msg-provider:1.0.0

# 启动用户Provider
docker run -d --name duda-user-provider \
  --network duda-network \
  -p 8082:8082 -p 20880:20880 \
  -e SPRING_PROFILES_ACTIVE=dev \
  duda/user-provider:1.0.0

# 启动用户API
docker run -d --name duda-user-api \
  --network duda-network \
  -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=dev \
  duda/user-api:1.0.0
```

---

## 🌐 访问地址

### Swagger UI (API文档)

**主文档**: http://localhost:8083/swagger-ui/index.html

**文档接口**:
- 所有接口: http://localhost:8083/v3/api-docs

**可用接口分组** (36个路径):
1. 用户管理 (8个接口) - UserController
2. 用户认证 (10个接口) - AuthController
3. **用户认证V2** (14个接口) - AuthV2Controller ✅

### 健康检查

```bash
# ID生成器
curl http://localhost:9090/actuator/health

# 消息服务
curl http://localhost:9091/actuator/health

# 用户Provider
curl http://localhost:8082/actuator/health

# 用户API
curl http://localhost:8083/actuator/health
```

---

## 🔧 核心配置

### Nacos 配置

**服务器地址**: http://120.26.170.213:8848/nacos
**用户名/密码**: nacos/nacos
**命名空间**: duda-dev

**服务注册情况**:
- ✅ duda-id-generator (INFRA_GROUP)
- ✅ duda-msg-provider (MSG_GROUP)
- ✅ duda-user-provider (USER_GROUP)
- ✅ duda-user-api (USER_GROUP)

### 数据库配置

**MySQL**: 120.26.170.213:3306
**数据库名**: duda_nexus
**用户名/密码**: root/duda2024

### Redis 配置

**Redis**: 120.26.170.213:6379
**密码**: Duda@2025 (云服务器)

---

## 📝 关键配置文件

### 1. Dubbo 配置规范

**重要**: 项目使用多 Registry 配置支持跨 Group 调用

**ID生成器** (INFRA_GROUP):
```yaml
dubbo:
  registry:
    address: nacos://120.26.170.213:8848
    parameters:
      namespace: duda-dev
      group: INFRA_GROUP
```

**用户服务** (USER_GROUP):
```yaml
dubbo:
  registries:
    infraRegistry:
      address: nacos://120.26.170.213:8848
      parameters:
        group: INFRA_GROUP
      default: false
    userRegistry:
      address: nacos://120.26.170.213:8848
      parameters:
        group: USER_GROUP
      default: true
```

### 2. Swagger 配置修复

**问题**: AuthV2Controller 不显示在 Swagger 中

**解决**: 在 `duda-common-web/src/main/java/com/duda/common/web/config/OpenApiConfig.java` 添加：

```java
@Bean
public GroupedOpenApi allApi() {
    return GroupedOpenApi.builder()
            .group("all")
            .pathsToMatch("/**")
            .build();
}
```

**位置**: `/Volumes/DudaDate/DudaNexus/duda-common/duda-common-web/src/main/java/com/duda/common/web/config/OpenApiConfig.java`

### 3. MSG 服务启动修复

**问题**: Spring Cloud 2023 要求配置导入检查

**解决**: 添加环境变量
```bash
SPRING_CLOUD_NACOS_CONFIG_IMPORT_CHECK_ENABLED=false
```

---

## 🔨 构建和部署

### 编译项目

```bash
# 编译所有模块
mvn clean install -DskipTests

# 只编译特定模块
mvn clean package -DskipTests -pl duda-usercenter/duda-user-api -am

# 完全跳过测试
mvn clean package -Dmaven.test.skip=true
```

### 构建 Docker 镜像

```bash
# ID生成器
docker build -t duda/id-generator-provider:1.0.0 \
  duda-id-generator/duda-id-generator-provider/

# 消息服务
docker build -t duda/msg-provider:1.0.0 \
  duda-msg/duda-msg-provider/

# 用户Provider
docker build -t duda/user-provider:1.0.0 \
  duda-usercenter/duda-user-provider/

# 用户API
docker build -t duda/user-api:1.0.0 \
  duda-usercenter/duda-user-api/
```

---

## ⚠️ 常见问题

### 1. Dubbo 调用超时

**现象**: `Invoke remote method timeout`

**原因**:
- 容器间网络问题
- Dubbo 端口映射错误

**解决**:
- 确保容器在同一个 Docker 网络中
- 映射正确的 Dubbo 端口 (通常是自动分配的 20880)
- 检查防火墙设置

### 2. MSG 服务启动失败

**现象**: `No spring.config.import property has been defined`

**解决**: 添加环境变量
```yaml
environment:
  - SPRING_CLOUD_NACOS_CONFIG_IMPORT_CHECK_ENABLED=false
```

### 3. Swagger 接口不完整

**现象**: 部分 Controller 不显示

**解决**: 确保 `OpenApiConfig.java` 中有 `GroupedOpenApi` Bean

### 4. 容器网络通信问题

**现象**: 容器间无法通信

**解决**:
```bash
# 确保所有容器在同一个网络中
docker network ls | grep duda-network

# 查看容器网络
docker inspect [容器名] | grep -A 10 "Networks"

# 测试连通性
docker exec [容器A] ping [容器B]
```

---

## 📦 项目结构

```
DudaNexus/
├── duda-common/              # 公共模块
│   ├── duda-common-core      # 核心工具类
│   ├── duda-common-web       # Web配置 (Swagger、CORS等)
│   ├── duda-common-database  # 数据库配置
│   ├── duda-common-redis     # Redis配置
│   └── duda-common-security  # 安全配置
│
├── duda-id-generator/        # ID生成器 (INFRA_GROUP)
│   └── duda-id-generator-provider
│
├── duda-msg/                 # 消息服务 (MSG_GROUP)
│   ├── duda-msg-interface
│   └── duda-msg-provider
│
├── duda-usercenter/          # 用户中心 (USER_GROUP)
│   ├── duda-user-interface   # RPC接口定义
│   ├── duda-user-provider    # RPC服务实现
│   └── duda-user-api         # REST API层
│
├── docker-compose.yml        # Docker编排配置
├── pom.xml                   # Maven主配置
└── PROJECT_LOG.md           # 本日志文件
```

---

## 📊 当前API接口清单

### 用户管理 (UserController) - 8个接口

1. `POST /user/register` - 用户注册
2. `POST /user/login` - 用户登录
3. `POST /user/logout` - 用户登出
4. `POST /user/refresh-token` - 刷新令牌
5. `GET /user/{userId}` - 根据ID查询用户
6. `GET /user/username/{username}` - 根据用户名查询
7. `GET /user/page` - 分页查询用户
8. `PUT /user/update` - 更新用户信息

### 用户认证 (AuthController) - 10个接口

1. `POST /api/auth/register` - 注册
2. `POST /api/auth/login` - 登录
3. `POST /api/auth/logout` - 登出
4. `POST /api/auth/refresh` - 刷新令牌
5. `POST /api/auth/validate` - 验证令牌
6. `POST /api/auth/sms/send` - 发送短信
7. `GET /api/auth/user/info` - 获取用户信息
8-10. 测试接口

### 用户认证V2 (AuthV2Controller) - 14个接口 ✅

**都达网账户**:
1. `POST /api/auth/v2/platform-account/register/password` - 账号密码注册
2. `POST /api/auth/v2/platform-account/login/password` - 账号密码登录
3. `POST /api/auth/v2/platform-account/login/sms` - 短信验证码登录

**服务商**:
4. `POST /api/auth/v2/service-provider/register/password` - 账号密码注册
5. `POST /api/auth/v2/service-provider/login/password` - 账号密码登录
6. `POST /api/auth/v2/service-provider/login/sms` - 短信验证码登录

**平台管理员**:
7. `POST /api/auth/v2/platform-admin/register/password` - 账号密码注册
8. `POST /api/auth/v2/platform-admin/login/password` - 账号密码登录

**后台管理员**:
9. `POST /api/auth/v2/backend-admin/register/password` - 账号密码注册
10. `POST /api/auth/v2/backend-admin/login/password` - 账号密码登录

**通用接口**:
11. `POST /api/auth/v2/sms/send` - 发送短信验证码
12. `POST /api/auth/v2/refresh` - 刷新令牌
13. `POST /api/auth/v2/logout` - 登出
14. `GET /api/auth/v2/validate` - 验证令牌

---

## 🎯 下次启动检查清单

### 启动前检查

- [ ] Docker 已启动
- [ ] 检查端口占用: `lsof -i:8082,8083,9090,9091`
- [ ] 检查网络: `docker network ls | grep duda-network`
- [ ] Nacos 可访问: `curl http://120.26.170.213:8848/nacos`

### 启动步骤

1. **启动服务**
   ```bash
   cd /Volumes/DudaDate/DudaNexus
   docker-compose up -d
   ```

2. **检查服务状态**
   ```bash
   docker-compose ps
   # 所有服务应该是 "Up" 或 "healthy"
   ```

3. **检查健康状态**
   ```bash
   curl http://localhost:8083/actuator/health
   curl http://localhost:8082/actuator/health
   curl http://localhost:9090/actuator/health
   curl http://localhost:9091/actuator/health
   ```

4. **验证 Swagger**
   ```bash
   open http://localhost:8083/swagger-ui/index.html
   # 应该看到 36 个接口，包括 AuthV2Controller
   ```

5. **检查 Nacos 注册**
   - 登录 Nacos: http://120.26.170.213:8848/nacos
   - 切换到 `duda-dev` 命名空间
   - 查看服务列表，应该看到 4 个服务

### 测试接口

**测试 AuthV2Controller 接口**:
```bash
# 测试都达网账户登录
curl -X POST http://localhost:8083/api/auth/v2/platform-account/login/password \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Duda@2025"}'
```

---

## 📖 重要文档

| 文档 | 路径 | 说明 |
|------|------|------|
| 快速启动指南 | `docs/quick-start-guide.md` | 数据库初始化、Nacos配置 |
| Dubbo配置规范 | `docs/DUBBO_CONFIG_STANDARD.md` | 多Registry配置 |
| Docker部署手册 | `DOCKER_DEPLOYMENT_MANUAL.md` | Docker部署完整指南 |
| 项目状态总结 | `docs/project-status-summary.md` | 项目完成度 |
| 当前日志 | `PROJECT_LOG.md` | 本文件 |

---

## 🔄 版本历史

### 2026-03-13 - v1.0

**完成工作**:
- ✅ 成功启动所有 4 个服务
- ✅ 修复 Swagger 配置，AuthV2Controller 正常显示
- ✅ 修复 MSG 服务启动问题
- ✅ 配置多 Registry 支持

**已知问题**:
- Dubbo 调用偶尔超时 (容器网络)
- MSG 服务健康检查可能失败

---

## 💡 开发提示

### 添加新服务

1. 在对应目录创建 provider/api 模块
2. 配置 `bootstrap.yml` 中的 Dubbo registry
3. 创建 Dockerfile
4. 构建镜像
5. 更新 `docker-compose.yml`

### 修改 Swagger 配置

配置文件位置:
```
duda-common/duda-common-web/src/main/java/com/duda/common/web/config/OpenApiConfig.java
```

修改后需要重新编译:
```bash
mvn clean install -DskipTests -pl duda-common/duda-common-web
```

### 重新构建并部署

```bash
# 1. 编译模块
mvn clean package -Dmaven.test.skip=true -pl [模块名] -am

# 2. 构建镜像
docker build -t [镜像名]:1.0.0 [模块路径]

# 3. 重启服务
docker-compose up -d --force-recreate [服务名]
```

---

## 📞 故障排查

### 查看容器日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker logs [容器名] --tail=100

# 实时跟踪日志
docker logs -f [容器名]
```

### 进入容器调试

```bash
# 进入容器
docker exec -it [容器名] /bin/bash

# 查看进程
docker exec [容器名] ps aux

# 测试网络连通性
docker exec [容器A] ping [容器B]
```

### 重启单个服务

```bash
docker-compose restart [服务名]

# 或者
docker restart [容器名]
```

---

**日志维护**: 本文件应该在每次重大变更后更新
**下次启动**: 从"🎯 下次启动检查清单"开始

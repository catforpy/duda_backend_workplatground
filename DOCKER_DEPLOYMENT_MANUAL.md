# Docker 部署完整手册

## 目录
1. [核心问题诊断](#1-核心问题诊断)
2. [Docker 网络原理](#2-docker-网络原理)
3. [单服务部署流程](#3-单服务部署流程)
4. [多服务部署方案](#4-多服务部署方案)
5. [常见故障排查](#5-常见故障排查)
6. [生产环境建议](#6-生产环境建议)

---

## 1. 核心问题诊断

### 1.1 今天遇到的所有错误及根本原因

#### 错误 1: Swagger 返回 base64 编码 ❌
**现象:** `/v3/api-docs` 返回 base64 字符串而不是 JSON
**原因:** `WebMvcConfig.java` 使用了 `configureMessageConverters()` 替换了所有默认转换器
**解决:** 改用 `extendMessageConverters()` 保留 Spring Boot 默认配置
**位置:** `duda-common/duda-common-web/src/main/java/com/duda/common/web/config/WebMvcConfig.java:19`

#### 错误 2: IDEA Docker 尝试拉取 -alpine 镜像 ❌
**现象:** `ERROR: load metadata for docker.io/library/eclipse-temurin:17-jre-alpine`
**原因:** IDEA Docker 插件自动添加 `-alpine` 后缀
**解决:**
- Dockerfile 使用 ARG 参数: `ARG BASE_IMAGE=eclipse-temurin:17-jre`
- IDEA 配置中取消勾选 "Pull latest image before build"

#### 错误 3: Dockerfile 使用 Alpine 特定命令 ❌
**现象:** `/bin/sh: 1: apk: not found`
**原因:** Dockerfile 使用 Alpine 的 `apk` 命令,但基础镜像 `eclipse-temurin:17-jre` 不是 Alpine
**解决:** 替换为标准 Linux 命令
```dockerfile
# 错误 (Alpine 特定):
RUN apk add --no-cache tzdata

# 正确 (标准 Linux):
RUN mkdir -p /usr/share/zoneinfo/Asia && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
```

#### 错误 4: Dubbo 端口不匹配 ❌
**现象:** API 调用 Provider 超时,显示 `Timeout after 5000ms waiting for result`
**原因:**
- Provider 配置 `dubbo.protocol.port: -1` (自动分配,分配到 20880)
- Dockerfile 只暴露了 20882 端口
- API 容器无法访问 Provider 的 20880 端口

**解决:** 在 `bootstrap.yml` 中配置固定端口:
```yaml
dubbo:
  protocol:
    name: dubbo
    port: 20882  # 固定端口,匹配 Dockerfile EXPOSE
```

#### 错误 5: 容器无法连接到数据库 ❌
**现象:** `Communications link failure`, `Connection refused`
**原因:** 容器内的 `localhost` 指向容器自己,不是宿主机
**解决:** 使用 `host.docker.internal` 访问宿主机服务:
```yaml
url: jdbc:mysql://host.docker.internal:3306/duda_nexus
```

---

## 2. Docker 网络原理

### 2.1 为什么是 172.21.0.x?

当你创建 Docker 自定义网络时,Docker 会分配一个私有子网:

```bash
docker network create duda-network
```

Docker 自动分配 `172.21.0.0/16` 子网,其中:
- `duda-user-provider`: 172.21.0.2
- `duda-user-api`: 172.21.0.3
- 网关: 172.21.0.1

### 2.2 容器通信方式

**方式 1: 自定义 bridge 网络 (推荐) ✅**
```bash
docker network create duda-network
docker run --name provider --network duda-network ...
docker run --name api --network duda-network ...
```

**优点:**
- 容器间可以通过容器名互相访问
- DNS 解析: `duda-user-provider` → `172.21.0.2`
- 隔离性好,安全性高

**方式 2: host 网络模式**
```bash
docker run --name api --network host ...
```

**缺点:**
- 失去了容器网络隔离
- 端口冲突风险高
- 不推荐在生产环境使用

### 2.3 宿主机访问

**容器访问宿主机服务:**
```
host.docker.internal  # Docker Desktop 提供的 DNS 名称
```

**宿主机访问容器服务:**
```
localhost:8083  # 端口映射 -p 8083:8083
```

---

## 3. 单服务部署流程

### 3.1 准备工作

**检查本地环境:**
```bash
# 检查 Docker 是否运行
docker ps

# 检查 MySQL 是否运行
docker ps | grep mysql

# 检查端口占用
lsof -i:8082,8083,20882
```

### 3.2 打包应用

```bash
cd /Volumes/DudaDate/DudaNexus

# 打包 Provider
mvn clean package -DskipTests -pl duda-usercenter/duda-user-provider -am

# 打包 API
mvn clean package -DskipTests -pl duda-usercenter/duda-user-api -am
```

### 3.3 构建镜像

```bash
# 构建 Provider 镜像
cd duda-usercenter/duda-user-provider
docker build -t duda/user-provider:1.0.0 .

# 构建 API 镜像
cd ../duda-user-api
docker build -t duda/user-api:1.0.0 .
```

### 3.4 创建网络

```bash
docker network create duda-network
```

### 3.5 启动容器

**启动 Provider:**
```bash
docker run -d --name duda-user-provider \
  --network duda-network \
  -p 8082:8082 \
  -p 20882:20882 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e JAVA_OPTS="-Xms512m -Xmx512m" \
  duda/user-provider:1.0.0
```

**启动 API:**
```bash
docker run -d --name duda-user-api \
  --network duda-network \
  -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e JAVA_OPTS="-Xms512m -Xmx512m" \
  duda/user-api:1.0.0
```

### 3.6 验证部署

```bash
# 检查容器状态
docker ps

# 检查健康状态
docker inspect duda-user-provider | grep -A5 Health

# 查看日志
docker logs -f duda-user-provider

# 测试 API
curl "http://localhost:8083/user/page?pageSize=10"

# 访问 Swagger
open http://localhost:8083/swagger-ui/index.html
```

---

## 4. 多服务部署方案

### 4.1 项目结构假设

```
DudaNexus/
├── duda-usercenter/
│   ├── duda-user-api
│   └── duda-user-provider
├──uda-ordercenter/
│   ├── duda-order-api
│   └── duda-order-provider
└── docker-compose.yml
```

### 4.2 方案一: 使用 Docker Compose (推荐) ⭐

**创建 `docker-compose.yml`:**

```yaml
version: '3.8'

services:
  # 用户 Provider
  duda-user-provider:
    image: duda/user-provider:1.0.0
    build:
      context: ./duda-usercenter/duda-user-provider
      dockerfile: Dockerfile
    container_name: duda-user-provider
    ports:
      - "8082:8082"
      - "20882:20882"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - JAVA_OPTS=-Xms512m -Xmx512m
    networks:
      - duda-network
    restart: unless-stopped

  # 用户 API
  duda-user-api:
    image: duda/user-api:1.0.0
    build:
      context: ./duda-usercenter/duda-user-api
      dockerfile: Dockerfile
    container_name: duda-user-api
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - JAVA_OPTS=-Xms512m -Xmx512m
    networks:
      - duda-network
    depends_on:
      - duda-user-provider
    restart: unless-stopped

  # 订单 Provider (未来添加)
  # duda-order-provider:
  #   image: duda/order-provider:1.0.0
  #   build:
  #     context: ./duda-ordercenter/duda-order-provider
  #   container_name: duda-order-provider
  #   ports:
  #     - "8084:8084"
  #     - "20883:20883"
  #   environment:
  #     - SPRING_PROFILES_ACTIVE=dev
  #   networks:
  #     - duda-network

  # 订单 API (未来添加)
  # duda-order-api:
  #   image: duda/order-api:1.0.0
  #   build:
  #     context: ./duda-ordercenter/duda-order-api
  #   container_name: duda-order-api
  #   ports:
  #     - "8085:8085"
  #   environment:
  #     - SPRING_PROFILES_ACTIVE=dev
  #   networks:
  #     - duda-network

networks:
  duda-network:
    driver: bridge
```

**部署命令:**
```bash
# 启动所有服务
docker-compose up -d

# 启动特定服务
docker-compose up -d duda-user-provider

# 查看日志
docker-compose logs -f duda-user-provider

# 停止所有服务
docker-compose down

# 重启服务
docker-compose restart duda-user-api
```

### 4.3 方案二: 手动管理多个容器

**启动脚本 `start-all.sh`:**

```bash
#!/bin/bash

# 创建网络
docker network create duda-network 2>/dev/null || echo "Network exists"

# 启动 User Provider
docker run -d --name duda-user-provider \
  --network duda-network \
  -p 8082:8082 -p 20882:20882 \
  -e SPRING_PROFILES_ACTIVE=dev \
  duda/user-provider:1.0.0

# 启动 User API
docker run -d --name duda-user-api \
  --network duda-network \
  -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=dev \
  duda/user-api:1.0.0

# 启动 Order Provider (未来)
# docker run -d --name duda-order-provider \
#   --network duda-network \
#   -p 8084:8084 -p 20883:20883 \
#   -e SPRING_PROFILES_ACTIVE=dev \
#   duda/order-provider:1.0.0

echo "All services started!"
docker ps
```

### 4.4 添加新服务的步骤

**步骤 1: 创建新模块目录**
```bash
mkdir -p duda-ordercenter/duda-order-provider
mkdir -p duda-ordercenter/duda-order-api
```

**步骤 2: 配置 Dubbo 端口**

在每个服务的 `bootstrap.yml` 中配置唯一端口:

```yaml
# User Provider
dubbo:
  protocol:
    port: 20882  # 唯一端口

# Order Provider
dubbo:
  protocol:
    port: 20883  # 不同的端口
```

**步骤 3: 创建 Dockerfile**

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8084 20883  # HTTP 端口 + Dubbo 端口
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

**步骤 4: 构建和部署**

```bash
# 打包
mvn clean package -DskipTests -pl duda-ordercenter/duda-order-provider -am

# 构建镜像
cd duda-ordercenter/duda-order-provider
docker build -t duda/order-provider:1.0.0 .

# 启动容器
docker run -d --name duda-order-provider \
  --network duda-network \
  -p 8084:8084 -p 20883:20883 \
  -e SPRING_PROFILES_ACTIVE=dev \
  duda/order-provider:1.0.0
```

---

## 5. 常见故障排查

### 5.1 容器无法启动

**检查步骤:**
```bash
# 查看容器状态
docker ps -a

# 查看失败原因
docker logs <container_name>

# 查看详细配置
docker inspect <container_name>
```

**常见原因:**
- 端口冲突: `lsof -i:8082`
- 镜像不存在: `docker images | grep duda`
- 环境变量错误: 检查 `-e` 参数

### 5.2 服务注册失败

**现象:** Nacos 控制台看不到服务

**排查:**
```bash
# 检查 Nacos 连接
docker logs <container_name> | grep -i nacos

# 检查网络连通性
docker exec <container_name> ping 120.26.170.213

# 检查命名空间配置
# 确保配置文件中的 namespace 与 Nacos 控制台一致
```

### 5.3 Dubbo 调用超时

**现象:** API 调用 Provider 超时

**排查清单:**

1. ✅ **检查 Dubbo 端口是否一致**
   ```bash
   # 检查 Provider 配置的端口
   docker logs <provider> | grep "port="

   # 检查 Dockerfile EXPOSE 的端口
   cat Dockerfile | grep EXPOSE
   ```

2. ✅ **检查端口映射**
   ```bash
   docker port <provider>
   # 应该看到: 20882/tcp -> 0.0.0.0:20882
   ```

3. ✅ **检查容器网络**
   ```bash
   docker inspect <api> | grep -A10 "Networks"
   # 确保两个容器在同一个网络
   ```

4. ✅ **检查容器间连通性**
   ```bash
   docker exec <api> wget -qO- http://<provider>:8082/actuator/health
   ```

### 5.4 数据库连接失败

**现象:** `Communications link failure`

**解决方案:**

**方案 1: 数据库在宿主机**
```yaml
url: jdbc:mysql://host.docker.internal:3306/duda_nexus
```

**方案 2: 数据库在 Docker 容器**
```bash
# 启动数据库容器
docker run -d --name mysql \
  --network duda-network \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=duda_nexus \
  mysql:8.0

# 修改应用配置
url: jdbc:mysql://mysql:3306/duda_nexus  # 使用容器名作为主机名
```

**方案 3: 数据库在远程服务器**
```yaml
url: jdbc:mysql://120.26.170.213:3306/duda_nexus
```

---

## 6. 生产环境建议

### 6.1 安全性

**不要在镜像中硬编码密码:**
```yaml
# ❌ 错误
environment:
  - DB_PASSWORD=root123

# ✅ 正确
environment:
  - DB_PASSWORD=${DB_PASSWORD}
```

**使用 Docker Secrets 或环境变量文件:**
```bash
docker run -d --env-file .env duda/user-provider:1.0.0
```

### 6.2 资源限制

```bash
docker run -d \
  --name duda-user-provider \
  --memory="1g" \
  --cpus="1.5" \
  --network duda-network \
  duda/user-provider:1.0.0
```

### 6.3 日志管理

```bash
# 限制日志大小
docker run -d \
  --log-opt max-size=10m \
  --log-opt max-file=3 \
  duda/user-provider:1.0.0

# 挂载日志目录
docker run -d \
  -v /var/log/duda:/var/log/duda \
  duda/user-provider:1.0.0
```

### 6.4 健康检查

**Dockerfile 中已包含:**
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider \
  http://localhost:8082/actuator/health || exit 1
```

**自定义健康检查:**
```bash
docker run -d \
  --health-cmd="curl -f http://localhost/actuator/health || exit 1" \
  --health-interval=5s \
  --health-timeout=3s \
  --health-retries=3 \
  duda/user-provider:1.0.0
```

### 6.5 重启策略

```bash
# 总是重启
docker run -d --restart always duda/user-provider:1.0.0

# 失败时重启 (默认)
docker run -d --restart=on-failure[:max-retries] duda/user-provider:1.0.0

# 除非手动停止
docker run -d --restart=unless-stopped duda/user-provider:1.0.0
```

---

## 7. 快速命令参考

### 7.1 日常操作

```bash
# 查看运行中的容器
docker ps

# 查看所有容器
docker ps -a

# 查看日志
docker logs -f <container_name>

# 进入容器
docker exec -it <container_name> sh

# 停止容器
docker stop <container_name>

# 启动容器
docker start <container_name>

# 重启容器
docker restart <container_name>

# 删除容器
docker rm <container_name>

# 删除镜像
docker rmi <image_name>
```

### 7.2 清理命令

```bash
# 删除所有停止的容器
docker container prune

# 删除未使用的镜像
docker image prune -a

# 删除未使用的网络
docker network prune

# 删除未使用的卷
docker volume prune

# 一键清理所有
docker system prune -a --volumes
```

---

## 8. 总结

### 8.1 今天修复的核心问题

1. ✅ **Dubbo 端口配置** - 从自动分配改为固定端口 20882
2. ✅ **数据库连接** - 使用 `host.docker.internal` 访问宿主机 MySQL
3. ✅ **Docker 网络** - 使用自定义 bridge 网络实现容器间通信
4. ✅ **Dockerfile 修复** - 移除 Alpine 特定命令

### 8.2 部署架构图

```
┌─────────────────────────────────────────────────────────┐
│                     Mac 宿主机                           │
│                                                          │
│  ┌──────────────┐      ┌──────────────┐                │
│  │   MySQL      │      │   Nacos      │                │
│  │   (Docker)   │      │  (Cloud)     │                │
│  │   :3306      │      │ 120.26.170.. │                │
│  └──────┬───────┘      └──────┬───────┘                │
│         │                     │                          │
│         │ host.docker.internal│                          │
│         ▼                     │                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │           Docker Bridge 网络: duda-network       │   │
│  │  172.21.0.0/16                                  │   │
│  │                                                   │   │
│  │  ┌─────────────────────┐  ┌──────────────────┐   │   │
│  │  │ duda-user-provider  │  │  duda-user-api   │   │   │
│  │  │ 172.21.0.2          │  │  172.21.0.3      │   │   │
│  │  │ HTTP: 8082          │  │  HTTP: 8083      │   │   │
│  │  │ Dubbo: 20882        │  │  Consumer       │   │   │
│  │  └─────────┬───────────┘  └────────┬─────────┘   │   │
│  └────────────┼──────────────────────┼──────────────┘   │
│               │ Dubbo RPC (20882)    │                  │
└───────────────┼──────────────────────┼──────────────────┘
                │                      │
        ┌───────┴──────────┐    ┌─────┴────────┐
        │ Port Mapping     │    │ Port Mapping │
        │ 8082:8082        │    │ 8083:8083    │
        │ 20882:20882      │    │              │
        └──────────────────┘    └──────────────┘
                │                      │
        ┌───────┴──────────┐    ┌─────┴────────┐
        │  宿主机端口       │    │  宿主机端口   │
        │  localhost:8082  │    │  localhost:8083│
        └──────────────────┘    └──────────────┘
```

### 8.3 关键配置文件

**Provider 配置:** `duda-user-provider/src/main/resources/bootstrap.yml`
```yaml
server:
  port: 8082

dubbo:
  protocol:
    port: 20882  # ⭐ 必须与 Dockerfile EXPOSE 一致

spring:
  datasource:
    url: jdbc:mysql://host.docker.internal:3306/duda_nexus  # ⭐ 访问宿主机
```

**API 配置:** `duda-user-api/src/main/resources/bootstrap.yml`
```yaml
server:
  port: 8083

dubbo:
  protocol:
    port: -1  # Consumer 可以自动分配

spring:
  datasource:
    url: jdbc:mysql://host.docker.internal:3306/duda_nexus
```

---

**文档版本:** 1.0
**更新时间:** 2026-03-11
**作者:** Claude + 用户协作
**适用于:** DudaNexus 微服务项目


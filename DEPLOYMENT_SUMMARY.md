# Docker 部署问题总结

## 问题 1: 172.21.0.3 是哪里来的?

**答:** 这是 Docker 自定义网络自动分配的 IP 地址。

### 详细解释

当你执行 `docker network create duda-network` 时,Docker 做了以下事情:

1. **创建一个虚拟网络交换机**
2. **分配一个私有子网** - 随机选择,例如 `172.21.0.0/16`
3. **为连接到该网络的容器分配 IP**

### IP 分配示例

```
duda-network (172.21.0.0/16)
├── 网关: 172.21.0.1
├── duda-user-provider: 172.21.0.2
└── duda-user-api: 172.21.0.3
```

### 为什么需要这个?

- **容器间通信:** API 容器需要调用 Provider 的 Dubbo 服务
- **DNS 解析:** 容器可以通过名称互相访问:
  ```bash
  # 在 API 容器内,可以用容器名访问 Provider
  curl http://duda-user-provider:8082/actuator/health
  ```

### 查看网络详情

```bash
# 查看网络列表
docker network ls

# 查看网络详细信息
docker network inspect duda-network

# 查看容器的 IP 地址
docker inspect duda-user-api | grep IPAddress
```

---

## 问题 2: 现在是怎么操作的?

### 完整操作流程

#### 步骤 1: 修改配置文件

**修改 Provider 配置:**
```yaml
# 文件: duda-user-provider/src/main/resources/bootstrap.yml
dubbo:
  protocol:
    port: 20882  # 固定端口,替代 -1 (自动分配)

spring:
  datasource:
    url: jdbc:mysql://host.docker.internal:3306/duda_nexus  # 替代 localhost
```

**修改 API 配置:**
```yaml
# 文件: duda-user-api/src/main/resources/bootstrap.yml
spring:
  datasource:
    url: jdbc:mysql://host.docker.internal:3306/duda_nexus
```

#### 步骤 2: 打包应用

```bash
cd /Volumes/DudaDate/DudaNexus
mvn clean package -DskipTests -pl \
  duda-usercenter/duda-user-provider,\
  duda-usercenter/duda-user-api -am
```

#### 步骤 3: 构建 Docker 镜像

```bash
# Provider
cd duda-usercenter/duda-user-provider
docker build -t duda/user-provider:1.0.0 .

# API
cd ../duda-user-api
docker build -t duda/user-api:1.0.0 .
```

#### 步骤 4: 创建 Docker 网络

```bash
docker network create duda-network
```

#### 步骤 5: 启动容器

```bash
# Provider
docker run -d --name duda-user-provider \
  --network duda-network \
  -p 8082:8082 -p 20882:20882 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e JAVA_OPTS="-Xms512m -Xmx512m" \
  duda/user-provider:1.0.0

# API
docker run -d --name duda-user-api \
  --network duda-network \
  -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e JAVA_OPTS="-Xms512m -Xmx512m" \
  duda/user-api:1.0.0
```

#### 步骤 6: 验证

```bash
# 检查容器状态
docker ps

# 测试 API
curl "http://localhost:8083/user/page?pageSize=10"

# 访问 Swagger
open http://localhost:8083/swagger-ui/index.html
```

---

## 问题 3: 刚才导致一直出错的原因是什么?

### 根本原因分析

有 **3 个关键错误** 一起导致部署失败:

### 错误 1: Dubbo 端口不匹配 (主要问题) 🔥

**问题链:**
```
应用配置: dubbo.protocol.port = -1 (自动分配)
↓
Provider 启动时自动选择端口 20880
↓
Dockerfile 只暴露端口 20882
↓
API 容器从 Nacos 获取到 Provider 的地址: 172.17.0.4:20880
↓
API 尝试连接 20880,但该端口未暴露,连接超时 ❌
```

**修复方法:**
```yaml
# bootstrap.yml
dubbo:
  protocol:
    port: 20882  # 固定端口,与 Dockerfile EXPOSE 一致
```

### 错误 2: 数据库连接字符串错误

**问题:**
```yaml
# 容器内的 localhost 指向容器自己,不是宿主机的 MySQL
url: jdbc:mysql://localhost:3306/duda_nexus  # ❌ 错误
```

**修复:**
```yaml
url: jdbc:mysql://host.docker.internal:3306/duda_nexus  # ✅ 正确
```

### 错误 3: Dockerfile 使用 Alpine 命令

**问题:**
```dockerfile
# eclipse-temurin:17-jre 不是 Alpine 镜像
RUN apk add --no-cache tzdata  # ❌ apk 命令不存在
```

**修复:**
```dockerfile
RUN mkdir -p /usr/share/zoneinfo/Asia && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime  # ✅ 标准 Linux 命令
```

---

## 问题 4: 如何部署更多的 API 和服务?

### 方案一: 使用 Docker Compose (推荐) ⭐

#### 目录结构
```
DudaNexus/
├── duda-usercenter/
│   ├── duda-user-api
│   └── duda-user-provider
├── duda-ordercenter/        # 新增: 订单中心
│   ├── duda-order-api
│   └── duda-order-provider
├── duda-productcenter/      # 新增: 商品中心
│   ├── duda-product-api
│   └── duda-product-provider
└── docker-compose.yml
```

#### docker-compose.yml 示例

```yaml
version: '3.8'

services:
  # ==================== 用户中心 ====================
  duda-user-provider:
    image: duda/user-provider:1.0.0
    build: ./duda-usercenter/duda-user-provider
    container_name: duda-user-provider
    ports:
      - "8082:8082"
      - "20882:20882"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    networks:
      - duda-network

  duda-user-api:
    image: duda/user-api:1.0.0
    build: ./duda-usercenter/duda-user-api
    container_name: duda-user-api
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    networks:
      - duda-network

  # ==================== 订单中心 (新增) ====================
  duda-order-provider:
    image: duda/order-provider:1.0.0
    build: ./duda-ordercenter/duda-order-provider
    container_name: duda-order-provider
    ports:
      - "8084:8084"      # HTTP 端口
      - "20883:20883"    # Dubbo 端口 (必须唯一)
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    networks:
      - duda-network

  duda-order-api:
    image: duda/order-api:1.0.0
    build: ./duda-ordercenter/duda-order-api
    container_name: duda-order-api
    ports:
      - "8085:8085"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    networks:
      - duda-network

  # ==================== 商品中心 (新增) ====================
  duda-product-provider:
    image: duda/product-provider:1.0.0
    build: ./duda-productcenter/duda-product-provider
    container_name: duda-product-provider
    ports:
      - "8086:8086"
      - "20884:20884"    # 不同的 Dubbo 端口
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    networks:
      - duda-network

  duda-product-api:
    image: duda/product-api:1.0.0
    build: ./duda-productcenter/duda-product-api
    container_name: duda-product-api
    ports:
      - "8087:8087"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    networks:
      - duda-network

networks:
  duda-network:
    driver: bridge
```

#### 部署命令

```bash
# 启动所有服务
docker-compose up -d

# 只启动用户中心
docker-compose up -d duda-user-provider duda-user-api

# 查看所有服务状态
docker-compose ps

# 查看特定服务日志
docker-compose logs -f duda-user-provider

# 重启某个服务
docker-compose restart duda-user-api

# 停止所有服务
docker-compose down
```

### 方案二: 使用启动脚本

#### 创建 `start-all.sh`

```bash
#!/bin/bash

set -e  # 遇到错误立即退出

echo "=== DudaNexus 微服务启动脚本 ==="

# 创建网络
echo "1. 创建 Docker 网络..."
docker network create duda-network 2>/dev/null || echo "  网络已存在"

# ==================== 用户中心 ====================
echo "2. 启动用户中心..."
docker run -d --name duda-user-provider \
  --network duda-network \
  -p 8082:8082 -p 20882:20882 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e JAVA_OPTS="-Xms512m -Xmx512m" \
  --restart unless-stopped \
  duda/user-provider:1.0.0

docker run -d --name duda-user-api \
  --network duda-network \
  -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e JAVA_OPTS="-Xms512m -Xmx512m" \
  --restart unless-stopped \
  duda/user-api:1.0.0

# ==================== 订单中心 ====================
echo "3. 启动订单中心..."
docker run -d --name duda-order-provider \
  --network duda-network \
  -p 8084:8084 -p 20883:20883 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e JAVA_OPTS="-Xms512m -Xmx512m" \
  --restart unless-stopped \
  duda/order-provider:1.0.0

docker run -d --name duda-order-api \
  --network duda-network \
  -p 8085:8085 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e JAVA_OPTS="-Xms512m -Xmx512m" \
  --restart unless-stopped \
  duda/order-api:1.0.0

# ==================== 检查状态 ====================
echo "4. 检查服务状态..."
sleep 5
docker ps | grep duda

echo "=== 所有服务已启动 ==="
```

#### 创建 `stop-all.sh`

```bash
#!/bin/bash

echo "=== 停止所有 DudaNexus 服务 ==="

# 停止并删除容器
docker stop duda-user-provider duda-user-api \
  duda-order-provider duda-order-api 2>/dev/null || true

docker rm duda-user-provider duda-user-api \
  duda-order-provider duda-order-api 2>/dev/null || true

echo "=== 所有服务已停止 ==="
```

#### 使用方法

```bash
chmod +x start-all.sh stop-all.sh

# 启动所有服务
./start-all.sh

# 停止所有服务
./stop-all.sh
```

### 方案三: 使用 Makefile (推荐用于开发环境)

#### 创建 `Makefile`

```makefile
.PHONY: help build start stop restart logs clean

# 默认目标
help:
	@echo "DudaNexus Docker 管理命令:"
	@echo "  make build    - 构建所有镜像"
	@echo "  make start    - 启动所有服务"
	@echo "  make stop     - 停止所有服务"
	@echo "  make restart  - 重启所有服务"
	@echo "  make logs     - 查看所有日志"
	@echo "  make clean    - 清理所有容器和镜像"
	@echo "  make user     - 只启动用户中心"
	@echo "  make order    - 只启动订单中心"

# 构建所有镜像
build:
	@echo "构建用户中心..."
	cd duda-usercenter/duda-user-provider && mvn clean package -DskipTests
	cd duda-usercenter/duda-user-provider && docker build -t duda/user-provider:1.0.0 .
	cd duda-usercenter/duda-user-api && mvn clean package -DskipTests
	cd duda-usercenter/duda-user-api && docker build -t duda/user-api:1.0.0 .
	@echo "构建订单中心..."
	cd duda-ordercenter/duda-order-provider && mvn clean package -DskipTests
	cd duda-ordercenter/duda-order-provider && docker build -t duda/order-provider:1.0.0 .
	cd duda-ordercenter/duda-order-api && mvn clean package -DskipTests
	cd duda-ordercenter/duda-order-api && docker build -t duda/order-api:1.0.0 .

# 创建网络
network:
	docker network create duda-network 2>/dev/null || true

# 启动用户中心
user: network
	docker run -d --name duda-user-provider \
	  --network duda-network \
	  -p 8082:8082 -p 20882:20882 \
	  -e SPRING_PROFILES_ACTIVE=dev \
	  --restart unless-stopped \
	  duda/user-provider:1.0.0
	docker run -d --name duda-user-api \
	  --network duda-network \
	  -p 8083:8083 \
	  -e SPRING_PROFILES_ACTIVE=dev \
	  --restart unless-stopped \
	  duda/user-api:1.0.0

# 启动订单中心
order: network
	docker run -d --name duda-order-provider \
	  --network duda-network \
	  -p 8084:8084 -p 20883:20883 \
	  -e SPRING_PROFILES_ACTIVE=dev \
	  --restart unless-stopped \
	  duda/order-provider:1.0.0
	docker run -d --name duda-order-api \
	  --network duda-network \
	  -p 8085:8085 \
	  -e SPRING_PROFILES_ACTIVE=dev \
	  --restart unless-stopped \
	  duda/order-api:1.0.0

# 启动所有服务
start: user order
	@echo "所有服务已启动"
	docker ps | grep duda

# 停止所有服务
stop:
	docker stop duda-user-provider duda-user-api duda-order-provider duda-order-api || true
	docker rm duda-user-provider duda-user-api duda-order-provider duda-order-api || true

# 重启所有服务
restart: stop start

# 查看日志
logs:
	docker logs -f duda-user-provider

# 清理
clean: stop
	docker network rm duda-network || true
```

#### 使用方法

```bash
# 查看帮助
make help

# 构建所有镜像
make build

# 启动所有服务
make start

# 只启动用户中心
make user

# 查看日志
make logs

# 停止所有服务
make stop

# 清理所有资源
make clean
```

---

## 添加新服务的检查清单

当添加新的微服务时,确保:

- [ ] **唯一的 HTTP 端口:** 例如 8086, 8088, 8090...
- [ ] **唯一的 Dubbo 端口:** 例如 20884, 20885, 20886...
- [ ] **配置固定 Dubbo 端口:** `dubbo.protocol.port=20884`
- [ ] **数据库使用 host.docker.internal**
- [ ] **连接到相同的 Docker 网络:** `--network duda-network`
- [ ] **Nacos 命名空间一致:** `namespace: duda-dev`
- [ ] **端口正确映射:** `-p 8086:8086 -p 20884:20884`

---

## 端口分配建议

### 用户中心
- Provider HTTP: 8082
- Provider Dubbo: 20882
- API HTTP: 8083

### 订单中心
- Provider HTTP: 8084
- Provider Dubbo: 20883
- API HTTP: 8085

### 商品中心
- Provider HTTP: 8086
- Provider Dubbo: 20884
- API HTTP: 8087

### 支付中心
- Provider HTTP: 8088
- Provider Dubbo: 20885
- API HTTP: 8089

---

**总结:**
1. ✅ 172.21.0.3 是 Docker 网络自动分配的 IP
2. ✅ 修改 Dubbo 端口和数据库连接字符串后重新构建
3. ✅ 三个关键错误:端口不匹配、数据库连接、Alpine 命令
4. ✅ 推荐使用 Docker Compose 管理多服务部署


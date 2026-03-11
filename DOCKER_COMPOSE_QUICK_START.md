# Docker Compose 快速启动指南

## ✅ 已验证可行

当前配置已成功部署并测试通过!

## 🚀 快速启动

### 启动所有服务
```bash
cd /Volumes/DudaDate/DudaNexus
docker-compose up -d
```

### 查看服务状态
```bash
docker-compose ps
```

**输出示例:**
```
NAME                 IMAGE                      STATUS                    PORTS
duda-user-api        duda/user-api:1.0.0        Up 46 seconds (healthy)   0.0.0.0:8083->8083/tcp
duda-user-provider   duda/user-provider:1.0.0   Up 52 seconds (healthy)   0.0.0.0:8082:8082/tcp, 0.0.0.0:20882:20882/tcp
```

### 查看日志
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f duda-user-provider
docker-compose logs -f duda-user-api

# 查看最近 50 行日志
docker-compose logs --tail=50 duda-user-provider
```

### 停止服务
```bash
# 停止所有服务 (保留容器)
docker-compose stop

# 停止并删除容器
docker-compose down

# 停止并删除容器、网络、卷
docker-compose down -v
```

### 重启服务
```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart duda-user-api
```

## 🧪 测试验证

### 1. 健康检查
```bash
# Provider
curl http://localhost:8082/actuator/health

# API
curl http://localhost:8083/actuator/health
```

### 2. 测试 API
```bash
# 获取用户列表
curl "http://localhost:8083/user/page?pageSize=10" | jq .

# 根据用户名查询
curl "http://localhost:8083/user/username/testuser003" | jq .
```

### 3. 访问 Swagger UI
```bash
open http://localhost:8083/swagger-ui/index.html
```

## 📝 常用命令

### 构建镜像
```bash
# 重新构建所有镜像
docker-compose build

# 重新构建特定服务
docker-compose build duda-user-provider

# 重新构建并启动
docker-compose up -d --build
```

### 查看资源使用
```bash
# 实时监控
docker stats

# 查看特定容器
docker stats duda-user-provider
```

### 进入容器
```bash
# 进入 Provider 容器
docker exec -it duda-user-provider sh

# 进入 API 容器
docker exec -it duda-user-api sh

# 在容器内执行命令
docker exec duda-user-provider ps aux
docker exec duda-user-api ls -la /app
```

### 查看网络配置
```bash
# 查看网络列表
docker network ls | grep duda

# 查看网络详情
docker network inspect duda-network
```

## 🔧 故障排查

### 问题 1: 端口被占用
```bash
# 检查端口占用
lsof -i:8082
lsof -i:8083
lsof -i:20882

# 停止占用端口的容器
docker stop <container_name>
```

### 问题 2: 服务启动失败
```bash
# 查看详细日志
docker-compose logs --tail=100 duda-user-provider

# 检查容器退出代码
docker-compose ps -a
docker inspect <container_id> | grep -A10 ExitCode
```

### 问题 3: 网络问题
```bash
# 删除并重新创建网络
docker-compose down
docker network rm duda-network
docker-compose up -d
```

### 问题 4: 服务间无法通信
```bash
# 测试容器间连通性
docker exec duda-user-api ping duda-user-provider
docker exec duda-user-api wget -qO- http://duda-user-provider:8082/actuator/health
```

## 📦 添加新服务

### 步骤 1: 创建新模块
```bash
mkdir -p duda-ordercenter/duda-order-provider
mkdir -p duda-ordercenter/duda-order-api
```

### 步骤 2: 配置 Dubbo 端口

**Provider 配置 (`bootstrap.yml`):**
```yaml
server:
  port: 8084  # HTTP 端口

dubbo:
  protocol:
    port: 20883  # Dubbo 端口 (必须唯一)

spring:
  datasource:
    url: jdbc:mysql://host.docker.internal:3306/duda_nexus
```

### 步骤 3: 更新 docker-compose.yml

```yaml
services:
  # ... 现有服务 ...

  # 新增: 订单 Provider
  duda-order-provider:
    image: duda/order-provider:1.0.0
    build:
      context: ./duda-ordercenter/duda-order-provider
      dockerfile: Dockerfile
    container_name: duda-order-provider
    ports:
      - "8084:8084"
      - "20883:20883"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - JAVA_OPTS=-Xms512m -Xmx512m
    networks:
      - duda-network
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8084/actuator/health"]
      interval: 30s
      timeout: 3s
      retries: 3
      start_period: 60s
    restart: unless-stopped

  # 新增: 订单 API
  duda-order-api:
    image: duda/order-api:1.0.0
    build:
      context: ./duda-ordercenter/duda-order-api
      dockerfile: Dockerfile
    container_name: duda-order-api
    ports:
      - "8085:8085"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - JAVA_OPTS=-Xms512m -Xmx512m
    networks:
      - duda-network
    depends_on:
      duda-order-provider:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8085/actuator/health"]
      interval: 30s
      timeout: 3s
      retries: 3
      start_period: 60s
    restart: unless-stopped

networks:
  duda-network:
    driver: bridge
    name: duda-network
```

### 步骤 4: 构建和启动
```bash
# 构建新服务
docker-compose build duda-order-provider duda-order-api

# 启动新服务 (不停止现有服务)
docker-compose up -d duda-order-provider duda-order-api

# 查看所有服务
docker-compose ps
```

## 🎯 端口分配建议

| 服务 | HTTP 端口 | Dubbo 端口 |
|------|----------|------------|
| 用户 Provider | 8082 | 20882 |
| 用户 API | 8083 | - |
| 订单 Provider | 8084 | 20883 |
| 订单 API | 8085 | - |
| 商品 Provider | 8086 | 20884 |
| 商品 API | 8087 | - |
| 支付 Provider | 8088 | 20885 |
| 支付 API | 8089 | - |

## 📊 监控和维护

### 查看容器资源使用
```bash
docker stats --no-stream
```

### 查看磁盘使用
```bash
# 查看镜像大小
docker images

# 查看容器大小
docker ps -s

# 查看 Docker 总体使用情况
docker system df
```

### 清理未使用的资源
```bash
# 清理停止的容器
docker container prune

# 清理未使用的镜像
docker image prune -a

# 清理未使用的网络
docker network prune

# 一键清理所有
docker system prune -a --volumes
```

## 🔐 生产环境建议

### 1. 使用环境变量文件

创建 `.env` 文件:
```env
# 数据库配置
DB_HOST=host.docker.internal
DB_PORT=3306
DB_NAME=duda_nexus
DB_USER=root
DB_PASSWORD=your_secure_password

# Nacos 配置
NACOS_HOST=120.26.170.213
NACOS_PORT=8848
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos

# JVM 配置
JAVA_OPTS=-Xms512m -Xmx512m -XX:+UseG1GC
```

修改 `docker-compose.yml`:
```yaml
services:
  duda-user-provider:
    environment:
      - SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev}
      - DB_HOST=${DB_HOST}
      - DB_PASSWORD=${DB_PASSWORD}
      - JAVA_OPTS=${JAVA_OPTS}
```

### 2. 资源限制

```yaml
services:
  duda-user-provider:
    deploy:
      resources:
        limits:
          cpus: '1.5'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
```

### 3. 日志配置

```yaml
services:
  duda-user-provider:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### 4. 健康检查优化

```yaml
services:
  duda-user-provider:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8082/actuator/health"]
      interval: 30s
      timeout: 3s
      retries: 3
      start_period: 60s
```

## ✅ 当前部署状态

**已部署服务:**
- ✅ duda-user-provider (健康)
- ✅ duda-user-api (健康)

**访问地址:**
- Swagger UI: http://localhost:8083/swagger-ui/index.html
- 用户 API: http://localhost:8083/user/page
- Provider 监控: http://localhost:8082/actuator/health

**验证测试:**
```bash
# 执行此命令验证部署
curl "http://localhost:8083/user/page?pageSize=10" | jq .
```

**预期输出:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 0
  },
  "success": true
}
```

---

**最后更新:** 2026-03-11
**状态:** ✅ 已验证可用
**版本:** 1.0

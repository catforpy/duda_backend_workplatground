# Docker镜像和容器命名规范

## 📦 镜像标签规范

### 格式
```
{仓库}/{服务名}:{版本}
```

### 用户中台服务

| 服务 | 镜像标签 | 说明 |
|------|----------|------|
| Provider | `duda/user-provider:1.0.0` | 用户RPC服务 |
| API | `duda/user-api:1.0.0` | 用户REST API服务 |

### 示例

```bash
# 构建镜像
docker build -t duda/user-provider:1.0.0 ./duda-usercenter/duda-user-provider
docker build -t duda/user-api:1.0.0 ./duda-usercenter/duda-user-api

# 查看镜像
docker images | grep duda
```

---

## 🐳 容器名称规范

### 格式
```
duda-{服务类型}-{服务名}
```

### 用户中台服务

| 服务 | 容器名称 | 说明 |
|------|----------|------|
| Provider | `duda-user-provider` | 用户RPC服务容器 |
| API | `duda-user-api` | 用户API服务容器 |

### 示例

```bash
# 查看运行中的容器
docker ps

# 查看所有容器
docker ps -a

# 进入容器
docker exec -it duda-user-provider sh
docker exec -it duda-user-api sh
```

---

## 🔧 常用Docker命令

### 镜像操作

```bash
# 构建镜像
docker build -t duda/user-provider:1.0.0 .

# 查看镜像
docker images

# 删除镜像
docker rmi duda/user-provider:1.0.0

# 推送到镜像仓库
docker push duda/user-provider:1.0.0
```

### 容器操作

```bash
# 启动容器
docker-compose up -d

# 停止容器
docker-compose stop

# 重启容器
docker-compose restart

# 删除容器
docker-compose down

# 查看容器日志
docker-compose logs -f duda-user-provider
docker-compose logs -f duda-user-api

# 查看容器状态
docker-compose ps

# 进入容器
docker exec -it duda-user-provider sh
docker exec -it duda-user-api sh
```

---

## 📋 版本号规范

### 格式
```
主版本.次版本.修订号
```

### 示例

- `1.0.0` - 初始版本
- `1.0.1` - Bug修复
- `1.1.0` - 新功能
- `2.0.0` - 重大更新

### 快速命令

```bash
# 打包并构建（使用脚本）
./build.sh

# 或手动执行
mvn clean package -DskipTests
docker build -t duda/user-provider:1.0.0 ./duda-usercenter/duda-user-provider
docker build -t duda/user-api:1.0.0 ./duda-usercenter/duda-user-api
```

---

## 🏷️ 标签建议

### 开发环境
```
duda/user-provider:dev
duda/user-api:dev
```

### 测试环境
```
duda/user-provider:test
duda/user-api:test
```

### 生产环境
```
duda/user-provider:1.0.0
duda/user-api:1.0.0
duda/user-provider:latest
duda/user-api:latest
```

---

## ⚠️ 常见错误

### 错误1: JAR文件名错误

**问题:** 打包时使用了错误的命令
```bash
# ❌ 错误
mvn clean package -DskipTests duda-user-provider
```

**解决:** 使用正确的打包命令
```bash
# ✅ 正确
mvn clean package -DskipTests
```

### 错误2: 镜像标签错误

**问题:** 使用了不规范的标签
```bash
# ❌ 错误
docker build -t user-provider .
```

**解决:** 使用规范的标签
```bash
# ✅ 正确
docker build -t duda/user-provider:1.0.0 .
```

### 错误3: 容器名称冲突

**问题:** 容器名称已存在
```bash
# ❌ 错误
Error: Conflict. The container name "duda-user-provider" is already in use.
```

**解决:** 删除旧容器或使用不同名称
```bash
# ✅ 删除旧容器
docker rm -f duda-user-provider

# ✅ 或使用不同名称
docker-compose down
```

---

## 📊 完整工作流

```bash
# 1. 清理旧的容器和镜像
docker-compose down
docker rmi duda/user-provider:1.0.0 duda/user-api:1.0.0 || true

# 2. 打包项目
mvn clean package -DskipTests

# 3. 构建镜像
docker build -t duda/user-provider:1.0.0 ./duda-usercenter/duda-user-provider
docker build -t duda/user-api:1.0.0 ./duda-usercenter/duda-user-api

# 4. 启动服务
docker-compose up -d

# 5. 查看状态
docker-compose ps

# 6. 查看日志
docker-compose logs -f
```

---

**使用规范命名，便于管理和维护！** ✅

# IDEA Docker 部署配置指南

## 问题说明

使用 IDEA 的 Docker 插件部署时出现错误：
```
ERROR [internal] load metadata for docker.io/library/eclipse-temurin:17-jre-alpine
ERROR [internal] load metadata for docker.io/library/maven:3.9-eclipse-temurin-17-alpine
```

**原因：** IDEA Docker 插件尝试使用 `-alpine` 后缀的镜像，但本地只有不带后缀的镜像。

---

## 本地已有镜像

```bash
docker images | grep -E "eclipse-temurin|maven"
```

```
maven                    3.9-eclipse-temurin-17   3665c3293ca8   35 hours ago    793MB
eclipse-temurin          17-jre                   7a7b6ad2ab91   3 weeks ago     398MB
```

**已拉取的镜像：**
- ✅ `eclipse-temurin:17-jre`（非 alpine 版本）
- ✅ `maven:3.9-eclipse-temurin-17`（非 alpine 版本）

---

## 解决方案

### 方案 1：修改 IDEA Docker 运行配置 ⭐ 推荐

#### 步骤 1：打开 Docker 运行配置

1. 在 IDEA 右上角找到运行配置下拉菜单
2. 选择 **"Edit Configurations..."**
3. 在左侧列表找到 **"user-provider-base Dockerfile"**

#### 步骤 2：检查并修改配置

**重要设置项：**

| 配置项 | 推荐值 | 说明 |
|--------|--------|------|
| **Context folder** | `./duda-usercenter/duda-user-provider` | Dockerfile 所在目录 |
| **Dockerfile** | `Dockerfile` | Dockerfile 文件名 |
| **Image tag** | `duda/user-provider:1.0.0` | 构建后的镜像标签 |
| **Container name** | `duda-user-provider` | 容器名称 |
| **Bind ports** | `8082:8082` | 端口映射 |

**关键设置：**
- ❌ **取消勾选** "Pull latest image before build"
- ❌ **取消勾选** "Auto publish"
- ✅ 设置 "Pull image" 为 **"Never"**

#### 步骤 3：Environment Variables（环境变量）

点击 **"Environment"** 字段，添加：

```bash
SPRING_PROFILES_ACTIVE=dev
JAVA_OPTS=-Xms512m -Xmx512m
```

#### 步骤 4：Bind Ports（端口绑定）

添加端口映射：
- `Host Port: 8082` → `Container Port: 8082`
- `Host Port: 20882` → `Container Port: 20882`

#### 步骤 5：保存并运行

点击 **"Apply"** 和 **"OK"** 保存配置。

然后点击绿色运行按钮 ▶️ 开始构建。

---

### 方案 2：使用命令行构建（最可靠）

如果 IDEA 配置仍然有问题，可以直接使用命令行：

```bash
# 进入项目目录
cd /Volumes/DudaDate/DudaNexus

# 打包应用
cd duda-usercenter/duda-user-provider
mvn clean package -DskipTests

# 构建 Docker 镜像
docker build -t duda/user-provider:1.0.0 .

# 运行容器
docker run -d \
  --name duda-user-provider \
  -p 8082:8082 \
  -p 20882:20882 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e JAVA_OPTS="-Xms512m -Xmx512m" \
  duda/user-provider:1.0.0
```

---

### 方案 3：使用 Docker Compose（最简单）

```bash
# 进入项目目录
cd /Volumes/DudaDate/DudaNexus

# 启动所有服务（包括 MySQL、Redis、Nacos）
docker-compose up -d

# 只启动用户服务
docker-compose up -d duda-user-provider

# 查看日志
docker-compose logs -f duda-user-provider

# 停止服务
docker-compose down
```

---

## Dockerfile 修改说明

已修改 `duda-user-provider/Dockerfile`，添加 ARG 参数：

```dockerfile
# 使用 ARG 明确指定基础镜像
ARG BASE_IMAGE=eclipse-temurin:17-jre
FROM ${BASE_IMAGE}
```

**作用：**
- 明确指定基础镜像名称
- 防止 IDEA 自动添加 `-alpine` 后缀
- 可以通过 `--build-arg` 覆盖（如果需要）

---

## 验证镜像

确认本地已有正确的镜像：

```bash
# 查看所有 eclipse-temurin 镜像
docker images | grep eclipse-temurin

# 应该看到：
# eclipse-temurin   17-jre    7a7b6ad2ab91   3 weeks ago   398MB
```

---

## IDEA Docker 插件常见问题

### 1. 自动添加 -alpine 后缀

**原因：** IDEA 可能在某些情况下"优化"镜像名称。

**解决：**
- 在 Dockerfile 中使用 ARG 参数
- 或在 IDEA 配置中明确指定镜像

### 2. 无法连接到 Docker

**检查：**
1. Docker Desktop 是否正在运行
2. 在 IDEA 设置中确认 Docker 连接：`Settings → Build, Execution, Deployment → Docker`

### 3. 构建上下文过大

**原因：** 包含了不必要的文件。

**解决：** 使用 `.dockerignore` 文件排除不需要的文件

---

## 完整的部署流程

### 使用 IDEA

1. ✅ 确保 Docker Desktop 运行中
2. ✅ 确认本地已有所需镜像
3. ✅ Maven 打包项目
4. ✅ 配置 Docker 运行配置（见方案 1）
5. ✅ 运行 Docker 构建

### 使用命令行

```bash
# 1. 打包
cd /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-provider
mvn clean package -DskipTests

# 2. 构建
docker build -t duda/user-provider:1.0.0 .

# 3. 运行
docker run -d --name duda-user-provider -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=dev \
  duda/user-provider:1.0.0

# 4. 查看日志
docker logs -f duda-user-provider
```

---

## 常用 Docker 命令

```bash
# 查看运行中的容器
docker ps

# 查看所有容器（包括停止的）
docker ps -a

# 查看容器日志
docker logs -f <container_name>

# 停止容器
docker stop <container_name>

# 删除容器
docker rm <container_name>

# 删除镜像
docker rmi <image_name>

# 进入容器内部
docker exec -it <container_name> sh
```

---

## 测试部署

部署成功后，测试服务：

```bash
# 测试健康检查
curl http://localhost:8082/actuator/health

# 测试 API
curl http://localhost:8082/user/page

# 查看容器资源使用
docker stats duda-user-provider
```

---

## 故障排查

### 容器启动失败

```bash
# 查看详细日志
docker logs <container_id>

# 检查容器状态
docker inspect <container_id>

# 重新构建并运行
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### 端口冲突

```bash
# 查看端口占用
lsof -i:8082

# 停止占用端口的容器
docker stop <container_name>
```

---

## 相关文档

- [Dockerfile 参考](./Dockerfile)
- [docker-compose.yml](../../docker-compose.yml)
- [部署指南](./deployment/01-部署环境规范.md)
- [数据库配置](../../database-schema.md)

---

**更新时间：** 2026-03-11
**版本：** 1.0

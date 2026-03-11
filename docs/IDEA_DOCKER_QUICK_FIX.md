# IDEA Docker 部署快速修复

## 立即解决

### 方法 1：在 IDEA 中修改配置（最快）

1. **打开运行配置**
   - 点击 IDEA 右上角运行配置下拉菜单
   - 选择 "Edit Configurations..."

2. **找到 Docker 配置**
   - 左侧列表找到 "user-provider-base Dockerfile"

3. **关键修改** ⭐
   - 找到并**取消勾选** "Pull latest image before build"
   - 或者找到 "Pull" 选项，选择 "Never"

4. **保存并运行**
   - 点击 "Apply" → "OK"
   - 点击绿色运行按钮 ▶️

### 方法 2：直接用命令行（100% 可用）

```bash
cd /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-provider
mvn clean package -DskipTests
docker build -t duda/user-provider:1.0.0 .
docker run -d --name duda-user-provider -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=dev \
  duda/user-provider:1.0.0
```

### 方法 3：使用 Docker Compose

```bash
cd /Volumes/DudaDate/DudaNexus
docker-compose up -d duda-user-provider
```

---

## 为什么会出现这个问题？

- 本地镜像：`eclipse-temurin:17-jre` ✅
- IDEA 尝试：`eclipse-temurin:17-jre-alpine` ❌

IDEA 可能自动添加了 `-alpine` 后缀。

## 修复后验证

```bash
# 检查容器是否运行
docker ps | grep duda-user-provider

# 查看日志
docker logs -f duda-user-provider

# 测试 API
curl http://localhost:8082/actuator/health
```

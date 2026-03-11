# Docker部署测试指南

## 🐳 一键部署测试环境

### 前置条件

1. 安装Docker Desktop
2. 确保端口没有被占用：3306, 6379, 8848, 8081, 8082, 20882

---

## 🚀 快速启动

### Step 1: 打包项目

```bash
cd /Volumes/DudaDate/DudaNexus

# 设置Java 17
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home

# 打包
mvn clean package -DskipTests
```

### Step 2: 启动Docker环境

```bash
# 启动所有服务（MySQL、Redis、Nacos、Provider、API）
docker-compose up -d

# 查看日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f duda-user-provider
docker-compose logs -f duda-user-api
```

### Step 3: 等待服务启动

```bash
# 查看服务状态
docker-compose ps

# 等待所有服务变为healthy状态（约1-2分钟）
```

### Step 4: 初始化Nacos配置

#### 4.1 登录Nacos

浏览器访问: http://localhost:8848/nacos
- 用户名: nacos
- 密码: nacos

#### 4.2 创建命名空间

- 命名空间ID: `duda-dev`
- 命名空间名称: `都达开发环境`

#### 4.3 切换到duda-dev命名空间

#### 4.4 创建配置文件

##### 配置1: common-dev.yml
- Data ID: `common-dev.yml`
- Group: `COMMON_GROUP`

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://mysql:3306/duda_nexus?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: Duda@2025
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20

  data:
    redis:
      host: redis
      port: 6379
      password: Duda@2025
      database: 0
      timeout: 5000ms
      jedis:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5

logging:
  level:
    root: INFO
    com.duda: DEBUG
```

##### 配置2: duda-user-provider-dev.yml
- Data ID: `duda-user-provider-dev.yml`
- Group: `USER_GROUP`

```yaml
server:
  port: 8082

dubbo:
  application:
    name: duda-user-provider
  protocol:
    name: dubbo
    port: 20882
  registry:
    address: nacos://nacos:8848
    parameters:
      namespace: duda-dev
      group: DEFAULT_GROUP
  scan:
    base-packages: com.duda.user.rpc
  provider:
    timeout: 5000
```

##### 配置3: duda-user-api-dev.yml
- Data ID: `duda-user-api-dev.yml`
- Group: `USER_GROUP`

```yaml
server:
  port: 8081

dubbo:
  application:
    name: duda-user-api
  consumer:
    check: false
    timeout: 5000
```

### Step 5: 重启服务

```bash
# 重启Provider和API（让它们从Nacos加载配置）
docker-compose restart duda-user-provider duda-user-api

# 查看启动日志
docker-compose logs -f duda-user-provider
docker-compose logs -f duda-user-api
```

### Step 6: 验证服务

#### 6.1 检查容器状态

```bash
docker-compose ps
```

所有服务应该显示 `healthy`

#### 6.2 检查Nacos服务注册

访问: http://localhost:8848/nacos
- 切换到duda-dev命名空间
- 服务管理 → 服务列表
- 应该看到: `duda-user-provider` 和 `duda-user-api`

#### 6.3 访问Swagger

```
http://localhost:8081/swagger-ui.html
```

---

## 🧪 测试用例

### 测试1: 用户登录

**接口:** `POST /user/login`

**请求体:**
```json
{
  "username": "admin",
  "password": "Duda@2025"
}
```

**预期返回:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "userType": "platform_admin",
    "status": "active"
  }
}
```

### 测试2: 查询用户

**接口:** `GET /user/1`

**预期返回:** 用户信息

### 测试3: 用户注册

**接口:** `POST /user/register`

**请求体:**
```json
{
  "username": "testuser",
  "password": "Test@123",
  "realName": "测试用户",
  "phone": "13900000001",
  "email": "test@example.com",
  "userType": "platform_account"
}
```

---

## 🛠️ 常用命令

### 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f duda-user-provider
docker-compose logs -f duda-user-api
docker-compose logs -f mysql
docker-compose logs -f redis
docker-compose logs -f nacos
```

### 进入容器

```bash
# 进入Provider容器
docker exec -it duda-user-provider sh

# 进入MySQL容器
docker exec -it duda-mysql mysql -uroot -pDuda@2025

# 进入Redis容器
docker exec -it duda-redis redis-cli -a Duda@2025
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart duda-user-provider
docker-compose restart duda-user-api
```

### 停止和删除

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（清空数据）
docker-compose down -v
```

### 重新构建

```bash
# 重新构建并启动
docker-compose up -d --build

# 重新构建特定服务
docker-compose up -d --build duda-user-provider
```

---

## 🔍 故障排查

### 1. 服务启动失败

**检查日志:**
```bash
docker-compose logs duda-user-provider
```

**常见问题:**
- 数据库未初始化 → 等待MySQL完全启动
- Nacos连接失败 → 检查Nacos是否healthy
- 端口冲突 → 检查端口占用

### 2. 服务注册失败

**检查:**
1. Nacos配置是否正确
2. 命名空间是否一致
3. 网络是否连通

### 3. API调用失败

**检查:**
1. Provider是否启动并注册到Nacos
2. Dubbo端口是否正确（20882）
3. 查看API日志

---

## 📊 服务地址

| 服务 | 地址 | 用户名/密码 |
|------|------|-------------|
| Nacos | http://localhost:8848/nacos | nacos/nacos |
| MySQL | localhost:3306 | root/Duda@2025 |
| Redis | localhost:6379 | - |
| 用户API | http://localhost:8081 | - |
| Swagger | http://localhost:8081/swagger-ui.html | - |
| Provider | http://localhost:8082 | - |

---

## ✅ 测试检查清单

- [ ] Docker环境就绪
- [ ] 项目打包成功
- [ ] Docker容器启动成功
- [ ] MySQL数据库初始化成功
- [ ] Nacos访问正常
- [ ] Nacos配置创建成功
- [ ] Provider启动并注册到Nacos
- [ ] API启动并注册到Nacos
- [ ] Swagger访问成功
- [ ] 用户登录测试通过
- [ ] 查询用户测试通过
- [ ] 用户注册测试通过

---

**准备好一键测试了！** 🚀

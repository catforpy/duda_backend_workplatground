# 用户中台测试指南

## 📋 测试流程

### 一、环境准备

#### 1.1 数据库初始化

```bash
# 连接MySQL
mysql -h 120.26.170.213 -uroot -pDuda@2025

# 执行建表脚本
source /Volumes/DudaDate/DudaNexus/sql/init-schema.sql;

# 执行初始化数据
source /Volumes/DudaDate/DudaNexus/sql/init-data.sql;

# 验证
USE duda_nexus;
SELECT COUNT(*) FROM users;  -- 应该返回1（admin账号）
```

#### 1.2 Nacos配置创建

**登录Nacos:** http://120.26.170.213:8848/nacos (nacos/nacos)

**1. 创建命名空间:**
- 命名空间ID: `duda-dev`
- 命名空间名称: `都达开发环境`

**2. 切换到duda-dev命名空间**

**3. 创建配置文件:**

##### 3.1 公共配置 (common-dev.yml)
- Data ID: `common-dev.yml`
- Group: `COMMON_GROUP`
- 配置格式: `YAML`

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://120.26.170.213:3306/duda_nexus?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: Duda@2025
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      validation-query: SELECT 1
      test-while-idle: true

  data:
    redis:
      host: 120.26.170.213
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

##### 3.2 Provider配置 (duda-user-provider-dev.yml)
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
    address: nacos://120.26.170.213:8848
    parameters:
      namespace: duda-dev
      group: DEFAULT_GROUP
  scan:
    base-packages: com.duda.user.rpc
  provider:
    timeout: 5000
```

##### 3.3 API配置 (duda-user-api-dev.yml)
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

---

### 二、启动服务

#### 2.1 启动Provider

```bash
cd /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-provider
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
mvn spring-boot:run
```

**验证:**
- 查看日志: `Tomcat started on port(s): 8082`
- Nacos控制台应该看到 `duda-user-provider` 注册成功

#### 2.2 启动API

**新开一个终端:**

```bash
cd /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-api
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
mvn spring-boot:run
```

**验证:**
- 查看日志: `Tomcat started on port(s): 8081`
- Nacos控制台应该看到 `duda-user-api` 注册成功

---

### 三、Swagger测试

#### 3.1 访问Swagger UI

```
http://localhost:8081/swagger-ui.html
```

#### 3.2 测试用例

##### 测试1: 用户登录 ✅

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

---

##### 测试2: 查询用户 ✅

**接口:** `GET /user/{userId}`

**路径参数:**
- `userId`: `1`

**预期返回:** 返回id为1的用户信息

---

##### 测试3: 用户注册 ✅

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

**预期返回:** 返回新创建的用户ID

---

##### 测试4: 分页查询用户 ✅

**接口:** `GET /user/page`

**查询参数:**
- `pageNum`: `1`
- `pageSize`: `10`

**预期返回:** 用户分页列表

---

### 四、验证检查清单

- [ ] 数据库创建成功（7张表）
- [ ] 初始化数据成功（admin账号）
- [ ] Nacos命名空间创建成功
- [ ] Nacos配置文件创建成功（3个）
- [ ] Provider启动成功（端口8082）
- [ ] Provider注册到Nacos成功
- [ ] API启动成功（端口8081）
- [ ] API注册到Nacos成功
- [ ] Swagger访问成功
- [ ] 用户登录测试通过
- [ ] 查询用户测试通过
- [ ] 用户注册测试通过

---

### 五、常见问题

#### 5.1 Provider启动失败

**检查:**
1. 数据库是否创建
2. Nacos配置是否正确
3. MySQL、Redis连接是否正常

#### 5.2 API启动失败

**检查:**
1. Provider是否已启动
2. Dubbo引用配置是否正确

#### 5.3 Swagger访问404

**检查:**
1. API服务是否启动
2. 端口是否正确（8081）

---

## 📊 测试结果记录

| 测试项 | 结果 | 备注 |
|--------|------|------|
| 数据库初始化 | ⏳ | |
| Nacos配置 | ⏳ | |
| Provider启动 | ⏳ | |
| API启动 | ⏳ | |
| 用户登录 | ⏳ | |
| 查询用户 | ⏳ | |
| 用户注册 | ⏳ | |

---

**准备就绪，可以开始测试！**

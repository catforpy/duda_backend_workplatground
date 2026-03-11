# DudaNexus 快速启动指南

## 📋 概述

本文档指导您快速搭建和测试 DudaNexus 用户中台服务。

---

## 🚀 快速启动流程

### 一、环境准备

#### 1. 基础环境检查

确保以下环境已安装并运行：

```bash
# Java 17
java -version

# Maven 3.8+
mvn -version

# MySQL 8.0+
mysql --version

# Redis 6.0+
redis-cli --version

# Nacos 2.x
curl http://120.26.170.213:8848/nacos/
```

#### 2. 服务运行状态检查

```bash
# MySQL
mysql -h 120.26.170.213 -uroot -pDuda@2025 -e "SELECT 1"

# Redis
redis-cli -h 120.26.170.213 -a Duda@2025 PING

# Nacos
curl http://120.26.170.213:8848/nacos/v1/console/health/liveness
```

---

### 二、数据库初始化

#### Step 1: 创建数据库和表

```bash
# 方式1: 使用MySQL命令行
mysql -h 120.26.170.213 -uroot -pDuda@2025 < /Volumes/DudaDate/DudaNexus/sql/init-schema.sql

# 方式2: 登录MySQL后执行
mysql -h 120.26.170.213 -uroot -pDuda@2025
```

然后在MySQL客户端中：

```sql
SOURCE /Volumes/DudaDate/DudaNexus/sql/init-schema.sql;
```

#### Step 2: 插入初始化数据

```bash
mysql -h 120.26.170.213 -uroot -pDuda@2025 < /Volumes/DudaDate/DudaNexus/sql/init-data.sql
```

或在MySQL客户端中：

```sql
SOURCE /Volumes/DudaDate/DudaNexus/sql/init-data.sql;
```

#### Step 3: 验证数据库

```sql
USE duda_nexus;

-- 查看所有表
SHOW TABLES;

-- 验证管理员账号
SELECT id, username, real_name, user_type, status FROM users WHERE username = 'admin';

-- 验证权限数据
SELECT COUNT(*) AS permission_count FROM permissions;
```

预期结果：
- 7张表创建成功
- 1个管理员账号（username: admin, password: Duda@2025）
- 5个角色
- 54个权限节点

---

### 三、Nacos配置创建

#### Step 1: 登录Nacos控制台

1. 浏览器访问: http://120.26.170.213:8848/nacos
2. 用户名: `nacos`
3. 密码: `nacos`

#### Step 2: 创建命名空间

1. 点击左侧菜单 **命名空间**
2. 点击 **新建命名空间**
3. 填写信息:
   - **命名空间ID**: `duda-dev`
   - **命名空间名称**: `都达开发环境`
   - **描述**: `DudaNexus开发环境`
4. 点击 **确定**

#### Step 3: 创建公共配置

1. 切换到 **duda-dev** 命名空间（右上角下拉框）
2. 点击 **配置管理** → **配置列表**
3. 点击 **+** 创建配置
4. 填写信息:
   - **Data ID**: `common-dev.yml`
   - **Group**: `COMMON_GROUP`
   - **配置格式**: `YAML`
   - **配置内容**: 见下方配置

**common-dev.yml 配置内容：**

```yaml
# 公共配置 - 所有服务共享
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
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      filters: stat,wall
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: admin123
      web-stat-filter:
        enabled: true
        url-pattern: /*
        exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"

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
          max-wait: 3000ms

logging:
  level:
    root: INFO
    com.duda: DEBUG
    com.baomidou.mybatisplus: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: '*'
  endpoint:
    health:
      show-details: always
```

5. 点击 **发布**

#### Step 4: 创建用户服务配置

1. 在 **duda-dev** 命名空间下
2. 点击 **+** 创建配置
3. 填写信息:
   - **Data ID**: `duda-user-provider-dev.yml`
   - **Group**: `USER_GROUP`
   - **配置格式**: `YAML`
   - **配置内容**: 见下方配置

**duda-user-provider-dev.yml 配置内容：**

```yaml
server:
  port: 8082

spring:
  application:
    name: duda-user-provider
  profiles:
    active: dev
  cloud:
    nacos:
      server-addr: 120.26.170.213:8848
      username: nacos
      password: nacos
      discovery:
        namespace: duda-dev
        group: USER_GROUP
        enabled: true
        register-enabled: true
      config:
        namespace: duda-dev
        group: USER_GROUP
        file-extension: yml
        shared-configs:
          - data-id: common-dev.yml
            group: COMMON_GROUP
            refresh: true

dubbo:
  application:
    name: duda-user-provider
    qos-enable: false
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
    retries: 2
    loadbalance: roundrobin

duda:
  user:
    default-password: Duda@2025
    token-expire-hours: 72
    captcha-enabled: false
    password-salt: duda_nexus_2025
```

4. 点击 **发布**

#### Step 5: 验证配置

在配置列表中应该看到：

```
duda-dev 命名空间:
├── COMMON_GROUP
│   └── common-dev.yml
└── USER_GROUP
    └── duda-user-provider-dev.yml
```

---

### 四、编译和启动服务

#### Step 1: 编译项目

```bash
cd /Volumes/DudaDate/DudaNexus

# 清理并编译
mvn clean install -DskipTests

# 或者只编译用户模块
cd duda-usercenter
mvn clean install -DskipTests
```

预期输出：

```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
duda-common-core ................................... SUCCESS
duda-common-web .................................... SUCCESS
duda-common-database ............................... SUCCESS
duda-common-redis .................................. SUCCESS
duda-user-interface ................................ SUCCESS
duda-user-provider ................................. SUCCESS
```

#### Step 2: 启动用户服务

```bash
cd /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-provider

# 方式1: 使用Maven启动
mvn spring-boot:run

# 方式2: 使用Java启动（先打包）
mvn clean package -DskipTests
java -jar target/duda-user-provider-1.0.0-SNAPSHOT.jar
```

#### Step 3: 验证启动成功

查看启动日志，应该看到：

```
✅ Tomcat started on port(s): 8082
✅ Dubbo service server started!
✅ Service registration successful!
✅ DudaNexus User Provider started successfully!
```

---

### 五、服务验证

#### 1. 验证Dubbo服务注册

**方式1: Nacos控制台查看**

1. 登录Nacos控制台
2. 切换到 **duda-dev** 命名空间
3. 点击 **服务管理** → **服务列表**
4. 应该看到: `duda-user-provider`

**方式2: 命令行查看**

```bash
curl "http://120.26.170.213:8848/nacos/v1/ns/instance/list?serviceName=duda-user-provider&namespaceId=duda-dev"
```

#### 2. 测试REST API

```bash
# 测试用户登录
curl -X POST http://localhost:8082/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Duda@2025"
  }'

# 预期返回
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    ...
  }
}
```

#### 3. 测试RPC调用（需要创建消费者）

参考 `rpc-test-guide.md` 创建测试消费者。

---

### 六、Druid监控

访问Druid监控页面：

```
URL: http://localhost:8082/druid/
用户名: admin
密码: admin123
```

可以查看：
- SQL监控
- URI监控
- Session监控
- 数据源状态

---

### 七、常见问题排查

#### 1. 数据库连接失败

**错误信息**: `Could not create connection to database server`

**解决方案**:
```bash
# 检查MySQL是否运行
mysql -h 120.26.170.213 -uroot -pDuda@2025 -e "SELECT 1"

# 检查数据库是否存在
mysql -h 120.26.170.213 -uroot -pDuda@2025 -e "SHOW DATABASES LIKE 'duda_nexus'"

# 检查Nacos配置
# 登录Nacos控制台，确认 common-dev.yml 配置正确
```

#### 2. Nacos配置加载失败

**错误信息**: `Config data not found`

**解决方案**:
```bash
# 检查Nacos是否运行
curl http://120.26.170.213:8848/nacos/v1/console/health/liveness

# 检查命名空间是否存在
# 登录Nacos控制台，确认 duda-dev 命名空间已创建

# 检查配置是否存在
# 登录Nacos控制台，确认配置已正确创建
```

#### 3. Dubbo服务注册失败

**错误信息**: `Failed to register service to registry`

**解决方案**:
```bash
# 检查bootstrap.yml配置
cat /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-provider/src/main/resources/bootstrap.yml

# 确认dubbo.registry.address配置正确
# 确认dubbo.scan.base-packages配置正确
```

#### 4. Redis连接失败

**错误信息**: `Unable to connect to Redis`

**解决方案**:
```bash
# 检查Redis是否运行
redis-cli -h 120.26.170.213 -a Duda@2025 PING

# 应该返回: PONG
```

---

## ✅ 验证清单

完成以下检查，确保系统正常运行：

- [ ] MySQL数据库 `duda_nexus` 创建成功
- [ ] 所有表结构创建成功（7张表）
- [ ] 初始化数据插入成功（管理员账号、角色、权限）
- [ ] Nacos命名空间 `duda-dev` 创建成功
- [ ] Nacos配置 `common-dev.yml` 创建成功
- [ ] Nacos配置 `duda-user-provider-dev.yml` 创建成功
- [ ] 项目编译成功（BUILD SUCCESS）
- [ ] 服务启动成功（端口8082）
- [ ] Dubbo服务注册到Nacos成功
- [ ] 可以通过REST API登录（admin/Duda@2025）
- [ ] Druid监控页面访问正常

---

## 📊 系统架构图

```
┌─────────────────────────────────────────────────────────┐
│                      Nacos (120.26.170.213:8848)         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ 服务发现注册  │  │   配置中心    │  │   命名空间    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            ↑
                            │ 注册
                            │
┌─────────────────────────────────────────────────────────┐
│           duda-user-provider (localhost:8082)            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Controller  │  │  RPC Service │  │  UserService │  │
│  │   REST API   │  │  @DubboService│ │   Business   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
         ↓                              ↓
┌──────────────┐              ┌──────────────┐
│ MySQL:3306   │              │ Redis:6379   │
│ duda_nexus   │              │              │
└──────────────┘              └──────────────┘
```

---

## 🎯 下一步

1. **创建RPC测试消费者**: 参考 `rpc-test-guide.md`
2. **实现JWT认证**: 添加Token生成和验证
3. **实现服务商管理**: 创建公司审核流程
4. **实现小程序管理**: 创建小程序授权功能

---

**状态**: ⏳ 待执行
**最后更新**: 2026-03-10

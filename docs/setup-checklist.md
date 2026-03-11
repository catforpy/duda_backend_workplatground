# DudaNexus 环境搭建执行清单

## 📋 执行说明

本文档是一份**可执行的检查清单**，按照顺序执行即可完成DudaNexus用户中台的完整搭建。

每完成一项，请在 `[]` 中填写 `x` 标记完成。

---

## 第一阶段：环境检查 ✅

### 1.1 检查Java环境

```bash
java -version
```

**预期输出:** `java version "17.x.x"`

完成标记: [ ]

---

### 1.2 检查Maven环境

```bash
mvn -version
```

**预期输出:** `Apache Maven 3.x.x`

完成标记: [ ]

---

### 1.3 检查MySQL连接

```bash
mysql -h 120.26.170.213 -uroot -pDuda@2025 -e "SELECT VERSION();"
```

**预期输出:** MySQL版本信息

完成标记: [ ]

---

### 1.4 检查Redis连接

```bash
redis-cli -h 120.26.170.213 -a Duda@2025 PING
```

**预期输出:** `PONG`

完成标记: [ ]

---

### 1.5 检查Nacos运行状态

```bash
curl http://120.26.170.213:8848/nacos/
```

**预期输出:** Nacos登录页面HTML

完成标记: [ ]

---

## 第二阶段：数据库初始化 🗄️

### 2.1 创建数据库和表

**方式1: 命令行执行**

```bash
cd /Volumes/DudaDate/DudaNexus
mysql -h 120.26.170.213 -uroot -pDuda@2025 < sql/init-schema.sql
```

**方式2: MySQL客户端**

```bash
mysql -h 120.26.170.213 -uroot -pDuda@2025
```

然后在MySQL中执行:
```sql
SOURCE /Volumes/DudaDate/DudaNexus/sql/init-schema.sql;
```

完成标记: [ ]

---

### 2.2 插入初始化数据

**方式1: 命令行执行**

```bash
mysql -h 120.26.170.213 -uroot -pDuda@2025 < sql/init-data.sql
```

**方式2: MySQL客户端**

在MySQL中执行:
```sql
SOURCE /Volumes/DudaDate/DudaNexus/sql/init-data.sql;
```

完成标记: [ ]

---

### 2.3 验证数据库初始化

在MySQL中执行以下验证SQL:

```sql
USE duda_nexus;

-- 1. 查看所有表
SHOW TABLES;
```

**预期结果:** 7张表（companies, mini_programs, permissions, role_permissions, roles, user_roles, users）

完成标记: [ ]

```sql
-- 2. 验证管理员账号
SELECT id, username, real_name, user_type, status
FROM users
WHERE username = 'admin';
```

**预期结果:**
- id: 1
- username: admin
- real_name: 系统管理员
- user_type: platform_admin
- status: active

完成标记: [ ]

```sql
-- 3. 统计各表数据量
SELECT 'users' AS table_name, COUNT(*) AS count FROM users
UNION ALL
SELECT 'roles', COUNT(*) FROM roles
UNION ALL
SELECT 'permissions', COUNT(*) FROM permissions;
```

**预期结果:**
- users: 1
- roles: 5
- permissions: 54

完成标记: [ ]

---

## 第三阶段：Nacos配置创建 ⚙️

### 3.1 登录Nacos控制台

1. 打开浏览器访问: http://120.26.170.213:8848/nacos
2. 输入用户名: `nacos`
3. 输入密码: `nacos`
4. 点击登录

完成标记: [ ]

---

### 3.2 创建命名空间

1. 点击左侧菜单 **命名空间**
2. 点击右上角 **新建命名空间**
3. 填写:
   - 命名空间ID: `duda-dev`
   - 命名空间名称: `都达开发环境`
   - 描述: `DudaNexus开发环境`
4. 点击 **确定**

**验证:** 在命名空间列表中能看到 `duda-dev`

完成标记: [ ]

---

### 3.3 切换到duda-dev命名空间

在控制台右上角，找到命名空间下拉框，选择 **duda-dev**

完成标记: [ ]

---

### 3.4 创建公共配置（common-dev.yml）

1. 点击左侧菜单 **配置管理** → **配置列表**
2. 点击右上角 **+** 创建配置
3. 填写配置信息:

   - **Data ID:** `common-dev.yml`
   - **Group:** `COMMON_GROUP`
   - **配置格式:** 选择 `YAML`
   - **配置内容:** 复制以下内容

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

4. 点击 **发布**

完成标记: [ ]

---

### 3.5 创建用户服务配置（duda-user-provider-dev.yml）

1. 在 **duda-dev** 命名空间下
2. 点击 **+** 创建配置
3. 填写配置信息:

   - **Data ID:** `duda-user-provider-dev.yml`
   - **Group:** `USER_GROUP`
   - **配置格式:** 选择 `YAML`
   - **配置内容:** 复制以下内容

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

完成标记: [ ]

---

### 3.6 验证Nacos配置

在配置列表中应该看到:

```
duda-dev 命名空间:
├── COMMON_GROUP
│   └── common-dev.yml
└── USER_GROUP
    └── duda-user-provider-dev.yml
```

完成标记: [ ]

---

## 第四阶段：项目编译和启动 🚀

### 4.1 编译项目

```bash
cd /Volumes/DudaDate/DudaNexus
mvn clean install -DskipTests
```

**预期输出:** `BUILD SUCCESS`

完成标记: [ ]

---

### 4.2 启动用户服务

**方式1: Maven启动（推荐用于开发）**

```bash
cd /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-provider
mvn spring-boot:run
```

**方式2: JAR包启动（推荐用于生产）**

```bash
cd /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-provider
mvn clean package -DskipTests
java -jar target/duda-user-provider-1.0.0-SNAPSHOT.jar
```

**预期输出:** 服务启动日志，包含:
- `Tomcat started on port(s): 8082`
- `Dubbo service server started`
- `DudaNexus User Provider started successfully`

完成标记: [ ]

---

## 第五阶段：服务验证 ✅

### 5.1 验证服务注册到Nacos

**方式1: Nacos控制台查看**

1. 登录Nacos控制台
2. 切换到 **duda-dev** 命名空间
3. 点击 **服务管理** → **服务列表**
4. 查找服务名: `duda-user-provider`

**预期结果:** 能看到 `duda-user-provider` 服务，实例数为1

完成标记: [ ]

---

### 5.2 测试REST API - 用户登录

```bash
curl -X POST http://localhost:8082/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Duda@2025"
  }'
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

完成标记: [ ]

---

### 5.3 测试REST API - 查询用户

```bash
curl -X GET http://localhost:8082/user/1
```

**预期返回:** 返回id为1的用户信息

完成标记: [ ]

---

### 5.4 访问Druid监控

1. 打开浏览器访问: http://localhost:8082/druid/
2. 输入用户名: `admin`
3. 输入密码: `admin123`

**预期结果:** 能看到Druid监控面板

完成标记: [ ]

---

### 5.5 验证RPC服务暴露

查看启动日志，应该包含:

```
Export dubbo service org.apache.dubbo.demo...
dubbo service server started!
```

完成标记: [ ]

---

## 第六阶段：功能测试 🧪

### 6.1 用户注册测试

```bash
curl -X POST http://localhost:8082/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@123",
    "realName": "测试用户",
    "phone": "13900000001",
    "email": "test@example.com",
    "userType": "platform_account"
  }'
```

**预期返回:** 注册成功的用户ID

完成标记: [ ]

---

### 6.2 分页查询测试

```bash
curl -X GET "http://localhost:8082/user/page?pageNum=1&pageSize=10"
```

**预期返回:** 用户分页列表

完成标记: [ ]

---

## 完成状态总览 🎯

### 环境检查
- [ ] Java 17
- [ ] Maven 3.x
- [ ] MySQL连接
- [ ] Redis连接
- [ ] Nacos运行

### 数据库初始化
- [ ] 建表脚本执行
- [ ] 初始化数据执行
- [ ] 数据验证通过

### Nacos配置
- [ ] 命名空间创建
- [ ] 公共配置创建
- [ ] 服务配置创建
- [ ] 配置验证通过

### 服务启动
- [ ] 项目编译成功
- [ ] 服务启动成功
- [ ] 无报错日志

### 服务验证
- [ ] Nacos服务注册
- [ ] 登录API测试通过
- [ ] Druid监控访问
- [ ] RPC服务暴露

### 功能测试
- [ ] 用户注册测试
- [ ] 分页查询测试

---

## 🎉 恭喜！

如果以上所有检查项都已完成，说明DudaNexus用户中台基础环境已搭建成功！

### 下一步建议:

1. ✅ 创建RPC消费者服务，测试Dubbo RPC调用
2. ✅ 实现JWT认证机制
3. ✅ 完善业务功能（服务商管理、小程序管理）
4. ✅ 编写单元测试和集成测试
5. ✅ 添加API文档（Swagger/Knife4j）

---

**最后更新:** 2026-03-10
**文档版本:** 1.0
**执行预计时间:** 30-45分钟

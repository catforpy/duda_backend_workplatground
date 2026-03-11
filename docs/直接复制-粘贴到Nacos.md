# 📋 直接复制粘贴到 Nacos 的完整配置

## 🎯 操作步骤

### 第一步：登录并创建命名空间

1. 访问：http://120.26.170.213:8848/nacos
2. 登录：nacos / nacos
3. 点击「命名空间」→「新建命名空间」
4. 填写：
   ```
   命名空间 ID: duda-dev
   命名空间名称: 开发环境
   ```
5. 点击「确定」

### 第二步：切换到 duda-dev 命名空间

在顶部下拉框选择「duda-dev」

### 第三步：创建配置（按顺序创建）

---

## 配置 1️⃣：common-dev.yml

**点击「创建配置」，填写：**

```
Data ID: common-dev.yml
Group: COMMON_GROUP
配置格式: YAML
```

**配置内容（复制下面全部）：**

```yaml
# 公共配置 - 开发环境
# 所有服务共享的基础配置

spring:
  # Redis 配置
  data:
    redis:
      host: 120.26.170.213
      port: 6379
      password: duda2024
      database: 0
      timeout: 10s
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 2s

  # RocketMQ 配置
  rocketmq:
    name-server: 120.26.170.213:9876
    producer:
      group: duda-producer-group
      send-message-timeout: 3000
      retry-times-when-send-failed: 2
      max-message-size: 4194304
    consumer:
      group: duda-consumer-group

# MyBatis-Plus 配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# Actuator 配置
management:
  endpoints:
    web:
      exposure:
        include: '*'
  endpoint:
    health:
      show-details: always

# 日志配置
logging:
  level:
    root: INFO
    com.duda: DEBUG
    com.alibaba.cloud.nacos: INFO
    org.springframework.cloud: INFO
  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%logger{50}] %msg%n'
```

点击「发布」

---

## 配置 2️⃣：duda-auth-provider-dev.yml

**点击「创建配置」，填写：**

```
Data ID: duda-auth-provider-dev.yml
Group: AUTH_GROUP
配置格式: YAML
```

**配置内容（复制下面全部）：**

```yaml
# 认证服务配置 - 开发环境

server:
  port: 8081

spring:
  application:
    name: duda-auth-provider

  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://120.26.170.213:3306/duda_auth?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: duda2024
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

# 认证服务特定配置
duda:
  auth:
    jwt:
      secret: duda-auth-secret-key-2026
      expiration: 7200
    token:
      header: Authorization
      prefix: Bearer
    captcha:
      enabled: true
```

点击「发布」

---

## 配置 3️⃣：duda-user-provider-dev.yml

**点击「创建配置」，填写：**

```
Data ID: duda-user-provider-dev.yml
Group: USER_GROUP
配置格式: YAML
```

**配置内容（复制下面全部）：**

```yaml
# 用户服务配置 - 开发环境

server:
  port: 8082

spring:
  application:
    name: duda-user-provider

  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://120.26.170.213:3306/duda_user?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: duda2024
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

# 用户服务特定配置
duda:
  user:
    default-avatar: https://duda.com/default-avatar.png
    nickname-prefix: 用户
    register:
      enabled: true
      email-required: false
      phone-required: true
```

点击「发布」

---

## 配置 4️⃣：duda-gateway-dev.yml

**点击「创建配置」，填写：**

```
Data ID: duda-gateway-dev.yml
Group: GATEWAY_GROUP
配置格式: YAML
```

**配置内容（复制下面全部）：**

```yaml
# 网关服务配置 - 开发环境

server:
  port: 8080

spring:
  application:
    name: duda-gateway

  cloud:
    gateway:
      # 服务发现配置
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true

      # 路由配置
      routes:
        # 认证服务路由
        - id: duda-auth-provider
          uri: lb://duda-auth-provider
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=2

        # 用户服务路由
        - id: duda-user-provider
          uri: lb://duda-user-provider
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=2

        # 搜索服务路由（预留）
        - id: duda-search-provider
          uri: lb://duda-search-provider
          predicates:
            - Path=/api/search/**
          filters:
            - StripPrefix=2

        # 内容服务路由（预留）
        - id: duda-content-provider
          uri: lb://duda-content-provider
          predicates:
            - Path=/api/content/**
          filters:
            - StripPrefix=2

        # 订单服务路由（预留）
        - id: duda-order-provider
          uri: lb://duda-order-provider
          predicates:
            - Path=/api/order/**
          filters:
            - StripPrefix=2

      # 全局 CORS 配置
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600

# 网关特定配置
duda:
  gateway:
    # 认白名单（不需要认证的路径）
    auth:
      skip-paths:
        - /api/auth/login
        - /api/auth/register
        - /api/auth/logout
        - /api/auth/captcha
        - /actuator/**
```

点击「发布」

---

## ✅ 完成检查

在 `duda-dev` 命名空间的配置列表中，应该看到：

| Data ID | Group |
|---------|-------|
| common-dev.yml | COMMON_GROUP |
| duda-auth-provider-dev.yml | AUTH_GROUP |
| duda-user-provider-dev.yml | USER_GROUP |
| duda-gateway-dev.yml | GATEWAY_GROUP |

---

## 🚀 下一步：创建数据库

```bash
mysql -h 120.26.170.213 -P 3306 -u root -pduda2024
```

执行以下 SQL：

```sql
CREATE DATABASE IF NOT EXISTS duda_auth
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS duda_user
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS duda_search
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS duda_content
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS duda_order
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 验证
SHOW DATABASES LIKE 'duda_%';
```

---

## 🎉 启动服务

在 IDEA 中启动 `duda-auth-provider` 服务即可！

---

**所有配置都完整了，直接复制粘贴即可！**

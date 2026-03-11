# Nacos 配置创建指南

## 🎯 Nacos 配置规划

### 登录 Nacos

```
地址: http://120.26.170.213:8848/nacos
账号: nacos
密码: nacos
```

---

## 📁 命名空间规划

### 1. 命名空间列表

| 命名空间 ID | 命名空间名称 | 描述 |
|------------|------------|------|
| `public` | 默认空间 | 生产环境（已存在） |
| `duda-dev` | 开发环境 | 开发测试环境 |
| `duda-test` | 测试环境 | 预发布测试环境 |

### 2. 创建命名空间

**步骤：**
1. 登录 Nacos 控制台
2. 点击左侧「命名空间」
3. 点击「新建命名空间」
4. 填写信息：
   - 命名空间 ID: `duda-dev`
   - 命名空间名称: `开发环境`
   - 描述: DudaNexus 开发测试环境

---

## 📝 配置文件清单

### 公共配置（所有服务共享）

#### 1. common-dev.yml

**位置：** `duda-dev` 命名空间

**用途：** 所有服务共享的开发环境配置

```yaml
# 公共配置 - 开发环境
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
    consumer:
      group: duda-consumer-group

# MyBatis-Plus 配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
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
    org.springframework.cloud.gateway: INFO
  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%logger{50}] %msg%n'
```

---

### 服务配置（每个服务独立）

#### 1. duda-auth-provider-dev.yml

**位置：** `duda-dev` 命名空间

```yaml
server:
  port: 8081

spring:
  application:
    name: duda-auth-provider

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

# 认证服务特有配置
duda:
  auth:
    jwt:
      secret: duda-auth-secret-key-2026
      expiration: 7200
    token:
      header: Authorization
      prefix: Bearer
```

#### 2. duda-user-provider-dev.yml

**位置：** `duda-dev` 命名空间

```yaml
server:
  port: 8082

spring:
  application:
    name: duda-user-provider

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

# 用户服务特有配置
duda:
  user:
    default-avatar: https://duda.com/default-avatar.png
    nickname-prefix: 用户
```

#### 3. duda-gateway-dev.yml

**位置：** `duda-dev` 命名空间

```yaml
server:
  port: 8080

spring:
  application:
    name: duda-gateway

  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
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

# 网关特有配置
duda:
  gateway:
    cors:
      allowed-origins: "*"
      allowed-methods: "*"
      allowed-headers: "*"
    auth:
      skip-paths:
        - /api/auth/login
        - /api/auth/register
        - /api/auth/logout
```

#### 4. duda-search-provider-dev.yml

**位置：** `duda-dev` 命名空间

```yaml
server:
  port: 8083

spring:
  application:
    name: duda-search-provider

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://120.26.170.213:3306/duda_search?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: duda2024
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20

  data:
    elasticsearch:
      client:
        rest:
          uris: http://120.26.170.213:9200
```

#### 5. duda-content-provider-dev.yml

**位置：** `duda-dev` 命名空间

```yaml
server:
  port: 8084

spring:
  application:
    name: duda-content-provider

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://120.26.170.213:3306/duda_content?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: duda2024
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20

  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

#### 6. duda-order-provider-dev.yml

**位置：** `duda-dev` 命名空间

```yaml
server:
  port: 8085

spring:
  application:
    name: duda-order-provider

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://120.26.170.213:3306/duda_order?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: duda2024
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20

# 订单服务特有配置
duda:
  order:
    order-timeout: 3600
    pay-timeout: 7200
```

---

## 📝 配置创建步骤

### 步骤 1: 创建命名空间

1. 登录 Nacos
2. 点击「命名空间」
3. 创建 `duda-dev` 命名空间

### 步骤 2: 创建公共配置

1. 切换到 `duda-dev` 命名空间
2. 点击「配置管理」→「配置列表」
3. 点击「+」创建配置
4. 填写信息：
   - Data ID: `common-dev.yml`
   - Group: `DEFAULT_GROUP`
   - 配置格式: `YAML`
   - 配置内容: 复制上面的 common-dev.yml 内容

### 步骤 3: 创建服务配置

为每个服务创建独立配置（重复以下步骤）：

1. 在 `duda-dev` 命名空间下
2. 点击「+」创建配置
3. 填写信息：
   - Data ID: `{服务名}-dev.yml`
   - Group: `DEFAULT_GROUP`
   - 配置格式: `YAML`
   - 配置内容: 对应服务的配置

**示例：**
```
Data ID: duda-auth-provider-dev.yml
Group: DEFAULT_GROUP
配置格式: YAML
配置内容: [上面提供的认证服务配置]
```

---

## 📊 配置清单总览

### 需要创建的配置文件

| 序号 | Data ID | Group | 用途 |
|-----|---------|-------|------|
| 1 | common-dev.yml | DEFAULT_GROUP | 公共配置（所有服务共享） |
| 2 | duda-auth-provider-dev.yml | DEFAULT_GROUP | 认证服务 |
| 3 | duda-user-provider-dev.yml | DEFAULT_GROUP | 用户服务 |
| 4 | duda-gateway-dev.yml | DEFAULT_GROUP | 网关服务 |
| 5 | duda-search-provider-dev.yml | DEFAULT_GROUP | 搜索服务 |
| 6 | duda-content-provider-dev.yml | DEFAULT_GROUP | 内容服务 |
| 7 | duda-order-provider-dev.yml | DEFAULT_GROUP | 订单服务 |
| 8 | duda-notification-provider-dev.yml | DEFAULT_GROUP | 通知服务 |
| 9 | duda-monitor-provider-dev.yml | DEFAULT_GROUP | 监控服务 |

---

## 🔧 配置验证

创建完成后，在服务的 `bootstrap.yml` 中引用：

```yaml
spring:
  application:
    name: duda-auth-provider  # 服务名
  cloud:
    nacos:
      username: nacos
      password: nacos
      config:
        server-addr: 120.26.170.213:8848
        namespace: duda-dev  # 命名空间
        group: DEFAULT_GROUP
        file-extension: yml
        shared-configs:
          # 引入公共配置
          - data-id: common-dev.yml
            group: DEFAULT_GROUP
            refresh: true
```

---

## ⚠️ 注意事项

1. **命名空间选择**
   - 开发环境使用 `duda-dev`
   - 生产环境使用 `public`

2. **Data ID 命名规则**
   - 格式: `{服务名}-{profile}.yml`
   - 示例: `duda-auth-provider-dev.yml`

3. **Group 使用**
   - 默认使用 `DEFAULT_GROUP`
   - 特殊业务可创建独立 Group

4. **配置优先级**
   - 服务配置 > 公共配置
   - 后加载的配置会覆盖先加载的配置

5. **敏感信息**
   - 生产环境建议使用 Nacos 的加密配置功能
   - 定期更换密码

---

**创建完成后，在 Nacos 控制台的「配置列表」中应该能看到以上所有配置文件。**

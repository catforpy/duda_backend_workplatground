# Nacos配置中心设置指南

## 📋 配置清单

### 1. 公共配置（common-dev.yml）

**Data ID**: `common-dev.yml`
**Group**: `COMMON_GROUP`
**命名空间**: `duda-dev`

```yaml
# 公共配置 - 所有服务共享

# 数据库配置
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

  # Redis配置
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

# 日志配置
logging:
  level:
    root: INFO
    com.duda: DEBUG
    com.baomidou.mybatisplus: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"

# 监控配置
management:
  endpoints:
    web:
      exposure:
        include: '*'
  endpoint:
    health:
      show-details: always
```

---

### 2. 用户服务配置（duda-user-provider-dev.yml）

**Data ID**: `duda-user-provider-dev.yml`
**Group**: `USER_GROUP`
**命名空间**: `duda-dev`

```yaml
# 用户服务专用配置

server:
  port: 8082

# 应用配置
spring:
  application:
    name: duda-user-provider
  profiles:
    active: dev

  # Cloud配置
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

# Dubbo配置（仅RPC相关，其他在bootstrap.yml）
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

# 业务配置
duda:
  user:
    # 用户默认密码
    default-password: Duda@2025
    # Token过期时间（小时）
    token-expire-hours: 72
    # 是否需要验证码
    captcha-enabled: false
    # 密码加密盐值
    password-salt: duda_nexus_2025
```

---

## 🚀 配置步骤

### Step 1: 登录Nacos控制台

1. 访问: `http://120.26.170.213:8848/nacos`
2. 用户名: `nacos`
3. 密码: `nacos`

### Step 2: 创建命名空间

1. 点击左侧菜单 **命名空间**
2. 点击 **新建命名空间**
3. 填写:
   - 命名空间ID: `duda-dev`
   - 命名空间名称: `都达开发环境`
   - 描述: `DudaNexus开发环境`
4. 点击 **确定**

### Step 3: 创建公共配置

1. 切换到 **duda-dev** 命名空间
2. 点击左侧菜单 **配置管理** → **配置列表**
3. 点击 **+** 创建配置
4. 填写:
   - Data ID: `common-dev.yml`
   - Group: `COMMON_GROUP`
   - 配置格式: `YAML`
   - 配置内容: 复制上面的 **common-dev.yml** 内容
5. 点击 **发布**

### Step 4: 创建用户服务配置

1. 在 **duda-dev** 命名空间下
2. 点击 **+** 创建配置
3. 填写:
   - Data ID: `duda-user-provider-dev.yml`
   - Group: `USER_GROUP`
   - 配置格式: `YAML`
   - 配置内容: 复制上面的 **duda-user-provider-dev.yml** 内容
4. 点击 **发布**

### Step 5: 验证配置

配置创建完成后，在配置列表中应该看到:

```
duda-dev 命名空间:
├── COMMON_GROUP
│   └── common-dev.yml
└── USER_GROUP
    └── duda-user-provider-dev.yml
```

---

## 🔍 配置说明

### 配置优先级

Spring Boot 配置加载顺序（优先级从高到低）:

1. **bootstrap.yml** (本地配置，用于服务发现)
2. **Nacos共享配置** (common-dev.yml)
3. **Nacos服务配置** (duda-user-provider-dev.yml)
4. **application.yml** (本地配置)

### 配置项说明

| 配置项 | 说明 | 示例值 |
|--------|------|--------|
| `spring.datasource.url` | 数据库连接地址 | `jdbc:mysql://120.26.170.213:3306/duda_nexus` |
| `spring.data.redis.host` | Redis主机地址 | `120.26.170.213` |
| `dubbo.protocol.port` | Dubbo协议端口 | `20882` (-1表示自动分配) |
| `duda.user.token-expire-hours` | Token过期时间 | `72` (小时) |

### 环境变量支持

配置支持环境变量替换，方便部署到不同环境:

```yaml
# 使用环境变量
username: ${NACOS_USERNAME:nacos}
password: ${NACOS_PASSWORD:nacos}
namespace: ${NACOS_NAMESPACE:duda-dev}
```

---

## ✅ 下一步

配置完成后，需要:

1. **创建数据库**: 参考 `database-schema.md`
2. **启动服务**: 运行 `duda-user-provider`
3. **验证注册**: 检查Nacos服务列表
4. **测试RPC调用**: 创建测试消费者

---

**配置状态**: ⏳ 待创建
**数据库状态**: ⏳ 待创建

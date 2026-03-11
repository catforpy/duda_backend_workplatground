# Nacos 配置创建指南 - 云服务器环境

## 登录 Nacos 控制台

1. 访问地址：http://120.26.170.213:8848/nacos
2. 用户名：`nacos`
3. 密码：`nacos`
4. 切换到命名空间：`duda-dev`

---

## 配置清单

需要在 Nacos 上创建以下配置：

### 配置 1：公共配置（common-dev.yml）

**基本信息：**
- Data ID: `common-dev.yml`
- Group: `COMMON_GROUP`
- 配置格式: `YAML`
- 配置内容: 见下方

**配置内容：**
```yaml
# 公共配置 - 开发环境
# 所有服务共享的基础配置

spring:
  # Redis 配置（云服务器）
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

  # RocketMQ 配置（云服务器）
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
    console: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%logger{50}]%msg%n'
```

---

### 配置 2：用户 API 服务配置（duda-user-api.yml）

**基本信息：**
- Data ID: `duda-user-api.yml`
- Group: `USER_GROUP`
- 配置格式: `YAML`
- 配置内容: 见下方

**配置内容：**
```yaml
# 用户 API 服务配置 - 开发环境

# SpringDoc OpenAPI 配置
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    disable-swagger-default-url: true
  show-actuator: false
  default-consumes-media-type: application/json
  default-produces-media-type: application/json

# JWT配置
jwt:
  config:
    secret: ${JWT_SECRET:duda-nexus-jwt-secret-key-2026-must-be-at-least-256-bits-long-for-hs256-algorithm}
    access-token-expiration: 900  # 15分钟（秒）
    refresh-token-expiration: 604800  # 7天（秒）
    token-prefix: "Bearer "
    header-key: "Authorization"
    refresh-token-prefix: "auth:refresh:"
    token-blacklist-prefix: "auth:blacklist:"
```

---

## 创建步骤

### 步骤 1：进入配置管理
1. 登录 Nacos 控制台
2. 点击左侧菜单「配置管理」→「配置列表」

### 步骤 2：选择命名空间
1. 在右上角「命名空间」下拉框中选择 `duda-dev`

### 步骤 3：创建配置
1. 点击右上角「+」按钮创建配置
2. 填写配置信息：
   - Data ID: 按上述清单填写
   - Group: 按上述清单填写
   - 配置格式: 选择 `YAML`
   - 配置内容: 复制粘贴上述内容
3. 点击「发布」

### 步骤 4：验证配置
配置发布后，在配置列表中应该能看到：

| Data ID | Group | 操作 |
|---------|-------|------|
| `common-dev.yml` | `COMMON_GROUP` | 编辑、删除、查看 |
| `duda-user-api.yml` | `USER_GROUP` | 编辑、删除、查看 |

---

## 配置说明

### 1. Data ID 命名规则
格式：`${service-name}-${profile}.${extension}`

示例：
- `common-dev.yml` - 公共配置，开发环境
- `duda-user-api.yml` - 用户API服务配置

### 2. Group 分组
- `COMMON_GROUP` - 公共配置，所有服务共享
- `USER_GROUP` - 用户相关服务配置
- `GATEWAY_GROUP` - 网关服务配置
- `AUTH_GROUP` - 认证服务配置

### 3. 共享配置机制
在 `bootstrap.yml` 中通过 `shared-configs` 引用：

```yaml
spring:
  cloud:
    nacos:
      config:
        shared-configs:
          - data-id: common-dev.yml
            group: COMMON_GROUP
            refresh: true
```

这样所有服务都能共享 Redis、RocketMQ 等公共配置。

---

## 配置验证

### 方法 1：通过控制台验证
1. 在配置列表中点击配置的「查看」按钮
2. 检查配置内容是否正确

### 方法 2：通过服务日志验证
启动应用后，查看日志是否成功拉取配置：

```bash
# 查看日志
tail -f /tmp/user-provider.log | grep -E "Nacos|config|refresher"

# 应该看到类似输出：
# [Nacos Config] Listening config: dataId=common-dev.yml, group=COMMON_GROUP
```

### 方法 3：通过 Actuator 端点验证
```bash
curl http://localhost:8082/actuator/refresh
```

---

## 常见问题

### Q1: 配置修改后不生效？
**A:** 检查 `bootstrap.yml` 中是否开启了 `refresh: true`

### Q2: 服务启动失败？
**A:** 检查配置内容格式是否正确，YAML 缩进是否正确

### Q3: 无法连接 Nacos？
**A:** 检查：
- Nacos 服务是否启动
- 网络是否畅通
- 用户名密码是否正确
- 命名空间是否正确

---

## 下一步

配置完成后：
1. ✅ 重启应用服务
2. ✅ 检查服务日志
3. ✅ 验证 Redis 连接
4. ✅ 验证 RocketMQ 连接
5. ✅ 在 Swagger 中测试 API

---

**配置文件路径参考：**
- 本地配置模板: `docs/nacos-configs/`
- Bootstrap 配置: `*/src/main/resources/bootstrap.yml`

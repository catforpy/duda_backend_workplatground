# Bootstrap 配置优化指南

## 🎯 目标
将 `duda-user-provider` 的配置从本地 `bootstrap.yml` 迁移到 Nacos 配置中心。

---

## ✅ 优化后的 bootstrap.yml（本地文件）

### **路径**: `duda-user-provider/src/main/resources/bootstrap.yml`

```yaml
# Bootstrap 配置 - 用户服务（优化版）

spring:
  application:
    name: duda-user-provider
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  cloud:
    nacos:
      server-addr: ${NACOS_HOST:120.26.170.213}:${NACOS_PORT:8848}
      username: ${NACOS_USERNAME:nacos}
      password: ${NACOS_PASSWORD:nacos}

      # 服务发现配置
      discovery:
        namespace: ${NACOS_NAMESPACE:duda-dev}
        group: USER_GROUP
        enabled: true
        register-enabled: true

      # 配置中心配置
      config:
        namespace: ${NACOS_NAMESPACE:duda-dev}
        group: USER_GROUP
        file-extension: yml
        refresh-enabled: true
        shared-configs:
          # 从 Nacos 拉取公共配置（Redis、RocketMQ、MyBatis-Plus等）
          - data-id: common-${spring.profiles.active:dev}.yml
            group: COMMON_GROUP
            refresh: true

# Dubbo 最小配置（启动必需）
dubbo:
  application:
    name: ${spring.application.name}
  protocol:
    name: dubbo
    port: -1  # -1表示自动分配端口
  registry:
    address: nacos://${spring.cloud.nacos.server-addr}
  scan:
    base-packages: com.duda.user.rpc
  consumer:
    check: false
    timeout: 5000
```

---

## 📋 Nacos 配置清单

### **配置1: duda-user-provider-dev.yml**

**命名空间**: `duda-dev`
**Group**: `USER_GROUP`
**Data ID**: `duda-user-provider-dev.yml`
**配置格式**: YAML

```yaml
# 服务器配置
server:
  port: 8082

# 数据库配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://120.26.170.213:3306/duda_nexus?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8
    username: root
    password: duda2024
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      validation-query: SELECT 1
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000

# Dubbo 完整配置
dubbo:
  registries:
    # Registry 1: 基础设施服务（INFRA_GROUP）- 用于调用ID生成器
    infraRegistry:
      address: nacos://120.26.170.213:8848
      parameters:
        namespace: duda-dev
        group: INFRA_GROUP
        use-as-config-center: false
        use-as-metadata-center: false
      default: false  # 不是默认 registry

    # Registry 2: 业务服务（USER_GROUP）- 默认 registry
    userRegistry:
      address: nacos://120.26.170.213:8848
      parameters:
        namespace: duda-dev
        group: USER_GROUP
        use-as-config-center: false
        use-as-metadata-center: false
      default: true  # 默认 registry

  consumer:
    timeout: 5000
    retries: 2

# RocketMQ 配置
rocketmq:
  name-server: 120.26.170.213:9876
  producer:
    group: user-producer-group
    send-message-timeout: 3000
    retry-times-when-send-failed: 2
    max-message-size: 4194304  # 4MB
    compress-msg-body-over-howmuch: 4096

# 健康检查配置（Actuator）
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
      show-components: always
  health:
    defaults:
      enabled: true
```

---

### **配置2: duda-user-provider-test.yml（测试环境）**

**命名空间**: `duda-test`
**Group**: `USER_GROUP`
**Data ID**: `duda-user-provider-test.yml`

```yaml
server:
  port: 8082

spring:
  datasource:
    url: jdbc:mysql://测试数据库地址:3306/duda_nexus?...
    username: test_user
    password: test_password

# 其他配置...
```

---

### **配置3: common-dev.yml（公共配置）**

**命名空间**: `duda-dev`
**Group**: `COMMON_GROUP`
**Data ID**: `common-dev.yml`

```yaml
# Redis 配置
spring:
  redis:
    host: 120.26.170.213
    port: 6379
    password: ''
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms

# MyBatis-Plus 配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.duda.*.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: ASSIGN_ID
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# 日志配置
logging:
  level:
    com.duda: debug
    org.springframework: info
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n"
```

---

## 🚀 迁移步骤

### **步骤1：备份现有配置**
```bash
cp duda-user-provider/src/main/resources/bootstrap.yml duda-user-provider/src/main/resources/bootstrap.yml.bak
```

### **步骤2：简化 bootstrap.yml**
将上面的「优化后的 bootstrap.yml」内容复制到本地文件。

### **步骤3：在 Nacos 创建配置**

1. 登录 Nacos 控制台
2. 进入「配置管理」→「配置列表」
3. 点击「+」创建配置

**配置1**:
- Data ID: `duda-user-provider-dev.yml`
- Group: `USER_GROUP`
- 配置格式: YAML
- 配置内容: 见上面的「配置1」

**配置2** (如果不存在):
- Data ID: `common-dev.yml`
- Group: `COMMON_GROUP`
- 配置格式: YAML
- 配置内容: 见上面的「配置3」

### **步骤4：启动应用验证**
```bash
cd duda-user-provider
mvn clean package
mvn spring-boot:run
```

### **步骤5：查看日志**
**预期输出**:
```
✓ 从 Nacos 加载配置: duda-user-provider-dev.yml
✓ 从 Nacos 加载公共配置: common-dev.yml
✓ 服务注册到 Nacos 成功: duda-user-provider
✓ Dubbo 服务导出成功
```

---

## 🔍 验证配置是否生效

### **方法1: 查看启动日志**
```bash
grep -i "nacos" logs/spring.log
```

### **方法2: Actuator 端点**
```bash
curl http://localhost:8082/actuator/health
```

### **方法3: Nacos 控制台**
- 进入「服务管理」→「服务列表」
- 查看命名空间 `duda-dev`
- 应该能看到 `duda-user-provider` 在线

---

## ⚠️ 常见问题

### **Q1: 启动报错 "找不到配置"**
**解决**:
- 检查 Nacos 配置是否存在
- 检查命名空间和 Group 是否正确
- 检查 Data ID 是否正确（`duda-user-provider-dev.yml`）

### **Q2: 数据库连接失败**
**解决**:
- 检查 Nacos 中的数据库配置是否正确
- 检查数据库是否可访问
- 查看日志中的具体错误信息

### **Q3: 服务未注册到 Nacos**
**解决**:
- 检查 `discovery.namespace` 和 `discovery.group` 是否正确
- 检查 Nacos 服务器是否可访问

---

## 📊 配置优先级

```
Nacos 配置中心（最高优先级）
    ↓
bootstrap.yml（本地）
    ↓
application.yml（本地，如果存在）
    ↓
默认值（最低优先级）
```

---

## 🎁 优势总结

| 优势 | 说明 |
|------|------|
| ✅ **集中管理** | 所有配置在 Nacos，统一管理 |
| ✅ **动态刷新** | 修改配置后支持热更新（`@RefreshScope`） |
| ✅ **环境隔离** | dev/test/prod 配置分离 |
| ✅ **版本回滚** | Nacos 保留历史版本 |
| ✅ **安全性** | 配置加密存储，按权限控制访问 |
| ✅ **多环境** | 一个应用，多个环境的配置 |

---

## 📞 需要帮助？

- Nacos 配置问题
- 配置迁移问题
- 应用启动问题

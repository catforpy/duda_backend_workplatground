# Nacos配置文件 - 直接复制到Nacos控制台使用

## 配置信息

**Data ID**: `duda-file-provider.yaml`
**Group**: `DUDA_FILE_GROUP`
**配置格式**: `YAML`
**配置内容**: 见下方

---

## 生产环境配置 (推荐)

```yaml
# ========================================
# duda-file模块 - 生产环境配置
# ========================================

# 数据库配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://你的数据库地址:3306/duda_file?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: 你的数据库用户名
    password: 你的数据库密码
    # 连接池配置
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

# API密钥加密配置 ⭐⭐⭐ 重要！
duda:
  file:
    encryption:
      # API密钥加密密钥
      # ⚠️ 生产环境请务必修改此密钥！
      # ⚠️ 密钥长度至少32字符，建议使用以下命令生成：
      #    openssl rand -base64 32
      # 示例生成的密钥: kK7x9P2qR8mN4vT6wY5zA1bC3dE7fG9hJ0lM2nO4pQ=
      key: "请使用openssl rand -base64 32命令生成的密钥替换这里"

    # 存储配置
    storage:
      default-type: ALIYUN_OSS
      default-region: cn-hangzhou

# 阿里云STS配置 (如果使用STS临时凭证)
aliyun:
  sts:
    # RAM角色ARN (在阿里云RAM控制台创建)
    role-arn: acs:ram::你的账号ID:role/duda-file-sts-role
    # STS接入点
    endpoint: sts.cn-hangzhou.aliyuncs.com
    # 默认过期时间(秒) 最小900秒(15分钟)，最大43200秒(12小时)
    default-duration: 3600

# MyBatis配置
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.duda.file.provider.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# Dubbo配置
dubbo:
  application:
    name: duda-file-provider
  protocol:
    name: dubbo
    port: 20880
    threads: 200
  registry:
    address: nacos://你的Nacos地址:8848
    group: DUDA_FILE_GROUP
  scan:
    base-packages: com.duda.file.provider
  provider:
    timeout: 60000
    retries: 0

# 日志配置
logging:
  level:
    root: INFO
    com.duda.file: INFO
    com.duda.file.mapper: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
  file:
    name: logs/duda-file-provider.log
    max-size: 100MB
    max-history: 30

# 服务器配置
server:
  port: 8080
  servlet:
    context-path: /
  tomcat:
    threads:
      max: 200
      min-spare: 10
```

---

## 开发环境配置

```yaml
# ========================================
# duda-file模块 - 开发环境配置
# ========================================

# 数据库配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/duda_file?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
    hikari:
      minimum-idle: 2
      maximum-pool-size: 10

# API密钥加密配置
duda:
  file:
    encryption:
      # 开发环境密钥 (生产环境必须更换！)
      key: "duda-file-dev-encryption-key-2025-v1"
    storage:
      default-type: ALIYUN_OSS
      default-region: cn-hangzhou

# MyBatis配置
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.duda.file.provider.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# Dubbo配置
dubbo:
  application:
    name: duda-file-provider-dev
  protocol:
    name: dubbo
    port: 20880
  registry:
    address: nacos://localhost:8848
    group: DUDA_FILE_GROUP
  scan:
    base-packages: com.duda.file.provider

# 日志配置
logging:
  level:
    root: INFO
    com.duda.file: DEBUG
    com.duda.file.mapper: DEBUG

server:
  port: 8080
```

---

## 测试环境配置

```yaml
# ========================================
# duda-file模块 - 测试环境配置
# ========================================

# 数据库配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://test-db-host:3306/duda_file?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: test_user
    password: test_password
    hikari:
      minimum-idle: 3
      maximum-pool-size: 15

# API密钥加密配置
duda:
  file:
    encryption:
      # 测试环境密钥 (与开发、生产环境不同)
      key: "duda-file-test-encryption-key-2025-v1"
    storage:
      default-type: ALIYUN_OSS
      default-region: cn-hangzhou

# MyBatis配置
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.duda.file.provider.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# Dubbo配置
dubbo:
  application:
    name: duda-file-provider-test
  protocol:
    name: dubbo
    port: 20881
  registry:
    address: nacos://test-nacos-host:8848
    group: DUDA_FILE_GROUP
  scan:
    base-packages: com.duda.file.provider

# 日志配置
logging:
  level:
    root: INFO
    com.duda.file: INFO
    com.duda.file.mapper: INFO

server:
  port: 8081
```

---

## 🔑 如何生成加密密钥

### 方法1: 使用OpenSSL命令 (推荐)

```bash
# 生成32字节随机密钥并Base64编码
openssl rand -base64 32

# 输出示例:
# kK7x9P2qR8mN4vT6wY5zA1bC3dE7fG9hJ0lM2nO4pQ=
```

### 方法2: 使用在线工具

访问: https://www.random.org/passwords/

参数设置:
- 长度: 32字符
- 包含: 大写字母、小写字母、数字、符号

### 方法3: 使用Python

```python
import secrets
import base64

# 生成32字节随机密钥
key_bytes = secrets.token_bytes(32)
key = base64.b64encode(key_bytes).decode('utf-8')
print(f"生成的加密密钥: {key}")
```

---

## 📋 Nacos配置步骤

### 第1步: 登录Nacos控制台

```
URL: http://你的Nacos地址:8848/nacos
用户名: nacos
密码: nacos
```

### 第2步: 创建配置

1. 点击左侧菜单 **配置管理** → **配置列表**
2. 点击右上角 **+** 按钮（创建配置）
3. 填写以下信息:

```
Data ID:        duda-file-provider.yaml
Group:          DUDA_FILE_GROUP
配置格式:       YAML
配置内容:       复制上面任一环境的配置内容
配置描述:       duda-file模块配置文件
```

4. 点击 **发布** 按钮

### 第3步: 验证配置

1. 在配置列表中找到 `duda-file-provider.yaml`
2. 点击 **编辑** 或 **详情** 查看配置内容
3. 确认 `duda.file.encryption.key` 已正确配置

---

## ⚠️ 重要注意事项

### 1. 生产环境安全

- ❌ **不要使用** 默认密钥 `duda-file-dev-encryption-key-2025`
- ❌ **不要使用** 开发环境密钥
- ❌ **不要在代码中硬编码** 密钥
- ✅ **必须使用** openssl生成的32字节随机密钥
- ✅ **定期轮换** 密钥（建议每季度）

### 2. 环境隔离

- 开发环境、测试环境、生产环境使用**不同的密钥**
- 不同的环境使用**不同的配置文件**（通过Data ID区分）
- 生产环境配置权限严格管理

### 3. 密钥轮换

如果需要更换密钥:

1. 生成新密钥: `openssl rand -base64 32`
2. 登录Nacos控制台
3. 找到 `duda-file-provider.yaml` 配置
4. 修改 `duda.file.encryption.key` 的值
5. 点击发布
6. 重启duda-file-provider服务（如需要）
7. 重新加密数据库中已存在的API密钥

---

## 🚀 快速开始

### 开发环境快速配置

1. 生成密钥:
```bash
openssl rand -base64 32
# 假设输出: abcdefgh1234567890abcdefghij=
```

2. 在Nacos创建配置，使用开发环境配置模板

3. 只需修改这几项:
```yaml
# 数据库地址
spring.datasource.url: jdbc:mysql://localhost:3306/duda_file

# 数据库用户名密码
spring.datasource.username: root
spring.datasource.password: root

# Nacos地址
dubbo.registry.address: nacos://localhost:8848

# 加密密钥
duda.file.encryption.key: "abcdefgh1234567890abcdefghij="
```

4. 启动应用即可！

---

## 📞 需要帮助？

如果配置过程中遇到问题:

1. 检查Nacos服务是否正常运行
2. 检查Data ID和Group是否正确
3. 检查YAML格式是否正确（注意缩进）
4. 查看应用日志，确认配置是否加载成功

---

**更新时间**: 2025-03-13
**版本**: v1.0

# 用户中台服务 - 启动和测试指南

## 📋 目录

1. [环境准备](#环境准备)
2. [数据库初始化](#数据库初始化)
3. [启动服务](#启动服务)
4. [测试接口](#测试接口)
5. [常见问题](#常见问题)

---

## 一、环境准备

### 1.1 确保以下服务已启动

```bash
# 1. MySQL 数据库（主库）
# IP: 120.26.170.213:3306
# 用户: root / duda2024

# 2. Redis
# IP: 120.26.170.213:6379
# 密码: duda2024

# 3. Nacos
# IP: 120.26.170.213:8848
# 用户名: nacos / 密码: nacos
```

### 1.2 在Nacos中创建配置

#### 1.2.1 创建命名空间

```bash
命名空间ID: duda-dev (自动生成)
命名空间名称: 都达开发环境
```

#### 1.2.2 创建公共配置

**配置名**: `common-dev.yml`
**分组**: `COMMON_GROUP`

```yaml
spring:
  data:
    redis:
      host: 120.26.170.213
      port: 6379
      password: duda2024
      database: 0
      jedis:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
```

#### 1.2.3 创建用户服务配置

**配置名**: `duda-user-provider-dev.yml`
**分组**: `USER_GROUP`

```yaml
server:
  port: 8082

spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://120.26.170.213:3306/duda_user?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: duda2024
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 20
      max-wait: 60000

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.duda.user.entity
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
```

---

## 二、数据库初始化

### 2.1 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS duda_user
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE duda_user;
```

### 2.2 创建用户表

```sql
CREATE TABLE users (
  -- 基本信息
  id VARCHAR(50) PRIMARY KEY COMMENT '用户ID',
  user_type VARCHAR(50) NOT NULL COMMENT '用户类型: platform_admin=平台管理员, service_provider=服务商, platform_account=都达网账户, backend_admin=后台管理员',
  username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
  phone VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号',
  email VARCHAR(100) COMMENT '邮箱',

  -- 状态信息
  status VARCHAR(20) NOT NULL DEFAULT 'inactive' COMMENT '账户状态: active=激活, inactive=未激活, frozen=冻结, deleted=已删除',
  last_login_time DATETIME COMMENT '最后登录时间',
  last_login_ip VARCHAR(50) COMMENT '最后登录IP',

  -- 时间戳
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

  -- 索引
  INDEX idx_user_type (user_type) COMMENT '用户类型索引',
  INDEX idx_status (status) COMMENT '状态索引',
  INDEX idx_phone (phone) COMMENT '手机号索引',
  INDEX idx_email (email) COMMENT '邮箱索引',
  INDEX idx_create_time (create_time) COMMENT '创建时间索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='基础用户表';
```

---

## 三、启动服务

### 3.1 编译项目

```bash
cd /Volumes/DudaDate/DudaNexus
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 编译整个项目（或只编译用户模块）
mvn clean compile -pl 'duda-usercenter/duda-user-provider' -am
```

### 3.2 启动服务

#### 方式1：IDEA中启动

找到 `UserProviderApplication.java`，右键 → Run

#### 方式2：命令行启动

```bash
cd /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-provider

mvn spring-boot:run
```

### 3.3 验证启动成功

看到以下日志表示启动成功：

```
用户服务启动成功！
Started UserProviderApplication in 15.234 seconds
```

访问：http://localhost:8082/doc.html （Swagger文档）

---

## 四、测试接口

### 4.1 测试用户注册

```bash
curl -X POST http://localhost:8082/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "userType": "platform_account",
    "username": "test001",
    "password": "Test@1234",
    "realName": "测试用户",
    "phone": "13800138000",
    "email": "test@example.com"
  }'
```

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": "1234567890123456789",  // 用户ID
  "timestamp": 1678901234567
}
```

### 4.2 测试用户登录

```bash
curl -X POST http://localhost:8082/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test001",
    "password": "Test@1234"
  }'
```

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "1234567890123456789",
    "userType": "platform_account",
    "username": "test001",
    "realName": "测试用户",
    "phone": "13800138000",
    "email": "test@example.com",
    "status": "active",
    "lastLoginTime": "2026-03-10T18:00:00",
    "lastLoginIp": "127.0.0.1",
    "createTime": "2026-03-10T17:00:00",
    "updateTime": "2026-03-10T18:00:00"
  },
  "timestamp": 1678901234567
}
```

### 4.3 测试获取用户信息

```bash
curl -X GET http://localhost:8082/user/1234567890123456789
```

### 4.4 测试分页查询

```bash
curl -X GET "http://localhost:8082/user/page?userType=platform_account&pageNum=1&pageSize=10"
```

---

## 五、常见问题

### 5.1 启动报错：无法连接Nacos

**原因**: Nacos服务未启动或配置错误

**解决**:
```bash
# 检查Nacos是否启动
curl http://120.26.170.213:8848/nacos/

# 检查用户名密码是否正确
```

### 5.2 启动报错：无法连接数据库

**原因**: 数据库未启动或配置错误

**解决**:
```bash
# 检查数据库连接
mysql -h120.26.170.213 -uroot -pduda2024 -e "SELECT 1"

# 检查数据库是否存在
mysql -h120.26.170.213 -uroot -pduda2024 -e "SHOW DATABASES LIKE 'duda_user'"
```

### 5.3 注册时报错：用户名已存在

**原因**: 数据库中已有该用户名

**解决**: 使用不同的用户名或删除现有数据

```sql
USE duda_user;
DELETE FROM users WHERE username = 'test001';
```

### 5.4 登录时报错：用户名或密码错误

**原因**: 密码使用BCrypt加密，验证失败

**检查**:
```sql
SELECT username, password_hash FROM users WHERE username = 'test001';
```

---

## 六、下一步

✅ **完成的功能**:
1. 用户注册（密码BCrypt加密）
2. 用户登录（密码验证）
3. 用户信息查询（支持缓存）
4. 用户信息更新
5. 用户删除（软删除）
6. 分页查询

⏭️ **待开发的功能**:
1. JWT Token生成和验证
2. 权限控制（基于用户类型）
3. 手机验证码登录
4. 微信登录（都达网账户）
5. 服务商管理
6. 都达网账户管理
7. 公司管理
8. 小程序管理

---

**测试完成后，请确认：**
- ✅ 注册功能正常
- ✅ 登录功能正常
- ✅ 缓存功能正常
- ✅ 数据库读写正常
- ✅ Swagger文档可访问

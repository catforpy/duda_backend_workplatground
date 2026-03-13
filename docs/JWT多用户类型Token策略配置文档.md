# JWT 多用户类型 Token 策略配置文档

## 📋 更新日期
2026-03-13

## 🎯 更新目标
实现不同用户类型使用不同的 JWT Token 策略，提升系统安全性。

## 🔑 核心特性

### 1. 不同用户类型使用不同的签名密钥
每种用户类型使用独立的 JWT 密钥，避免单一密钥泄露影响所有用户。

### 2. 不同用户类型使用不同的 Token 过期时间
根据用户权限等级设置不同的 Access Token 和 Refresh Token 过期时间：
- 高权限用户：短过期时间，降低安全风险
- 低权限用户：长过期时间，提升用户体验

## 📊 用户类型与 Token 策略对照表

| 用户类型 | Access Token 过期时间 | Refresh Token 过期时间 | 安全级别 | 密钥名称 |
|---------|---------------------|----------------------|---------|---------|
| **platform-admin**<br/>平台管理员 | 15分钟<br/>（900秒） | 7天<br/>（604800秒） | 🔴 最高 | platform-admin-secret |
| **service-provider**<br/>服务商 | 2小时<br/>（7200秒） | 30天<br/>（2592000秒） | 🟡 中等 | service-provider-secret |
| **platform-account**<br/>平台账号（普通用户） | 7天<br/>（604800秒） | 60天<br/>（5184000秒） | 🟢 较低 | platform-account-secret |
| **backend-admin**<br/>后台管理员 | 30分钟<br/>（1800秒） | 7天<br/>（604800秒） | 🟠 高 | backend-admin-secret |

## 🔧 技术实现

### 1. JwtProperties 配置类

**位置：** `duda-common/duda-common-security/src/main/java/com/duda/common/security/properties/JwtProperties.java`

**特点：**
- 使用嵌套类结构，清晰组织配置
- 支持 `@ConfigurationProperties` 从 Nacos 自动加载配置
- 提供便捷方法根据用户类型获取配置

**配置结构：**
```java
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private Common common = new Common();        // 通用配置
    private UserType userType = new UserType();  // 用户类型配置

    // 根据用户类型获取配置的方法
    public String getUserTypeSecret(String userType) { ... }
    public Long getUserTypeAccessTokenExpiration(String userType) { ... }
    public Long getUserTypeRefreshTokenExpiration(String userType) { ... }

    // 嵌套类：Common, UserType, PlatformAdmin, ServiceProvider, etc.
}
```

### 2. JwtTokenProvider Token 生成工具

**位置：** `duda-common/duda-common-security/src/main/java/com/duda/common/security/util/JwtTokenProvider.java`

**核心方法：**
- `generateAccessToken(userId, username, userType)` - 生成 Access Token
- `generateRefreshToken(userId, userType)` - 生成 Refresh Token
- `validateToken(token)` - 验证 Token（自动选择正确的密钥）
- `getSigningKeyByUserType(userType)` - 根据用户类型获取签名密钥

**关键逻辑：**
```java
// 生成 Token 时使用用户类型特定的密钥和过期时间
public String generateAccessToken(Long userId, String username, String userType) {
    Long expiration = jwtProperties.getUserTypeAccessTokenExpiration(userType);
    return createToken(claims, expiration, userType);
}

// 验证 Token 时从 Token 中提取 userType，使用对应的密钥验证
public boolean validateToken(String token) {
    String userType = getUserTypeFromToken(token);
    SecretKey signingKey = getSigningKeyByUserType(userType);
    // 使用对应的密钥验证签名
}
```

### 3. TokenService Token 管理服务

**位置：** `duda-common/duda-common-security/src/main/java/com/duda/common/security/service/TokenService.java`

**功能：**
- 生成 Access Token 和 Refresh Token
- 存储 Refresh Token 到 Redis（使用用户类型特定的过期时间）
- 刷新 Access Token
- 登出（将 Access Token 加入黑名单，删除 Refresh Token）
- 验证 Access Token

### 4. Nacos 配置

**Data ID：** `jwt-config.yml`
**Group：** `COMMON_GROUP`
**命名空间：** `duda-dev` (开发环境)

**配置内容：**
```yaml
jwt:
  # 通用配置
  common:
    token-prefix: "Bearer "
    header-key: "Authorization"
    refresh-token-prefix: "auth:refresh:"
    token-blacklist-prefix: "auth:blacklist:"

  # 不同用户类型的配置
  user-type:
    platform-admin:
      secret: "duda-platform-admin-secret-key-2026-must-be-at-least-256-bits-for-hs256"
      access-expiration: 900    # 15分钟
      refresh-expiration: 604800  # 7天

    service-provider:
      secret: "duda-service-provider-secret-key-2026-must-be-at-least-256-bits-for-hs256"
      access-expiration: 7200   # 2小时
      refresh-expiration: 2592000  # 30天

    platform-account:
      secret: "duda-platform-account-secret-key-2026-must-be-at-least-256-bits-for-hs256"
      access-expiration: 604800  # 7天
      refresh-expiration: 5184000  # 60天

    backend-admin:
      secret: "duda-backend-admin-secret-key-2026-must-be-at-least-256-bits-for-hs256"
      access-expiration: 1800   # 30分钟
      refresh-expiration: 604800  # 7天
```

### 5. Bootstrap 配置

**位置：** `duda-usercenter/duda-user-api/src/main/resources/bootstrap.yml`

**Nacos 配置导入：**
```yaml
spring:
  cloud:
    nacos:
      config:
        shared-configs:
          # JWT 配置
          - data-id: jwt-config.yml
            group: COMMON_GROUP
            refresh: true
          # 公共配置（Redis, RocketMQ, MyBatis-Plus等）
          - data-id: common-${spring.profiles.active:dev}.yml
            group: COMMON_GROUP
            refresh: true
```

## 📝 使用示例

### 1. 服务商登录

**请求：**
```bash
POST /api/auth/v2/service-provider/login/password
Content-Type: application/json

{
  "username": "merchant1",
  "password": "password123",
  "clientIp": "192.168.1.1",
  "clientType": "web",
  "deviceId": "device_uuid_xxx"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,  // 服务商的 Access Token：2小时
    "userType": "service_provider",
    "username": "merchant1"
  }
}
```

**日志验证：**
```
2026-03-13 15:11:38 INFO  JwtTokenProvider - 生成Access Token，userType=service_provider, expiration=7200秒
2026-03-13 15:11:38 INFO  JwtTokenProvider - 生成Refresh Token，userType=service_provider, expiration=2592000秒
```

## 🔐 安全优势

### 1. 密钥隔离
- 每种用户类型使用独立的密钥
- 即使普通用户的密钥泄露，也不会影响管理员账户
- 可以单独撤销某个用户类型的密钥

### 2. 过期时间分级
- 高权限用户：短 Token 有效期，降低泄露风险
- 普通用户：长 Token 有效期，提升体验
- 符合最小权限原则

### 3. 审计与监控
- 日志中清晰记录用户类型和过期时间
- 便于审计和异常检测

## 🚀 部署步骤

### 1. 更新代码
```bash
# 编译 common-security 模块
cd /Volumes/DudaDate/DudaNexus
mvn clean install -pl duda-common/duda-common-security -am -DskipTests

# 编译 user-api 模块
mvn clean install -pl duda-usercenter/duda-user-api -am -DskipTests
```

### 2. 更新 Nacos 配置
1. 登录 Nacos 控制台：http://120.26.170.213:8848/nacos
2. 命名空间：`duda-dev`
3. 配置管理 → 配置列表
4. 创建或编辑 `jwt-config.yml` (Group: `COMMON_GROUP`)
5. 粘贴配置内容（参考上面的配置示例）
6. 发布配置

### 3. 重新构建 Docker 镜像
```bash
cd duda-usercenter/duda-user-api
docker build -t duda-user-api:latest .
```

### 4. 重启服务
```bash
# 停止并删除旧容器
docker stop duda-user-api
docker rm duda-user-api

# 启动新容器
docker run -d --name duda-user-api \
  -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e NACOS_HOST=120.26.170.213 \
  -e NACOS_PORT=8848 \
  -e NACOS_USERNAME=nacos \
  -e NACOS_PASSWORD=nacos \
  -e NACOS_NAMESPACE=duda-dev \
  --network duda-network \
  duda-user-api:latest
```

### 5. 验证配置
查看日志确认配置加载：
```bash
docker logs duda-user-api | grep "jwt-config.yml"
```

预期输出：
```
[Nacos Config] Listening config: dataId=jwt-config.yml, group=COMMON_GROUP
```

### 6. 测试登录
使用不同用户类型登录，验证 Token 过期时间：
- 服务商：7200秒（2小时）
- 平台管理员：900秒（15分钟）
- 平台账号：604800秒（7天）

## ⚠️ 注意事项

### 1. 密钥管理
- **生产环境必须修改默认密钥**
- 建议从环境变量读取密钥，而不是硬编码
- 密钥长度至少 256 位（HS256 算法要求）

### 2. 配置热更新
- Nacos 配置支持热更新
- 修改 `jwt-config.yml` 后会自动刷新
- 新配置对新登录生效，已发出的 Token 不受影响

### 3. 向后兼容
- 保留旧的 `getSigningKey()` 方法（标记为 @Deprecated）
- Token Payload 中包含 `userType` 字段，用于选择正确的密钥验证
- 旧 Token 在过期前仍可正常使用

### 4. 测试建议
- 分别测试不同用户类型的登录
- 验证 Token 过期时间
- 验证 Refresh Token 功能
- 验证登出功能（黑名单）

## 📚 相关文档

- [Nacos 配置导入问题解决方案](./Nacos配置导入问题解决方案.md)
- [登录接口使用说明](./登录接口使用说明.md)
- [服务商接口使用说明](./服务商接口使用说明.md)
- [短信服务使用说明](./短信服务使用说明.md)

## 🔄 更新历史

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-03-13 | 1.0 | 初始版本，实现多用户类型 Token 策略 |

---

**作者：** DudaNexus Team
**最后更新：** 2026-03-13

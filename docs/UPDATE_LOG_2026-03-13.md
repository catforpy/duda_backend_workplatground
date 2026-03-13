# 项目更新日志 - 2026-03-13

## 🎯 本次更新概览

### 核心功能：JWT 多用户类型 Token 策略

实现不同用户类型使用不同的 JWT 密钥和过期时间，提升系统安全性。

---

## 📝 详细更新内容

### 1. JWT 配置重构

#### 文件：`duda-common/duda-common-security/src/main/java/com/duda/common/security/properties/JwtProperties.java`

**主要变更：**
- ✅ 从扁平化属性改为嵌套类结构
- ✅ 支持 `jwt.common.*` 和 `jwt.user-type.*` 配置
- ✅ 每个用户类型独立的密钥和过期时间配置
- ✅ 新增方法：`getUserTypeSecret()`, `getUserTypeAccessTokenExpiration()`, `getUserTypeRefreshTokenExpiration()`

**新增嵌套类：**
- `Common` - 通用配置（token-prefix, header-key 等）
- `UserType` - 用户类型配置容器
- `PlatformAdmin` - 平台管理员配置
- `ServiceProvider` - 服务商配置
- `PlatformAccount` - 平台账号配置
- `BackendAdmin` - 后台管理员配置

### 2. JWT Token 生成工具更新

#### 文件：`duda-common/duda-common-security/src/main/java/com/duda/common/security/util/JwtTokenProvider.java`

**主要变更：**
- ✅ `generateAccessToken()` 方法新增 `userType` 参数
- ✅ `generateRefreshToken()` 方法新增 `userType` 参数
- ✅ `createToken()` 方法根据用户类型使用不同的密钥和过期时间
- ✅ `getSigningKeyByUserType()` 方法根据用户类型获取对应的签名密钥
- ✅ `validateToken()` 方法从 Token 中提取 `userType`，使用对应密钥验证
- ✅ 修复 `getSigningKey()` 方法调用已废弃的 `getSecret()` 的问题

**关键代码：**
```java
// 根据用户类型获取密钥
private SecretKey getSigningKeyByUserType(String userType) {
    String secret = jwtProperties.getUserTypeSecret(userType);
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
}

// 验证 Token 时使用对应密钥
public boolean validateToken(String token) {
    String userType = getUserTypeFromToken(token);
    SecretKey signingKey = getSigningKeyByUserType(userType);
    // 使用对应密钥验证
}
```

### 3. Token 服务更新

#### 文件：`duda-common/duda-common-security/src/main/java/com/duda/common/security/service/TokenService.java`

**主要变更：**
- ✅ `generateTokens()` 方法传递 `userType` 参数
- ✅ Refresh Token 存储到 Redis 时使用用户类型特定的过期时间
- ✅ 返回的 TokenDTO 包含用户类型对应的过期时间

### 4. Nacos 配置文件

#### 新增文件：`sql/nacos-jwt-config.yml`

**配置内容：**
- JWT 通用配置（token-prefix, header-key, refresh-token-prefix, token-blacklist-prefix）
- 4 种用户类型的独立配置（密钥、Access Token 过期时间、Refresh Token 过期时间）

**用户类型与过期时间：**
| 用户类型 | Access Token | Refresh Token |
|---------|-------------|---------------|
| platform-admin | 15分钟 (900秒) | 7天 (604800秒) |
| service-provider | 2小时 (7200秒) | 30天 (2592000秒) |
| platform-account | 7天 (604800秒) | 60天 (5184000秒) |
| backend-admin | 30分钟 (1800秒) | 7天 (604800秒) |

### 5. Bootstrap 配置更新

#### 文件：`duda-usercenter/duda-user-api/src/main/resources/bootstrap.yml`

**主要变更：**
- ✅ 新增 `jwt-config.yml` 导入配置
- ✅ 配置在 `COMMON_GROUP` 组下
- ✅ 启用配置热更新 (`refresh: true`)

### 6. 文档更新

#### 新增文档：
- ✅ `docs/JWT多用户类型Token策略配置文档.md` - 完整的技术实现文档
- ✅ `docs/UPDATE_LOG_2026-03-13.md` - 本次更新日志（本文件）
- ✅ `docs/jwt-config-example.yml` - JWT 配置示例

---

## 🧪 测试验证

### 功能测试
- ✅ 服务商密码登录成功
- ✅ Token 过期时间正确：7200秒（2小时）
- ✅ Token Payload 包含正确的 userType
- ✅ JWT 配置从 Nacos 成功加载

### 日志验证
```
2026-03-13 15:11:38 INFO  JwtTokenProvider - 生成Access Token，userType=service_provider, expiration=7200秒
2026-03-13 15:11:38 INFO  JwtTokenProvider - 生成Refresh Token，userType=service_provider, expiration=2592000秒
2026-03-13 15:11:38 INFO  TokenService - 生成Token成功，userId: 4943568288616448, username: liuxizhuang, userType: service_provider
```

### 配置验证
```
[Nacos Config] Listening config: dataId=jwt-config.yml, group=COMMON_GROUP
```

---

## 📦 部署变更

### Docker 镜像
- ✅ 重新构建 `duda-user-api:latest` 镜像
- ✅ 镜像包含最新的 JWT 配置代码

### 容器
- ✅ 重启 `duda-user-api` 容器
- ✅ 启动 `duda-user-provider` 容器
- ✅ 启动 `duda-msg-provider` 容器
- ✅ 启动 `duda-id-generator` 容器

### 运行状态
```
CONTAINER ID   IMAGE                              STATUS
duda-user-api       duda/user-api:latest           Up 6 minutes
duda-user-provider  duda/user-provider:1.0.0      Up About a minute (healthy)
duda-msg-provider   duda/msg-provider:1.0.0       Up 15 seconds (health: starting)
duda-id-generator   duda/id-generator-provider:1.0.0   Up 15 seconds (healthy)
```

---

## 🔄 兼容性

### 向后兼容
- ✅ 保留旧的 `getSigningKey()` 方法（标记为 @Deprecated）
- ✅ Token Payload 包含 `userType` 字段，用于密钥选择
- ✅ 旧 Token 在过期前仍可正常验证

### 数据库变更
- ❌ 无数据库变更

### API 变更
- ❌ 无 API 变更

---

## ⚠️ 注意事项

### 安全建议
1. **生产环境必须修改默认密钥**
2. 建议从环境变量读取密钥
3. 定期轮换密钥

### 配置管理
1. Nacos 配置支持热更新
2. 修改 `jwt-config.yml` 后自动刷新
3. 新配置对新登录生效，已发出的 Token 不受影响

### 测试建议
1. 测试不同用户类型的登录
2. 验证 Token 过期时间
3. 验证 Refresh Token 功能
4. 验证登出功能（黑名单）

---

## 📚 相关文档

- [JWT多用户类型Token策略配置文档.md](./JWT多用户类型Token策略配置文档.md)
- [Nacos配置导入问题解决方案.md](./Nacos配置导入问题解决方案.md)
- [登录接口使用说明.md](./登录接口使用说明.md)

---

## 🔗 问题与解决

### 问题 1：Nacos 配置无法绑定到 Map 属性
**现象：** 使用 `Map<String, String> userTypeSecrets` 时，配置无法从 Nacos 加载

**解决方案：** 改用嵌套类结构（`UserType`, `PlatformAdmin` 等），Spring Boot `@ConfigurationProperties` 可以正确绑定

### 问题 2：Token 验证失败
**现象：** 使用错误的密钥验证 Token

**解决方案：** 从 Token Payload 中提取 `userType`，使用对应的密钥验证

### 问题 3：登录返回 500 错误
**现象：** `No provider available from registry`

**解决方案：** 启动 `duda-user-provider` 和 `duda-msg-provider` 服务

---

## 👥 贡献者

- DudaNexus Team

---

**更新日期：** 2026-03-13
**版本：** 1.0.0

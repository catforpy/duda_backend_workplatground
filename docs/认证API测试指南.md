# 认证 API 测试指南

## 快速开始

### 1. 启动服务

```bash
cd /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-api
mvn spring-boot:run
```

### 2. 访问 Swagger UI

```
http://localhost:8082/swagger-ui.html
```

### 3. 测试接口（在 Swagger 中直接点击 "Try it out"）

---

## API 接口测试

### 场景1：普通用户 - 手机号注册

**接口**: `POST /api/auth/register`

**请求体**:
```json
{
  "userType": "normal",
  "registerType": "phone",
  "username": "testuser001",
  "password": "pass123456",
  "confirmPassword": "pass123456",
  "phone": "13800138000",
  "phoneVerifyCode": "123456",
  "realName": "测试用户",
  "clientIp": "127.0.0.1"
}
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1000123456789,
    "username": "testuser001",
    "userType": "normal",
    "userTypeName": "普通用户",
    "phone": "138****8000",
    "status": "active",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
}
```

---

### 场景2：普通用户 - 手机号登录

**接口**: `POST /api/auth/login`

**请求体**:
```json
{
  "loginType": "phone_password",
  "userType": "normal",
  "phone": "13800138000",
  "password": "pass123456",
  "clientIp": "127.0.0.1"
}
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "userId": 1000123456789,
    "username": "testuser001",
    "userType": "normal",
    "userTypeName": "普通用户",
    "phone": "138****8000",
    "status": "active"
  }
}
```

---

### 场景3：商家用户 - 手机号注册

**接口**: `POST /api/auth/register`

**请求体**:
```json
{
  "userType": "merchant",
  "registerType": "phone",
  "username": "merchant001",
  "password": "pass123456",
  "confirmPassword": "pass123456",
  "phone": "13800138001",
  "phoneVerifyCode": "123456",
  "realName": "测试商家",
  "clientIp": "127.0.0.1"
}
```

---

### 场景4：商家用户 - 手机号登录

**接口**: `POST /api/auth/login`

**请求体**:
```json
{
  "loginType": "phone_password",
  "userType": "merchant",
  "phone": "13800138001",
  "password": "pass123456",
  "clientIp": "127.0.0.1"
}
```

---

### 场景5：邮箱注册

**接口**: `POST /api/auth/register`

**请求体**:
```json
{
  "userType": "normal",
  "registerType": "email",
  "username": "emailuser001",
  "password": "pass123456",
  "confirmPassword": "pass123456",
  "email": "testuser@example.com",
  "emailVerifyCode": "123456",
  "clientIp": "127.0.0.1"
}
```

---

### 场景6：邮箱登录

**接口**: `POST /api/auth/login`

**请求体**:
```json
{
  "loginType": "email_password",
  "userType": "normal",
  "email": "testuser@example.com",
  "password": "pass123456",
  "clientIp": "127.0.0.1"
}
```

---

### 场景7：第三方注册（微信）

**接口**: `POST /api/auth/register`

**请求体**:
```json
{
  "userType": "normal",
  "registerType": "third_party",
  "thirdPartyPlatform": "wechat",
  "thirdPartyOpenId": "oTEST_OPEN_ID_001",
  "thirdPartyUnionId": "uTEST_UNION_ID_001",
  "thirdPartyNickname": "微信用户",
  "thirdPartyAvatar": "https://thirdparty.com/avatar.jpg",
  "clientIp": "127.0.0.1"
}
```

---

### 场景8：第三方登录（微信）

**接口**: `POST /api/auth/login`

**请求体**:
```json
{
  "loginType": "third_party",
  "userType": "normal",
  "thirdPartyPlatform": "wechat",
  "thirdPartyAuthCode": "test_auth_code_001",
  "thirdPartyOpenId": "oTEST_OPEN_ID_001",
  "clientIp": "127.0.0.1"
}
```

---

## 使用 curl 测试

### 1. 手机号注册

```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "userType": "normal",
    "registerType": "phone",
    "username": "testuser001",
    "password": "pass123456",
    "confirmPassword": "pass123456",
    "phone": "13800138000",
    "phoneVerifyCode": "123456",
    "realName": "测试用户",
    "clientIp": "127.0.0.1"
  }'
```

### 2. 手机号登录

```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "loginType": "phone_password",
    "userType": "normal",
    "phone": "13800138000",
    "password": "pass123456",
    "clientIp": "127.0.0.1"
  }'
```

### 3. 刷新Token

```bash
curl -X POST "http://localhost:8082/api/auth/refresh?refreshToken=YOUR_REFRESH_TOKEN"
```

### 4. 验证Token

```bash
curl -X GET http://localhost:8082/api/auth/validate \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 数据库验证

### 查看用户数据

```sql
-- 连接数据库
mysql -h 120.26.170.213 -P 3306 -u root -pduda2024 duda_nexus

-- 查看users表（应该只有20个字段）
DESC users_00;

-- 查看用户数据
SELECT id, username, phone, email, user_type, status FROM users_00;

-- 查看user_profiles表
SELECT * FROM user_profiles;

-- 查看user_third_parties表
SELECT * FROM user_third_parties;
```

---

## 常见问题

### 1. 端口冲突

如果端口8082被占用，修改 `duda-user-api/src/main/resources/bootstrap.yml`:

```yaml
server:
  port: 8083  # 改成其他端口
```

### 2. 数据库连接失败

检查 `bootstrap.yml` 中的数据库配置：

```yaml
datasource:
  url: jdbc:mysql://120.26.170.213:3306/duda_nexus?...
  username: root
  password: duda2024
```

### 3. Nacos 连接失败

确保 Nacos 已启动：

```bash
# 检查 Nacos 状态
curl http://120.26.170.213:8848/nacos/
```

### 4. Token 验证失败

确保使用完整的 Token，包括 "Bearer " 前缀：

```bash
# 正确
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# 错误（缺少 Bearer）
Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 12种登录组合

### 普通用户（normal）

1. ✅ 手机号注册 → 手机号登录
2. ✅ 邮箱注册 → 邮箱登录
3. ✅ 微信注册 → 微信登录

### 商家用户（merchant）

4. ✅ 手机号注册 → 手机号登录
5. ✅ 邮箱注册 → 邮箱登录
6. ✅ 微信注册 → 微信登录

### 运营人员（operator）

7. ✅ 手机号注册 → 手机号登录
8. ✅ 邮箱注册 → 邮箱登录
9. ✅ 微信注册 → 微信登录

### 管理员（admin）

10. ✅ 手机号注册 → 手机号登录
11. ✅ 邮箱注册 → 邮箱登录
12. ✅ 微信注册 → 微信登录

---

## 下一步

1. ✅ 启动服务
2. ✅ 访问 Swagger UI
3. ✅ 测试注册接口
4. ✅ 测试登录接口
5. ✅ 验证数据库数据
6. ✅ 测试 Token 刷新
7. ✅ 测试用户登出

**准备好了吗？让我们开始测试吧！** 🚀

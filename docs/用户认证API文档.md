# 用户认证 API 文档

## 概述

DudaNexus 用户认证系统支持 **4种用户身份** × **3种注册方式** × **3种登录方式**，共计 12 种注册组合和 12 种登录组合。

### 用户身份

| 身份类型 | 代码 | 权限级别 | 说明 |
|---------|------|---------|------|
| 普通用户 | `normal` | 1 | 可以浏览商品、下单购买、发布社交动态、观看直播 |
| 商家用户 | `merchant` | 2 | 可以发布商品、管理店铺、进行直播带货 |
| 运营人员 | `operator` | 3 | 可以管理平台内容和用户、查看运营数据 |
| 管理员 | `admin` | 4 | 拥有所有权限、可以进行系统配置 |

### 注册方式

| 方式 | 说明 | 必填字段 |
|------|------|---------|
| 手机号注册 | 手机号 + 密码 + 验证码 | phone, password, phoneVerifyCode |
| 邮箱注册 | 邮箱 + 密码 + 验证码 | email, password, emailVerifyCode |
| 第三方注册 | 第三方账号绑定 | thirdPartyPlatform, thirdPartyOpenId |

### 登录方式

| 方式 | 说明 | 必填字段 |
|------|------|---------|
| 手机号密码 | 手机号 + 密码 | phone, password |
| 邮箱密码 | 邮箱 + 密码 | email, password |
| 第三方登录 | OAuth 授权 | thirdPartyPlatform, thirdPartyAuthCode |

---

## API 接口

### 基础路径

```
http://your-domain/api/auth
```

### 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": "2026-03-11T10:30:00"
}
```

---

## 1. 用户注册

### 接口信息

- **路径**: `/api/auth/register`
- **方法**: `POST`
- **Content-Type**: `application/json`

### 请求参数

#### 1.1 手机号注册（普通用户）

```json
{
  "userType": "normal",
  "registerType": "phone",
  "username": "johndoe",
  "password": "pass123456",
  "confirmPassword": "pass123456",
  "phone": "13800138000",
  "phoneVerifyCode": "123456",
  "realName": "张三",
  "inviteCode": "INVITE123",
  "clientIp": "192.168.1.1",
  "clientType": "web"
}
```

#### 1.2 手机号注册（商家）

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
  "clientIp": "192.168.1.1",
  "clientType": "web"
}
```

#### 1.3 邮箱注册（普通用户）

```json
{
  "userType": "normal",
  "registerType": "email",
  "username": "johndoe",
  "password": "pass123456",
  "confirmPassword": "pass123456",
  "email": "user@example.com",
  "emailVerifyCode": "123456",
  "clientIp": "192.168.1.1",
  "clientType": "web"
}
```

#### 1.4 第三方注册（微信）

```json
{
  "userType": "normal",
  "registerType": "third_party",
  "thirdPartyPlatform": "wechat",
  "thirdPartyOpenId": "oXXXXXXXXXXXXXXXX",
  "thirdPartyUnionId": "uXXXXXXXXXXXXXXXX",
  "thirdPartyNickname": "张三",
  "thirdPartyAvatar": "https://example.com/avatar.jpg",
  "clientIp": "192.168.1.1",
  "clientType": "mini_program"
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userType | String | 是 | 用户类型：normal/merchant/operator/admin |
| registerType | String | 是 | 注册方式：phone/email/third_party |
| username | String | 否 | 用户名（3-20个字符） |
| password | String | 条件 | 密码（6-20个字符，phone/email注册时必填） |
| confirmPassword | String | 条件 | 确认密码（password必填时必填） |
| phone | String | 条件 | 手机号（phone注册时必填） |
| phoneVerifyCode | String | 条件 | 手机验证码（phone注册时必填） |
| email | String | 条件 | 邮箱（email注册时必填） |
| emailVerifyCode | String | 条件 | 邮箱验证码（email注册时必填） |
| thirdPartyPlatform | String | 条件 | 第三方平台（third_party注册时必填） |
| thirdPartyOpenId | String | 条件 | 第三方OpenID（third_party注册时必填） |
| thirdPartyUnionId | String | 否 | 第三方UnionID |
| thirdPartyNickname | String | 否 | 第三方昵称 |
| thirdPartyAvatar | String | 否 | 第三方头像URL |
| realName | String | 否 | 真实姓名 |
| inviteCode | String | 否 | 邀请码 |
| clientIp | String | 否 | 客户端IP |
| clientType | String | 否 | 客户端类型：web/app/mini_program |

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 10001,
    "username": "johndoe",
    "userType": "normal",
    "userTypeName": "普通用户",
    "realName": "张三",
    "phone": "138****8000",
    "email": "j***@example.com",
    "avatar": "https://example.com/avatar.jpg",
    "status": "active",
    "statusDesc": "激活成功",
    "registerTime": "2026-03-11T10:30:00",
    "needLogin": true,
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "welcomeMessage": "欢迎加入都达云台！"
  },
  "timestamp": "2026-03-11T10:30:00"
}
```

---

## 2. 用户登录

### 接口信息

- **路径**: `/api/auth/login`
- **方法**: `POST`
- **Content-Type**: `application/json`

### 请求参数

#### 2.1 手机号密码登录

```json
{
  "loginType": "phone_password",
  "userType": "normal",
  "phone": "13800138000",
  "password": "pass123456",
  "clientIp": "192.168.1.1",
  "clientType": "web"
}
```

#### 2.2 邮箱密码登录

```json
{
  "loginType": "email_password",
  "userType": "normal",
  "email": "user@example.com",
  "password": "pass123456",
  "clientIp": "192.168.1.1",
  "clientType": "web"
}
```

#### 2.3 第三方登录（微信）

```json
{
  "loginType": "third_party",
  "userType": "normal",
  "thirdPartyPlatform": "wechat",
  "thirdPartyAuthCode": "auth_code_xxx",
  "thirdPartyOpenId": "oXXXXXXXXXXXXXXXX",
  "clientIp": "192.168.1.1",
  "clientType": "mini_program"
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| loginType | String | 是 | 登录方式：phone_password/email_password/third_party |
| userType | String | 否 | 用户类型（用于区分不同身份登录） |
| phone | String | 条件 | 手机号（phone_password登录时必填） |
| email | String | 条件 | 邮箱（email_password登录时必填） |
| password | String | 条件 | 密码（phone_password/email_password登录时必填） |
| thirdPartyPlatform | String | 条件 | 第三方平台（third_party登录时必填） |
| thirdPartyAuthCode | String | 条件 | 第三方授权码（third_party登录时必填） |
| thirdPartyOpenId | String | 条件 | 第三方OpenID（third_party登录时选填） |
| clientIp | String | 否 | 客户端IP |
| clientType | String | 否 | 客户端类型：web/app/mini_program |
| deviceId | String | 否 | 设备唯一标识 |

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "userId": 10001,
    "username": "johndoe",
    "userType": "normal",
    "userTypeName": "普通用户",
    "realName": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "phone": "138****8000",
    "email": "j***@example.com",
    "isFirstLogin": false,
    "needCompleteInfo": false,
    "status": "active",
    "statusDesc": "正常",
    "companyId": 1001,
    "department": "技术部",
    "position": "工程师",
    "loginTime": "2026-03-11T10:30:00"
  },
  "timestamp": "2026-03-11T10:30:00"
}
```

---

## 3. 用户登出

### 接口信息

- **路径**: `/api/auth/logout`
- **方法**: `POST`
- **Headers**: `Authorization: Bearer {accessToken}`

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| Authorization | String | Header | 是 | 访问令牌，格式：Bearer {token} |
| userId | Long | Query | 是 | 用户ID |

### 请求示例

```bash
POST /api/auth/logout?userId=10001
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": "2026-03-11T10:30:00"
}
```

---

## 4. 刷新Token

### 接口信息

- **路径**: `/api/auth/refresh`
- **方法**: `POST`

### 请求参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| refreshToken | String | Query | 是 | 刷新令牌 |

### 请求示例

```bash
POST /api/auth/refresh?refreshToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "timestamp": "2026-03-11T10:30:00"
}
```

---

## 5. 验证Token

### 接口信息

- **路径**: `/api/auth/validate`
- **方法**: `GET`
- **Headers**: `Authorization: Bearer {accessToken}`

### 请求示例

```bash
GET /api/auth/validate
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": true,
  "timestamp": "2026-03-11T10:30:00"
}
```

---

## 6. 获取当前用户信息

### 接口信息

- **路径**: `/api/auth/user/info`
- **方法**: `GET`
- **Headers**: `Authorization: Bearer {accessToken}`

### 请求示例

```bash
GET /api/auth/user/info
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 10001,
    "username": "johndoe",
    "userType": "normal",
    "userTypeName": "普通用户",
    "realName": "张三",
    "phone": "138****8000",
    "email": "j***@example.com",
    "avatar": "https://example.com/avatar.jpg",
    "status": "active",
    "statusDesc": "正常"
  },
  "timestamp": "2026-03-11T10:30:00"
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效或已过期） |
| 403 | 禁止访问（权限不足） |
| 409 | 冲突（如账号已存在） |
| 429 | 请求过于频繁（限流） |
| 500 | 服务器内部错误 |

### 错误响应示例

```json
{
  "code": 400,
  "message": "手机号格式不正确",
  "data": null,
  "timestamp": "2026-03-11T10:30:00"
}
```

---

## 业务场景示例

### 场景1：普通用户手机号注册并登录

1. **发送验证码**
   ```bash
   POST /api/sms/send
   {
     "phone": "13800138000",
     "type": "register"
   }
   ```

2. **注册账号**
   ```bash
   POST /api/auth/register
   {
     "userType": "normal",
     "registerType": "phone",
     "username": "newuser",
     "password": "pass123456",
     "confirmPassword": "pass123456",
     "phone": "13800138000",
     "phoneVerifyCode": "123456"
   }
   ```

3. **登录**
   ```bash
   POST /api/auth/login
   {
     "loginType": "phone_password",
     "phone": "13800138000",
     "password": "pass123456"
   }
   ```

### 场景2：商家用户通过微信登录

1. **获取微信授权码**
   - 前端调用微信OAuth2.0授权接口
   - 获取 auth_code

2. **登录**
   ```bash
   POST /api/auth/login
   {
     "loginType": "third_party",
     "userType": "merchant",
     "thirdPartyPlatform": "wechat",
     "thirdPartyAuthCode": "auth_code_from_wechat",
     "clientType": "mini_program"
   }
   ```

3. **如果是首次登录，需要完善信息**
   ```bash
   POST /api/user/complete-info
   Headers:
     Authorization: Bearer {accessToken}
   {
     "realName": "张三",
     "phone": "13800138000",
     "shopName": "测试店铺"
   }
   ```

### 场景3：运营人员邮箱登录

1. **登录**
   ```bash
   POST /api/auth/login
   {
     "loginType": "email_password",
     "userType": "operator",
     "email": "operator@duda.com",
     "password": "pass123456"
   }
   ```

2. **访问运营后台**
   ```bash
   GET /api/operator/dashboard
   Headers:
     Authorization: Bearer {accessToken}
   ```

---

## 安全建议

1. **使用HTTPS**：生产环境必须使用 HTTPS 协议
2. **Token存储**：AccessToken 应存储在内存中，RefreshToken 可存储在 localStorage
3. **密码加密**：前端使用 HTTPS 传输，后端使用 BCrypt 加密存储
4. **验证码**：验证码应有有效期（如5分钟）和次数限制（如每个手机号每天最多5次）
5. **限流**：登录接口应有频率限制，防止暴力破解
6. **日志记录**：记录所有登录、登出操作，用于审计和安全分析

---

## 测试

### 运行测试

```bash
cd duda-common/duda-common-security
mvn test
```

### 测试覆盖

- ✅ 4种用户身份 × 3种注册方式 = 12种注册组合
- ✅ 4种用户身份 × 3种登录方式 = 12种登录组合
- ✅ Token刷新和验证
- ✅ 异常场景处理
- ✅ 完整的注册-登录-登出流程

---

## 总结

DudaNexus 认证系统提供了灵活、安全、可扩展的用户认证解决方案：

- ✅ 支持 4 种用户身份，权限分级管理
- ✅ 支持 3 种注册方式和 3 种登录方式，共 12 种组合
- ✅ 基于 JWT 的双 Token 机制，安全可靠
- ✅ 支持第三方登录（微信、QQ、支付宝等）
- ✅ 完善的参数验证和异常处理
- ✅ 防重复提交、限流等安全机制
- ✅ 完整的测试用例，保证代码质量

可以直接用于生产环境！

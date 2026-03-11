# 多种登录方式实现文档

## 📋 目录
1. [功能概述](#功能概述)
2. [技术实现](#技术实现)
3. [使用方式](#使用方式)
4. [配置说明](#配置说明)
5. [测试验证](#测试验证)
6. [待完善功能](#待完善功能)

---

## 功能概述

### 已实现的登录方式

✅ **账号密码登录**
- 用户名+密码登录
- 支持注册和登录
- 密码BCrypt加密存储

✅ **手机验证码登录**
- 手机号+验证码登录
- 开发环境：验证码输出到日志
- 生产环境：可配置真实短信发送
- 支持手机号注册并自动登录

🔜 **微信扫码登录（预留接口）**
- 接口已定义
- 等待微信开放平台权限

---

## 技术实现

### 1. 架构设计

```
┌─────────────────────────────────────────────────────────┐
│                    前端（Flutter/Web）                   │
└────────────────────┬────────────────────────────────────┘
                     │ REST API
┌────────────────────▼────────────────────────────────────┐
│              duda-user-api (API层)                      │
│  - UserController                                       │
│  - 提供 HTTP 接口                                        │
└────────────────────┬────────────────────────────────────┘
                     │ Dubbo RPC
┌────────────────────▼────────────────────────────────────┐
│           duda-user-provider (Provider层)               │
│  - UserServiceImpl                                       │
│  - LoginServiceImpl                                      │
│  - SmsServiceImpl                                       │
└────────────────────┬────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         ▼                       ▼
┌──────────────┐         ┌──────────────┐
│    MySQL     │         │    Redis     │
│  (用户数据)  │         │  (验证码)     │
└──────────────┘         └──────────────┘
```

### 2. 核心类说明

#### DTO层

**登录请求DTO:**
- `PasswordLoginReqDTO` - 账号密码登录请求
- `SmsCodeLoginReqDTO` - 手机验证码登录请求
- `WechatQrLoginReqDTO` - 微信扫码登录请求

**注册请求DTO:**
- `PasswordRegisterReqDTO` - 账号密码注册请求
- `SmsCodeRegisterReqDTO` - 手机验证码注册请求

**返回DTO:**
- `SmsCheckDTO` - 验证码校验结果
- `UserDTO` - 用户信息

#### Service层

**ISmsService** - 短信验证码服务
```java
// 发送登录验证码
SmsSendResultEnum sendLoginCode(String phone);

// 校验登录验证码
SmsCheckDTO checkLoginCode(String phone, Integer code);
```

**LoginService** - 登录服务
```java
// 账号密码登录
Long loginByPassword(String username, String password);

// 手机验证码登录
Long loginBySmsCode(String phone, Integer code);

// 手机号注册并登录
Long registerAndLoginBySmsCode(String phone, Integer code, String nickname);
```

#### Enum层

**LoginTypeEnum** - 登录方式枚举
- PASSWORD - 账号密码登录
- SMS_CODE - 手机验证码登录
- WECHAT_QR - 微信扫码登录

**SmsSendResultEnum** - 短信发送结果枚举
- SEND_SUCCESS - 发送成功
- PARAM_ERROR - 参数错误
- SEND_FAIL - 发送失败

### 3. 关键技术点

#### 验证码生成和存储

```java
// 1. 生成4位随机验证码
int code = new Random().nextInt(1000, 9999);

// 2. 存储到Redis，60秒过期
String cacheKey = "sms:login:code:" + phone;
redisTemplate.opsForValue().set(cacheKey, code, 60, TimeUnit.SECONDS);

// 3. 校验后立即删除（防止重复使用）
redisTemplate.delete(cacheKey);
```

#### 密码加密

```java
// 使用BCrypt加密
BCrypt.hashpw(password);

// 校验密码
BCrypt.checkpw(password, hashedPassword);
```

#### 开发/生产环境切换

```yaml
# application-sms.yml
duda:
  sms:
    real-send: false  # false=开发环境，true=生产环境
```

---

## 使用方式

### 1. 账号密码登录

#### 注册接口

**请求:**
```http
POST /user/register/password
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456",
  "realName": "测试用户",
  "phone": "13800138000",
  "email": "test@example.com"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": 4920436132941824,
  "success": true
}
```

#### 登录接口

**请求:**
```http
POST /user/login/password
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 4920436132941824,
    "username": "testuser",
    "realName": "测试用户",
    "phone": "13800138000",
    "email": "test@example.com",
    "status": "active",
    "lastLoginTime": "2026-03-11T15:30:00"
  },
  "success": true
}
```

### 2. 手机验证码登录

#### 发送验证码

**请求:**
```http
POST /user/send-login-code?phone=13800138000
```

**响应（开发环境）:**
```json
{
  "code": 200,
  "message": "success",
  "data": true,
  "success": true
}
```

**开发环境日志输出:**
```
========================================
【短信验证码】手机号：13800138000，验证码：1234
========================================
```

**说明:**
- 开发环境：验证码输出到控制台日志
- 生产环境：调用真实短信服务商API发送短信

#### 注册并登录

**请求:**
```http
POST /user/register/sms-code
Content-Type: application/json

{
  "phone": "13800138000",
  "code": 1234,
  "nickname": "新用户"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 4920436132941825,
    "phone": "13800138000",
    "username": "13800138000",
    "realName": "新用户",
    "status": "active"
  },
  "success": true
}
```

#### 手机号登录

**请求:**
```http
POST /user/login/sms-code
Content-Type: application/json

{
  "phone": "13800138000",
  "code": 1234
}
```

**说明:**
- 如果手机号已注册，直接登录
- 如果手机号未注册，返回错误提示

### 3. 微信扫码登录（预留）

**请求:**
```http
POST /user/login/wechat-qr
Content-Type: application/json

{
  "code": "wx_code_123456",
  "state": "state_abc"
}
```

**响应:**
```json
{
  "code": 500,
  "message": "微信扫码登录功能尚未实现，请等待后续版本",
  "success": false
}
```

---

## 配置说明

### 1. 短信配置

**文件位置:** `duda-user-provider/src/main/resources/application-sms.yml`

```yaml
duda:
  sms:
    # 是否开启真实发送短信
    # false: 开发环境，验证码输出到日志
    # true: 生产环境，调用真实短信API
    real-send: false

    # 验证码有效期（秒）
    code-expire-seconds: 60

    # 验证码长度
    code-length: 4

    # 短信服务器配置（生产环境）
    server-ip: app.cloopen.com
    server-port: 8883
    account-id: your_account_id
    account-token: your_account_token
    app-id: your_app_id
```

### 2. 启动配置

**方式一：包含SMS配置**
```yaml
spring:
  profiles:
    active: dev,sms
```

**方式二：直接在application.yml中配置**
```yaml
spring:
  profiles:
    active: dev

duda:
  sms:
    real-send: false
    code-expire-seconds: 60
    code-length: 4
```

### 3. 生产环境配置

接入真实短信服务商时，需要修改：

1. **配置短信服务商信息**
   - 容联云通讯
   - 阿里云短信
   - 腾讯云短信

2. **修改SmsServiceImpl.java**
   ```java
   private boolean sendRealSms(String phone, int code) {
       // TODO: 接入真实的短信服务商API
       // 参考老项目实现：/Volumes/DudaDate/Live_app/Backend/qiyu-live-app/qiyu-live-msg-provider
   }
   ```

---

## 测试验证

### 1. 单元测试示例

#### 测试账号密码登录

```java
@Test
public void testLoginByPassword() {
    // 1. 注册
    PasswordRegisterReqDTO registerReq = new PasswordRegisterReqDTO();
    registerReq.setUsername("testuser001");
    registerReq.setPassword("123456");
    registerReq.setRealName("测试用户");

    Long userId = userRpc.registerByPassword(registerReq);
    assertNotNull(userId);

    // 2. 登录
    PasswordLoginReqDTO loginReq = new PasswordLoginReqDTO();
    loginReq.setUsername("testuser001");
    loginReq.setPassword("123456");

    UserDTO userDTO = userRpc.loginByPassword(loginReq);
    assertEquals(userId, userDTO.getId());
}
```

#### 测试手机验证码登录

```java
@Test
public void testLoginBySmsCode() {
    String phone = "13900139000";

    // 1. 发送验证码
    Boolean sendResult = userRpc.sendLoginCode(phone);
    assertTrue(sendResult);

    // 2. 从Redis获取验证码（仅测试用）
    String cacheKey = "sms:login:code:" + phone;
    Integer code = (Integer) redisTemplate.opsForValue().get(cacheKey);
    assertNotNull(code);

    // 3. 登录
    SmsCodeLoginReqDTO loginReq = new SmsCodeLoginReqDTO();
    loginReq.setPhone(phone);
    loginReq.setCode(code);

    UserDTO userDTO = userRpc.loginBySmsCode(loginReq);
    assertNotNull(userDTO);
}
```

### 2. API测试（Swagger）

访问 Swagger UI: `http://localhost:8083/swagger-ui/index.html`

**测试步骤:**

1. **账号密码注册**
   - 接口：`POST /user/register/password`
   - 输入用户名、密码
   - 执行，查看返回的用户ID

2. **账号密码登录**
   - 接口：`POST /user/login/password`
   - 输入用户名、密码
   - 执行，查看返回的用户信息

3. **发送验证码**
   - 接口：`POST /user/send-login-code`
   - 输入手机号
   - 执行，查看控制台日志获取验证码

4. **手机验证码登录**
   - 接口：`POST /user/login/sms-code`
   - 输入手机号和验证码
   - 执行，查看返回的用户信息

### 3. Docker部署测试

```bash
# 1. 重新构建
cd /Volumes/DudaDate/DudaNexus
mvn clean package -DskipTests
docker-compose build

# 2. 重启服务
docker-compose down
docker-compose up -d

# 3. 查看日志
docker-compose logs -f duda-user-provider

# 4. 测试接口
curl "http://localhost:8083/user/send-login-code?phone=13900139000"
```

---

## 待完善功能

### 1. 微信扫码登录

**需要接入的接口:**
- [ ] 微信开放平台权限申请
- [ ] 获取微信access_token
- [ ] 获取微信用户信息
- [ ] 绑定微信openid到用户账号

**实现位置:**
```java
// UserServiceImpl.java
@Override
public UserDTO loginByWechatQr(WechatQrLoginReqDTO loginReq) {
    // TODO: 实现微信扫码登录
}
```

**参考老项目:**
- 路径：`/Volumes/DudaDate/Live_app/Backend/qiyu-live-app`
- 关键文件：
  - 微信登录Controller
  - 微信SDK集成代码

### 2. 短信服务商接入

**推荐服务商:**
- 阿里云短信：https://help.aliyun.com/product/44282
- 腾讯云短信：https://cloud.tencent.com/product/sms
- 容联云通讯：https://www.yuntongxun.com/

**接入步骤:**
1. 注册账号并获取API密钥
2. 配置短信模板
3. 实现`sendRealSms()`方法
4. 测试发送

**参考实现:**
- 老项目路径：`/Volumes/DudaDate/Live_app/Backend/qiyu-live-app/qiyu-live-msg-provider`
- 文件：`SmsServiceImpl.java`的`sendSmsToCCP()`方法

### 3. Token管理

**需要实现的功能:**
- [ ] JWT Token生成
- [ ] Token刷新机制
- [ ] Token黑名单（登出）
- [ ] 多端登录控制

**实现建议:**
```java
// 创建TokenService
@Service
public class TokenService {
    // 生成Token
    public String createToken(Long userId);

    // 验证Token
    public boolean validateToken(String token);

    // 刷新Token
    public String refreshToken(String token);

    // 销毁Token（登出）
    public void destroyToken(String token);
}
```

### 4. 登录日志

**建议记录的信息:**
- 登录时间
- 登录IP
- 登录方式
- 登录设备
- 登录状态

**表设计:**
```sql
CREATE TABLE `user_login_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `login_type` varchar(20) NOT NULL COMMENT '登录方式',
  `login_ip` varchar(50) COMMENT '登录IP',
  `login_device` varchar(100) COMMENT '登录设备',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `status` varchar(20) NOT NULL COMMENT '登录状态',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_login_time` (`login_time`)
) COMMENT='用户登录日志表';
```

---

## 常见问题

### 1. 验证码收不到？

**开发环境:**
- 检查控制台日志输出
- 查看Redis是否有验证码缓存

**生产环境:**
- 检查短信服务商配置
- 检查短信模板是否审核通过
- 检查账户余额

### 2. 密码错误？

- 确认密码加密方式一致（BCrypt）
- 检查数据库中密码字段是否正确存储
- 确认前后端密码传输没有丢失

### 3. 手机号已注册？

- 手机号注册时会检查是否已存在
- 如果手机号已存在，会提示错误
- 建议使用"手机号+验证码"直接登录

---

## 文件清单

### 新增文件

**DTO层:**
- `PasswordLoginReqDTO.java` - 账号密码登录请求
- `SmsCodeLoginReqDTO.java` - 手机验证码登录请求
- `WechatQrLoginReqDTO.java` - 微信扫码登录请求
- `PasswordRegisterReqDTO.java` - 账号密码注册请求
- `SmsCodeRegisterReqDTO.java` - 手机验证码注册请求
- `SmsCheckDTO.java` - 验证码校验结果

**Enum层:**
- `LoginTypeEnum.java` - 登录方式枚举
- `SmsSendResultEnum.java` - 短信发送结果枚举

**Service层:**
- `ISmsService.java` - 短信服务接口
- `SmsServiceImpl.java` - 短信服务实现
- `LoginService.java` - 登录服务接口
- `LoginServiceImpl.java` - 登录服务实现

**Config层:**
- `SmsConfig.java` - 短信配置类

**配置文件:**
- `application-sms.yml` - 短信配置

### 修改文件

**接口层:**
- `IUserRpc.java` - 添加多种登录方法
- `UserRpcImpl.java` - 实现多种登录方法

**服务层:**
- `UserService.java` - 添加登录方法声明
- `UserServiceImpl.java` - 实现登录方法

**控制器层:**
- `UserController.java` - 添加登录接口

---

## 总结

✅ **已完成:**
- 账号密码登录
- 手机验证码登录（开发环境）
- 完整的接口定义
- Swagger文档支持

🔜 **待完成:**
- 微信扫码登录
- 短信服务商接入
- Token管理
- 登录日志

**开发环境已可直接使用，生产环境需要配置短信服务商。**

---

**文档版本:** 1.0
**更新时间:** 2026-03-11
**作者:** DudaNexus Team

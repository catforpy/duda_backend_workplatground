# API接口设计规范文档

## 1. RESTful API设计原则

### 1.1 资源命名规范

**URL设计原则：**
- 使用名词复数表示资源
- 使用小写字母
- 使用连字符（-）分隔单词
- 避免使用动词

**示例：**
```
# 正确示例
GET    /api/v1/users              # 获取用户列表
GET    /api/v1/users/{id}         # 获取指定用户
POST   /api/v1/users              # 创建用户
PUT    /api/v1/users/{id}         # 更新用户
DELETE /api/v1/users/{id}         # 删除用户

# 错误示例
GET    /api/v1/getUsers           # 使用了动词
GET    /api/v1/User/{id}          # 使用了大写
POST   /api/v1/createUser         # 使用了动词
```

### 1.2 HTTP方法使用

| 方法 | 用途 | 是否幂等 | 示例 |
|------|------|---------|------|
| GET | 查询资源 | 是 | GET /api/v1/users/{id} |
| POST | 创建资源 | 否 | POST /api/v1/users |
| PUT | 全量更新资源 | 是 | PUT /api/v1/users/{id} |
| PATCH | 部分更新资源 | 否 | PATCH /api/v1/users/{id} |
| DELETE | 删除资源 | 是 | DELETE /api/v1/users/{id} |

---

## 2. URL设计规范

### 2.1 版本控制

**通过URL进行版本控制：**
```
/api/v1/users
/api/v2/users
```

**通过请求头进行版本控制（推荐）：**
```
GET /api/users
Header: Accept: application/vnd.duda.v1+json
```

### 2.2 分页参数

```
GET /api/v1/users?page=1&size=20&sort=createTime,desc
```

**参数说明：**
- `page`：页码（从1开始）
- `size`：每页数量（默认20，最大100）
- `sort`：排序字段，格式：字段名,排序方向（asc/desc）

### 2.3 过滤参数

```
GET /api/v1/users?status=1&createTimeStart=2024-01-01&createTimeEnd=2024-12-31
```

### 2.4 字段选择

```
GET /api/v1/users?fields=id,userName,userEmail
```

### 2.5 嵌套资源

```
# 获取用户的订单
GET /api/v1/users/{userId}/orders

# 获取订单的商品
GET /api/v1/orders/{orderId}/items
```

---

## 3. 请求规范

### 3.1 请求头

**通用请求头：**
```
Content-Type: application/json
Accept: application/json
Authorization: Bearer {token}
X-Request-ID: {uuid}
X-Client-Type: web/ios/android/miniprogram
X-Client-Version: 1.0.0
```

### 3.2 请求体格式

```json
{
  "userName": "testuser",
  "password": "123456",
  "userEmail": "test@duda.com",
  "userPhone": "13800138000"
}
```

**请求体规范：**
- 使用驼峰命名
- 时间格式：yyyy-MM-dd HH:mm:ss
- 金额单位：分
- 布尔值：true/false

---

## 4. 响应规范

### 4.1 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1709769600000,
  "traceId": "trace-id-123",
  "path": "/api/v1/users/1"
}
```

**字段说明：**
- `code`：响应码
- `message`：响应消息
- `data`：响应数据
- `timestamp`：响应时间戳
- `traceId`：链路追踪ID
- `path`：请求路径

### 4.2 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 20,
    "pages": 5
  }
}
```

### 4.3 列表响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userName": "user1"
    },
    {
      "id": 2,
      "userName": "user2"
    }
  ]
}
```

---

## 5. 错误码规范

### 5.1 错误码设计

**错误码格式：`{模块码}{错误类型}{具体错误}`**

| 错误码 | 说明 | 示例 |
|--------|------|------|
| 200 | 成功 | 操作成功 |
| 400 | 请求参数错误 | 参数校验失败 |
| 401 | 未认证 | 未登录或Token过期 |
| 403 | 无权限 | 无操作权限 |
| 404 | 资源不存在 | 用户不存在 |
| 409 | 资源冲突 | 用户已存在 |
| 429 | 请求过于频繁 | 触发限流 |
| 500 | 服务器内部错误 | 系统异常 |
| 503 | 服务不可用 | 服务降级 |

### 5.2 业务错误码

```
# 用户模块错误码（10xxx）
10001  用户不存在
10002  用户已存在
10003  密码错误
10004  用户已被禁用

# 订单模块错误码（20xxx）
20001  订单不存在
20002  订单状态异常
20003  订单已支付
20004  库存不足

# 商品模块错误码（30xxx）
30001  商品不存在
30002  商品已下架
30003  库存不足

# 支付模块错误码（40xxx）
40001  支付失败
40002  支付金额不一致
40003  支付已超时
```

### 5.3 错误响应格式

```json
{
  "code": 10001,
  "message": "用户不存在",
  "data": null,
  "timestamp": 1709769600000,
  "traceId": "trace-id-123",
  "path": "/api/v1/users/999"
}
```

**带详细错误的响应：**
```json
{
  "code": 400,
  "message": "参数校验失败",
  "data": {
    "errors": [
      {
        "field": "userName",
        "message": "用户名不能为空"
      },
      {
        "field": "password",
        "message": "密码长度不能少于6位"
      }
    ]
  }
}
```

---

## 6. 接口安全

### 6.1 认证方式

**JWT Token认证：**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 6.2 签名验证

**请求签名流程：**
1. 将所有参数按字母顺序排序
2. 拼接成字符串：key1=value1&key2=value2
3. 加上密钥：key1=value1&key2=value2&secret=your-secret
4. MD5加密生成签名
5. 将签名放在请求头：X-Signature

**示例：**
```
X-Signature: md5(param1=value1&param2=value2&secret=your-secret)
X-Timestamp: 1709769600
X-Nonce: random-string
```

---

## 7. 接口限流

### 7.1 限流策略

**IP限流：**
- 同一IP每分钟最多100次请求

**用户限流：**
- 同一用户每分钟最多200次请求

**接口限流：**
- 普通接口：每秒100次
- 核心接口：每秒1000次

### 7.2 限流响应

```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试",
  "data": {
    "limit": 100,
    "remaining": 0,
    "reset": 1709769660
  }
}
```

---

## 8. 常用接口示例

### 8.1 用户模块

**用户注册**
```
POST /api/v1/users/register
Content-Type: application/json

{
  "userName": "testuser",
  "password": "123456",
  "userEmail": "test@duda.com",
  "userPhone": "13800138000",
  "verifyCode": "123456"
}

Response:
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 123,
    "userName": "testuser",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**用户登录**
```
POST /api/v1/auth/login
Content-Type: application/json

{
  "userName": "testuser",
  "password": "123456"
}

Response:
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 123,
    "userName": "testuser",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireIn": 3600
  }
}
```

**获取用户信息**
```
GET /api/v1/users/{id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "userName": "testuser",
    "userNick": "测试用户",
    "userAvatar": "https://cdn.duda.com/avatar/123.jpg",
    "userEmail": "test@duda.com",
    "userPhone": "138****8000",
    "userType": 1,
    "createTime": "2024-01-01 12:00:00"
  }
}
```

### 8.2 订单模块

**创建订单**
```
POST /api/v1/orders
Authorization: Bearer {token}
Content-Type: application/json

{
  "receiverName": "张三",
  "receiverPhone": "13800138000",
  "receiverAddress": "北京市朝阳区XXX",
  "items": [
    {
      "productId": 1,
      "skuId": 101,
      "count": 2
    }
  ],
  "remark": "尽快发货"
}

Response:
{
  "code": 200,
  "message": "下单成功",
  "data": {
    "orderId": 456,
    "orderNo": "2024030712345678",
    "totalAmount": 29900,
    "payAmount": 29900
  }
}
```

**订单列表**
```
GET /api/v1/orders?page=1&size=20&status=1
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "orderId": 456,
        "orderNo": "2024030712345678",
        "totalAmount": 29900,
        "orderStatus": 1,
        "createTime": "2024-03-07 12:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 20,
    "pages": 3
  }
}
```

### 8.3 商品模块

**商品列表**
```
GET /api/v1/products?categoryId=1&page=1&size=20

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "productId": 1,
        "productName": "商品名称",
        "mainImage": "https://cdn.duda.com/product/1.jpg",
        "price": 19900,
        "sales": 1000,
        "stock": 500
      }
    ],
    "total": 100,
    "page": 1,
    "size": 20,
    "pages": 5
  }
}
```

**商品详情**
```
GET /api/v1/products/{id}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "productId": 1,
    "productName": "商品名称",
    "productTitle": "商品标题",
    "mainImage": "https://cdn.duda.com/product/1.jpg",
    "images": [
      "https://cdn.duda.com/product/1.jpg",
      "https://cdn.duda.com/product/2.jpg"
    ],
    "price": 19900,
    "originalPrice": 29900,
    "stock": 500,
    "sales": 1000,
    "detail": "<p>商品详情HTML</p>",
    "skus": [
      {
        "skuId": 101,
        "skuName": "规格1",
        "price": 19900,
        "stock": 200
      }
    ]
  }
}
```

### 8.4 支付模块

**创建支付**
```
POST /api/v1/payments
Authorization: Bearer {token}
Content-Type: application/json

{
  "orderId": 456,
  "payType": 1
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "paymentId": 789,
    "payParams": {
      // 第三方支付参数
    }
  }
}
```

---

## 9. WebSocket接口

### 9.1 连接

```
WS /ws/im/connect?token={token}
```

### 9.2 消息格式

**客户端→服务端：**
```json
{
  "type": "message",
  "data": {
    "toUserId": 123,
    "content": "你好"
  }
}
```

**服务端→客户端：**
```json
{
  "type": "message",
  "data": {
    "messageId": 1,
    "fromUserId": 456,
    "fromUserName": "张三",
    "content": "你好",
    "sendTime": "2024-03-07 12:00:00"
  }
}
```

---

## 10. 接口文档

### 10.1 文档工具

推荐使用Swagger/OpenAPI生成接口文档。

### 10.2 注解示例

```java
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息")
    @Parameter(name = "id", description = "用户ID", required = true)
    public Result<UserVO> getUser(@PathVariable Long id) {
        // ...
    }

    @PostMapping
    @Operation(summary = "创建用户")
    @RequestBody(description = "用户信息", required = true)
    public Result<Long> createUser(@RequestBody @Valid CreateUserCommand cmd) {
        // ...
    }
}
```

---

## 11. 接口测试

### 11.1 单元测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
```

### 11.2 接口测试工具

- Postman
- Apifox
- JMeter
- curl

---

## 12. 最佳实践

1. **幂等性设计**
   - POST创建操作不幂等
   - PUT/DELETE操作幂等
   - 使用幂等键保证重复请求安全

2. **批量操作**
   - 提供批量接口提高性能
   - 限制批量操作数量（最多100条）

3. **异步处理**
   - 耗时操作使用异步处理
   - 返回任务ID，提供查询接口

4. **数据脱敏**
   - 敏感数据脱敏返回
   - 手机号、身份证等

5. **接口版本管理**
   - 新版本保持兼容
   - 废弃接口提前通知

6. **监控告警**
   - 监控接口调用量
   - 监控响应时间
   - 监控错误率

7. **文档维护**
   - 及时更新接口文档
   - 提供示例代码
   - 标注废弃接口

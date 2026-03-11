# Redis 和 MQ 工具类使用指南

## 📦 新增的公共模块

### Redis 模块

**模块：** `duda-common-redis`

#### 1. RedisKeyBuilder - Redis Key 构建器

**功能：** 统一管理 Redis Key 的命名规范

**使用方法：**
```java
// 创建自己的 Key 构建器（继承 RedisKeyBuilder）
@Configuration
public class UserRedisKeyBuilder extends RedisKeyBuilder {
    private static final String USER = "user";

    public String buildUserInfoKey(Long userId) {
        // 生成 key: duda:user:info:123456
        return getPrefix() + getSplitItem() + USER + getSplitItem() + "info" + getSplitItem() + userId;
    }
}

// 使用
@Autowired
private UserRedisKeyBuilder redisKeyBuilder;

String key = redisKeyBuilder.buildUserInfoKey(userId);
redisTemplate.opsForValue().set(key, userDTO, 3600);
```

**Key 格式：**
```
duda:业务模块:功能:参数
示例: duda:user:info:123456
```

#### 2. RedisUtils - Redis 工具类

**功能：** 封装常用的 Redis 操作

**使用方法：**
```java
@Autowired
private RedisUtils redisUtils;

// 1. 字符串操作
redisUtils.set("user:1", userDTO);
redisUtils.set("user:1", userDTO, 3600);  // 1小时后过期
UserDTO user = redisUtils.get("user:1", UserDTO.class);
redisUtils.delete("user:1");
Boolean exists = redisUtils.hasKey("user:1");

// 2. Hash 操作
redisUtils.hSet("user:map", "name", "test");
Object name = redisUtils.hGet("user:map", "name");
Map<Object, Object> map = redisUtils.hGetAll("user:map");

// 3. Set 操作
redisUtils.sAdd("user:set", 1L, 2L, 3L);
Set<Object> members = redisUtils.sMembers("user:set");
Boolean isMember = redisUtils.sIsMember("user:set", 1L);

// 4. ZSet 操作（排行榜）
redisUtils.zAdd("ranking:users", userId, 100.0);
Set<Object> top10 = redisUtils.zRange("ranking:users", 0, 9);
redisUtils.zRemove("ranking:users", userId);
```

---

### MQ 模块

**模块：** `duda-common-rocketmq`

#### 1. MqTopicConstants - Topic 命名常量

**功能：** 统一管理所有 Topic 名称

**使用方法：**
```java
// 直接使用常量
rocketMQTemplate.syncSend(
    MqTopicConstants.USER_REGISTER,  // "UserRegister"
    message,
    "user-register-group"
);
```

**已定义的 Topic：**
- `USER_CACHE_ASYNC_DELETE` - 用户缓存异步删除
- `USER_REGISTER` - 用户注册
- `USER_LOGIN` - 用户登录
- `USER_LOGOUT` - 用户注销
- `TOKEN_REFRESH` - Token刷新
- `ORDER_CREATE` - 订单创建
- `ORDER_PAID` - 订单支付
- 等等...

#### 2. MqConsumerGroupConstants - 消费者组命名常量

**功能：** 统一管理所有消费者组名称

**使用方法：**
```java
@RocketMQMessageListener(
    topic = MqTopicConstants.USER_REGISTER,
    consumerGroup = MqConsumerGroupConstants.USER_REGISTER_GROUP
)
public class UserRegisterListener implements RocketMQListener<UserRegisterMsg> {
    @Override
    public void onMessage(UserRegisterMsg message) {
        // 处理消息
    }
}
```

#### 3. RocketMQUtils - MQ 工具类

**功能：** 封装常用的 MQ 操作

**使用方法：**
```java
@Autowired
private RocketMQUtils rocketMQUtils;

// 1. 同步发送消息
rocketMQUtils.syncSend(
    MqTopicConstants.USER_REGISTER,
    userDTO
);

// 2. 异步发送消息
rocketMQUtils.asyncSend(
    MqTopicConstants.USER_REGISTER,
    userDTO
);

// 3. 延时消息（30分钟后）
rocketMQUtils.syncSendDelay(
    MqTopicConstants.ORDER_TIMEOUT,
    orderId,
    16  // 延时等级16 = 30分钟
);

// 4. OneWay 消息（不关心结果）
rocketMQUtils.sendOneWay(
    MqTopicConstants.USER_LOGOUT,
    userId
);
```

#### 4. BaseMqMsg - 消息体基类

**功能：** 所有 MQ 消息的父类

**使用方法：**
```java
@Data
public class UserRegisterMsg extends BaseMqMsg {
    private Long userId;
    private String username;
    private String nickname;
}

// 发送消息
UserRegisterMsg msg = new UserRegisterMsg();
msg.setUserId(1L);
msg.setUsername("test");
msg.setNickname("测试用户");
rocketMQUtils.syncSend(MqTopicConstants.USER_REGISTER, msg);
```

---

## 🎯 实际使用示例

### 示例 1：用户缓存管理

```java
@Service
public class UserService {

    @Autowired
    private UserRedisKeyBuilder redisKeyBuilder;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 获取用户信息（带缓存）
     */
    public UserDTO getUserById(Long userId) {
        // 1. 构建缓存key
        String cacheKey = redisKeyBuilder.buildUserInfoKey(userId);

        // 2. 尝试从缓存获取
        UserDTO cachedUser = redisUtils.get(cacheKey, UserDTO.class);
        if (cachedUser != null) {
            return cachedUser;
        }

        // 3. 从数据库查询
        UserPO userPO = userMapper.selectById(userId);
        if (userPO == null) {
            throw new BizException("用户不存在");
        }

        // 4. 转换为DTO
        UserDTO userDTO = BeanCopyUtils.copy(userPO, UserDTO.class);

        // 5. 存入缓存（1小时）
        redisUtils.set(cacheKey, userDTO, 3600);

        return userDTO;
    }

    /**
     * 更新用户信息（清除缓存）
     */
    public void updateUser(UserDTO userDTO) {
        // 1. 更新数据库
        UserPO userPO = userMapper.selectById(userDTO.getId());
        BeanCopyUtils.update(userDTO, userPO);
        userMapper.updateById(userPO);

        // 2. 清除缓存
        String cacheKey = redisKeyBuilder.buildUserInfoKey(userDTO.getId());
        redisUtils.delete(cacheKey);

        // 3. 发送MQ消息，异步删除其他服务的缓存
        UserCacheMsg msg = new UserCacheMsg();
        msg.setUserId(userDTO.getId());
        rocketMQUtils.asyncSend(
            MqTopicConstants.USER_CACHE_ASYNC_DELETE,
            msg
        );
    }
}
```

### 示例 2：用户注册

```java
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RocketMQUtils rocketMQUtils;

    /**
     * 用户注册
     */
    public void register(UserDTO userDTO) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getUsername, userDTO.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BizException("用户名已存在");
        }

        // 2. 保存用户
        UserPO userPO = BeanCopyUtils.copy(userDTO, UserPO.class);
        userPO.setId(IdGenerator.nextId());
        userMapper.insert(userPO);

        // 3. 发送注册消息（用于发送欢迎邮件、短信等）
        UserRegisterMsg msg = new UserRegisterMsg();
        msg.setUserId(userPO.getId());
        msg.setUsername(userPO.getUsername());
        msg.setNickname(userPO.getNickname());

        rocketMQUtils.asyncSend(
            MqTopicConstants.USER_REGISTER,
            msg
        );
    }
}

/**
 * 消息监听器（在通知服务中）
 */
@Component
public class UserRegisterListener implements RocketMQListener<UserRegisterMsg> {

    @Autowired
 private NotificationService notificationService;

    @Override
    public void onMessage(UserRegisterMsg message) {
        // 发送欢迎邮件
        notificationService.sendWelcomeEmail(message.getUsername());

        // 发送欢迎短信
        notificationService.sendWelcomeSms(message.getUsername());
    }
}
```

### 示例 3：订单超时取消

```java
@Service
public class OrderService {

    @Autowired
    private RocketMQUtils rocketMQUtils;

    /**
     * 创建订单
     */
    public void createOrder(OrderDTO orderDTO) {
        // 1. 创建订单
        OrderPO orderPO = BeanCopyUtils.copy(orderDTO, OrderPO.class);
        orderPO.setId(IdGenerator.nextId());
        orderMapper.insert(orderPO);

        // 2. 发送延时消息（30分钟后自动取消）
        rocketMQUtils.syncSendDelay(
            MqTopicConstants.ORDER_TIMEOUT,
            orderPO.getId(),
            16  // 延时30分钟
        );
    }
}

/**
 * 订单超时监听器
 */
@Component
public class OrderTimeoutListener implements RocketMQ<Long> {

    @Autowired
    private OrderService orderService;

    @Override
    public void onMessage(Long orderId) {
        // 自动取消订单
        orderService.cancelOrder(orderId);
    }
}
```

---

## 📋 Topic 命名规范

### 命名规则

```
{业务模块}{具体功能}
```

### 示例

| 业务 | Topic | 说明 |
|------|-------|------|
| 用户服务 | `UserRegister` | 用户注册 |
| 用户服务 | `UserLogin` | 用户登录 |
| 用户服务 | `UserCacheAsyncDelete` | 用户缓存删除 |
| 认证服务 | `TokenRefresh` | Token刷新 |
| 认证服务 | `TokenInvalidate` | Token失效 |
| 订单服务 | `OrderCreate` | 订单创建 |
| 订单服务 | `OrderPaid` | 订单支付 |
| 订单服务 | `OrderTimeout` | 订单超时 |
| 内容服务 | `ContentPublish` | 内容发布 |
| 内容服务 | `ContentAudit` | 内容审核 |
| 通知服务 | `SystemNotification` | 系统通知 |

---

## 📋 Consumer Group 命名规范

### 命名规则

```
{业务模块}-{功能}-group
```

### 示例

| 业务 | Consumer Group | 说明 |
|------|---------------|------|
| 用户服务 | `user-register-group` | 用户注册 |
| 用户服务 | `user-cache-delete-group` | 缓存删除 |
| 认证服务 | `token-refresh-group` | Token刷新 |
| 订单服务 | `order-create-group` | 订单创建 |
| 订单服务 | `order-paid-group` | 订单支付 |

---

## ✅ 优势总结

1. **统一命名** - 所有 Key、Topic、Group 都有统一规范
2. **避免冲突** - 通过前缀和分隔符避免 Key 冲突
3. **便于管理** - 集中管理所有 Topic 和 Group 名称
4. **简化使用** - 工具类封装常用操作，使用简单

---

## 📝 注意事项

1. **Redis Key 命名**
   - 使用 `RedisKeyBuilder` 构建所有 Key
   - 格式：`{应用名}:{模块}:{功能}:{参数}`
   - 示例：`duda:user:info:123456`

2. **MQ Topic 命名**
   - 使用 `MqTopicConstants` 定义所有 Topic
   - 格式：`{业务模块}{具体功能}`（大驼峰）
   - 示例：`UserRegister`

3. **消息体**
   - 继承 `BaseMqMsg` 类
   - 自动生成 `timestamp` 和 `messageId`
   - 自动包含 `source`、`target`、`payload`

---

**现在 Redis 和 MQ 的工具类都已经准备好了，直接注入使用即可！** 🎉

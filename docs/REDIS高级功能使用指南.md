# Redis 高级功能使用指南

本文档详细说明 DudaNexus 项目中 Redis 高级功能的使用方法。

## 目录

1. [缓存策略](#缓存策略)
2. [缓存防护](#缓存防护)
3. [分布式锁](#分布式锁)
4. [接口幂等性](#接口幂等性)
5. [限流功能](#限流功能)
6. [测试验证](#测试验证)

---

## 缓存策略

### 1. Cache Aside（旁路缓存）

**适用场景**：读多写少的绝大多数业务

**读流程**：查缓存 → 缓存未命中则查 DB → 将数据写入缓存
**写流程**：更新 DB → 删除缓存

#### 使用示例

```java
@Autowired
private CacheAside cacheAside;

// 读操作
public UserDTO getUserById(Long userId) {
    String cacheKey = "user:" + userId;
    return cacheAside.get(cacheKey, UserDTO.class, key -> {
        // 缓存未命中时查询数据库
        return userMapper.selectById(userId);
    }, 3600); // 过期时间1小时
}

// 写操作
public boolean updateUser(UserDTO userDTO) {
    String cacheKey = "user:" + userDTO.getId();
    return cacheAside.update(cacheKey, key -> {
        // 更新数据库
        return userMapper.updateById(userDTO) > 0;
    });
}

// 删除操作
public boolean deleteUser(Long userId) {
    String cacheKey = "user:" + userId;
    return cacheAside.delete(cacheKey, key -> {
        // 删除数据库记录
        return userMapper.deleteById(userId) > 0;
    });
}
```

---

### 2. 延迟双删（Double Delete）

**适用场景**：写操作频繁，对一致性要求较高的业务

**执行流程**：
1. 删除缓存
2. 更新数据库
3. 休眠一段时间（如 500ms）
4. 再次删除缓存

#### 使用示例

```java
@Autowired
private DoubleDeleteCache doubleDeleteCache;

// 同步版本
public boolean updateProduct(Product product) {
    String cacheKey = "product:" + product.getId();
    return doubleDeleteCache.updateSync(cacheKey, key -> {
        return productMapper.updateById(product) > 0;
    }, 500); // 延迟500ms
}

// 异步版本（推荐）
public boolean updateProductAsync(Product product) {
    String cacheKey = "product:" + product.getId();
    doubleDeleteCache.updateAsync(cacheKey, key -> {
        return productMapper.updateById(product) > 0;
    }, 500); // 第二次删除异步执行，不阻塞主流程
    return true;
}
```

---

## 缓存防护

### 1. 防止缓存穿透

**问题**：查询不存在的数据，导致每次都查数据库

**解决方案**：缓存空值 + 布隆过滤器

#### 使用示例

```java
@Autowired
private CacheProtection cacheProtection;

public UserDTO getUserById(Long userId) {
    String cacheKey = "user:" + userId;
    return cacheProtection.getPreventPenetration(cacheKey, UserDTO.class, key -> {
        // 查询数据库
        return userMapper.selectById(userId);
    }, 3600);
}
```

### 2. 防止缓存击穿

**问题**：热点 key 过期，大量请求同时穿透到数据库

**解决方案**：互斥锁（分布式锁）

#### 使用示例

```java
public UserDTO getHotUser(Long userId) {
    String cacheKey = "hot:user:" + userId;
    return cacheProtection.getPreventBreakdown(cacheKey, UserDTO.class, key -> {
        // 查询数据库
        return userMapper.selectById(userId);
    }, 3600);
}
```

### 3. 防止缓存雪崩

**问题**：大量 key 同时过期，导致数据库压力激增

**解决方案**：随机过期时间 + 多级缓存

#### 使用示例

```java
public void cacheUserWithRandomExpire(Long userId, UserDTO user) {
    String cacheKey = "user:" + userId;
    // 基础过期时间 + 随机时间（0-300秒）
    cacheProtection.setPreventAvalanche(cacheKey, user, 3600);
}
```

---

## 分布式锁

### 基本使用

```java
@Autowired
private RedisDistributedLock distributedLock;

public void processOrder(Long orderId) {
    String lockKey = "lock:order:" + orderId;
    String lockValue = UUID.randomUUID().toString();

    try {
        // 尝试获取锁（超时时间30秒）
        boolean locked = distributedLock.tryLock(lockKey, lockValue, 30);
        if (!locked) {
            throw new BizException("订单正在处理中，请稍后重试");
        }

        // 处理订单逻辑
        // ...

    } finally {
        // 释放锁
        distributedLock.unlock(lockKey, lockValue);
    }
}
```

### 带等待时间的锁

```java
public void processOrderWithWait(Long orderId) {
    String lockKey = "lock:order:" + orderId;
    String lockValue = UUID.randomUUID().toString();

    try {
        // 尝试获取锁，最多等待5秒
        boolean locked = distributedLock.tryLock(lockKey, lockValue, 30, 5000);
        if (!locked) {
            throw new BizException("订单正在处理中，请稍后重试");
        }

        // 处理订单逻辑
        // ...

    } finally {
        distributedLock.unlock(lockKey, lockValue);
    }
}
```

### 锁续期（防止业务执行时间过长）

```java
public void processLongTask(Long taskId) {
    String lockKey = "lock:task:" + taskId;
    String lockValue = UUID.randomUUID().toString();

    try {
        boolean locked = distributedLock.tryLock(lockKey, lockValue, 30);
        if (!locked) {
            throw new BizException("任务正在处理中");
        }

        // 定时任务：每10秒续期一次
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            distributedLock.renewLock(lockKey, lockValue, 30);
        }, 10, 10, TimeUnit.SECONDS);

        try {
            // 执行长时间任务
            // ...

        } finally {
            future.cancel(true);
            scheduler.shutdown();
        }

    } finally {
        distributedLock.unlock(lockKey, lockValue);
    }
}
```

---

## 接口幂等性

### 场景1：防止表单重复提交

```java
@Autowired
private IdempotentHelper idempotentHelper;

@PostMapping("/order/create")
public Result<Long> createOrder(@RequestBody CreateOrderReq req) {
    // 生成幂等key
    String idempotentKey = "order:create:" + req.getUserId() + ":" + req.getRequestId();

    // 检查是否重复提交
    if (!idempotentHelper.checkAndSet(idempotentKey, 300)) {
        throw new BizException("请勿重复提交");
    }

    // 创建订单
    Long orderId = orderService.createOrder(req);

    return Result.success(orderId);
}
```

### 场景2：使用 Token 模式

```java
// 1. 前端先获取 token
@GetMapping("/idempotent/token")
public Result<String> getIdempotentToken() {
    String token = idempotentHelper.generateToken(getCurrentUserId(), "createOrder");
    return Result.success(token);
}

// 2. 提交时携带 token
@PostMapping("/order/create")
public Result<Long> createOrder(@RequestParam String token, @RequestBody CreateOrderReq req) {
    // 验证并消费 token
    if (!idempotentHelper.validateAndConsumeToken(token)) {
        throw new BizException("请勿重复提交");
    }

    // 创建订单
    Long orderId = orderService.createOrder(req);

    return Result.success(orderId);
}
```

---

## 限流功能

### 1. 固定窗口限流

```java
@Autowired
private RateLimiter rateLimiter;

@GetMapping("/api/user/list")
public Result<List<User>> getUserList() {
    String key = "api:user:list:" + getCurrentUserId();

    // 每分钟最多10次请求
    if (!rateLimiter.allowFixedWindow(key, 10, 60)) {
        throw new BizException("请求过于频繁，请稍后重试");
    }

    // 业务逻辑
    return Result.success(userService.list());
}
```

### 2. 滑动窗口限流（推荐）

```java
@GetMapping("/api/user/detail")
public Result<User> getUserDetail(@RequestParam Long userId) {
    String key = "api:user:detail:" + getCurrentUserId();

    // 每10秒最多5次请求
    if (!rateLimiter.allowSlidingWindow(key, 5, 10000)) {
        throw new BizException("请求过于频繁，请稍后重试");
    }

    // 业务逻辑
    return Result.success(userService.getById(userId));
}
```

### 3. 令牌桶限流

```java
@PostMapping("/api/order/create")
public Result<Long> createOrder(@RequestBody CreateOrderReq req) {
    String key = "api:order:create:" + getCurrentUserId();

    // 桶容量20，每秒补充5个令牌
    if (!rateLimiter.allowTokenBucket(key, 20, 5)) {
        throw new BizException("请求过于频繁，请稍后重试");
    }

    // 业务逻辑
    return Result.success(orderService.createOrder(req));
}
```

### 4. 漏桶限流

```java
@PostMapping("/api/payment/process")
public Result<Boolean> processPayment(@RequestBody PaymentReq req) {
    String key = "api:payment:process:" + getCurrentUserId();

    // 桶容量10，每秒漏3个
    if (!rateLimiter.allowLeakyBucket(key, 10, 3)) {
        throw new BizException("请求过于频繁，请稍后重试");
    }

    // 业务逻辑
    return Result.success(paymentService.process(req));
}
```

---

## 测试验证

### 运行测试

```bash
# 进入 Redis 模块目录
cd duda-common/duda-common-redis

# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=RedisAdvancedFeaturesTest

# 运行特定测试方法
mvn test -Dtest=RedisAdvancedFeaturesTest#testDistributedLock
```

### 测试覆盖范围

测试类 `RedisAdvancedFeaturesTest` 包含以下测试：

1. **缓存策略测试**
   - ✅ Cache Aside 读操作
   - ✅ Cache Aside 更新操作
   - ✅ 延迟双删（同步）
   - ✅ 延迟双删（异步）

2. **缓存防护测试**
   - ✅ 缓存穿透防护
   - ✅ 缓存击穿防护
   - ✅ 缓存雪崩防护

3. **分布式锁测试**
   - ✅ 基本锁操作
   - ✅ 带等待时间的锁
   - ✅ 并发锁测试

4. **接口幂等性测试**
   - ✅ 基本幂等性检查
   - ✅ Token 模式验证

5. **限流测试**
   - ✅ 固定窗口限流
   - ✅ 滑动窗口限流
   - ✅ 令牌桶限流
   - ✅ 漏桶限流

6. **综合场景测试**
   - ✅ 完整业务流程（限流 + 幂等 + 分布式锁 + 缓存）

---

## 最佳实践

### 1. 合理选择缓存策略

| 场景 | 推荐策略 | 原因 |
|------|---------|------|
| 读多写少 | Cache Aside | 简单高效，一致性好 |
| 写操作频繁 | 延迟双删 | 最大限度保证一致性 |
| 金融/支付 | Write Through | 强一致性要求 |
| 统计/日志 | Write Back | 允许短暂不一致，性能最优 |

### 2. 限流策略选择

| 场景 | 推荐算法 | 原因 |
|------|---------|------|
| API 接口限流 | 滑动窗口 | 平滑限流，用户体验好 |
| 防止突发流量 | 令牌桶 | 可以应对流量突增 |
| 保护下游服务 | 漏桶 | 恒定速率，保护系统稳定 |
| 简单场景 | 固定窗口 | 实现简单，性能高 |

### 3. 分布式锁注意事项

1. **设置合理的超时时间**：根据业务执行时间设置，避免业务未执行完锁就过期
2. **设置唯一的锁值**：使用 UUID 或 requestId，避免误删其他客户端的锁
3. **使用 Lua 脚本**：保证加锁和解锁的原子性
4. **实现锁续期**：对于长时间任务，需要实现锁续期机制

### 4. 缓存过期时间设置

| 数据类型 | 推荐过期时间 | 原因 |
|---------|-------------|------|
| 用户基本信息 | 1-4 小时 | 变更频率低 |
| 热点数据 | 5-30 分钟 | 需要保持新鲜度 |
| 配置信息 | 10-60 分钟 | 允许短暂不一致 |
| 计数器 | 根据业务需求 | 如：1分钟、1小时 |

---

## 性能优化建议

1. **使用连接池**：合理配置 Jedis 连接池参数
2. **批量操作**：使用 Pipeline 或 Lua 脚本减少网络往返
3. **合理使用数据结构**：
   - 简单 KV：String
   - 对象属性：Hash
   - 排行榜：ZSet
   - 去重统计：Set
   - 消息队列：List
4. **设置合理的过期时间**：避免内存占用过大
5. **监控 Redis 性能**：关注慢查询、内存使用、命中率等指标

---

## 故障排查

### 1. 缓存未生效

检查：
- Redis 连接是否正常
- Key 是否正确
- 过期时间是否设置
- 序列化是否正常

### 2. 分布式锁超时

检查：
- 锁超时时间是否太短
- 业务执行时间是否过长
- 是否需要实现锁续期

### 3. 限流不准确

检查：
- 限流算法是否选择正确
- 时间窗口是否合理
- 并发场景下的测试

---

## 总结

本文档详细介绍了 DudaNexus 项目中 Redis 高级功能的使用方法，包括：

✅ 完整的缓存策略实现
✅ 三大缓存防护方案
✅ 生产级分布式锁
✅ 接口幂等性保证
✅ 多种限流算法
✅ 完善的测试用例

所有功能都经过充分测试，可以直接用于生产环境！

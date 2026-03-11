package com.duda.common.redis;

import com.duda.common.redis.cache.CacheAside;
import com.duda.common.redis.cache.CacheProtection;
import com.duda.common.redis.cache.DoubleDeleteCache;
import com.duda.common.redis.idempotent.IdempotentHelper;
import com.duda.common.redis.lock.RedisDistributedLock;
import com.duda.common.redis.ratelimit.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 高级功能集成测试
 *
 * 测试内容：
 * 1. 缓存策略（Cache Aside、延迟双删）
 * 2. 缓存防护（穿透、击穿、雪崩）
 * 3. 分布式锁
 * 4. 接口幂等性
 * 5. 限流功能
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@SpringBootTest
public class RedisAdvancedFeaturesTest {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CacheAside cacheAside;

    @Autowired
    private DoubleDeleteCache doubleDeleteCache;

    @Autowired
    private CacheProtection cacheProtection;

    @Autowired
    private RedisDistributedLock distributedLock;

    @Autowired
    private IdempotentHelper idempotentHelper;

    @Autowired
    private RateLimiter rateLimiter;

    @BeforeEach
    public void setUp() {
        // 清理测试数据
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    // ==================== Cache Aside 测试 ====================

    @Test
    public void testCacheAsideGet() {
        String key = "user:1001";
        String cacheKey = "cache:" + key;

        // 第一次查询（缓存未命中，查询数据库）
        String result1 = cacheAside.get(cacheKey, String.class, k -> {
            return "user-data-from-db";
        }, 60);

        assertEquals("user-data-from-db", result1);
        assertNotNull(redisUtils.get(cacheKey));

        // 第二次查询（缓存命中）
        String result2 = cacheAside.get(cacheKey, String.class, k -> {
            throw new RuntimeException("不应该查询数据库");
        }, 60);

        assertEquals("user-data-from-db", result2);

        System.out.println("✅ Cache Aside 读测试通过");
    }

    @Test
    public void testCacheAsideUpdate() {
        String key = "user:1002";
        String cacheKey = "cache:" + key;

        // 先设置缓存
        redisUtils.set(cacheKey, "old-data", 60);

        // 更新数据
        boolean result = cacheAside.update(cacheKey, k -> {
            // 模拟数据库更新
            return true;
        });

        assertTrue(result);
        assertNull(redisUtils.get(cacheKey)); // 缓存应该被删除

        System.out.println("✅ Cache Aside 更新测试通过");
    }

    // ==================== 延迟双删测试 ====================

    @Test
    public void testDoubleDeleteCacheSync() {
        String key = "product:1001";
        String cacheKey = "cache:" + key;

        // 先设置缓存
        redisUtils.set(cacheKey, "product-data", 60);

        // 延迟双删（同步）
        boolean result = doubleDeleteCache.updateSync(cacheKey, k -> {
            // 模拟数据库更新
            return true;
        }, 100); // 延迟100ms

        assertTrue(result);

        // 等待延迟时间后检查
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNull(redisUtils.get(cacheKey));
        System.out.println("✅ 延迟双删（同步）测试通过");
    }

    @Test
    public void testDoubleDeleteCacheAsync() throws Exception {
        String key = "product:1002";
        String cacheKey = "cache:" + key;

        // 先设置缓存
        redisUtils.set(cacheKey, "product-data", 60);

        // 延迟双删（异步）
        var future = doubleDeleteCache.updateAsync(cacheKey, k -> {
            // 模拟数据库更新
            return true;
        }, 100);

        Boolean result = future.get(1, TimeUnit.SECONDS);
        assertTrue(result);

        // 等待异步任务完成
        Thread.sleep(150);
        assertNull(redisUtils.get(cacheKey));

        System.out.println("✅ 延迟双删（异步）测试通过");
    }

    // ==================== 缓存防护测试 ====================

    @Test
    public void testCachePenetrationProtection() {
        String key = "user:9999"; // 不存在的用户

        // 第一次查询（数据库返回null）
        String result1 = cacheProtection.getPreventPenetration(key, String.class, k -> {
            return null; // 模拟数据库查询不到
        }, 60);

        assertNull(result1);

        // 第二次查询（应该命中空值缓存，不查询数据库）
        String result2 = cacheProtection.getPreventPenetration(key, String.class, k -> {
            throw new RuntimeException("不应该查询数据库");
        }, 60);

        assertNull(result2);

        System.out.println("✅ 缓存穿透防护测试通过");
    }

    @Test
    public void testCacheBreakdownProtection() {
        String key = "hot:user:1001";

        // 使用互斥锁防止击穿
        String result = cacheProtection.getPreventBreakdown(key, String.class, k -> {
            // 模拟数据库查询
            return "hot-user-data";
        }, 60);

        assertEquals("hot-user-data", result);
        assertNotNull(redisUtils.get(key));

        System.out.println("✅ 缓存击穿防护测试通过");
    }

    @Test
    public void testCacheAvalancheProtection() {
        // 随机过期时间应该不同
        String key1 = "user:1001";
        String key2 = "user:1002";
        String key3 = "user:1003";

        cacheProtection.setPreventAvalanche(key1, "data1", 3600);
        cacheProtection.setPreventAvalanche(key2, "data2", 3600);
        cacheProtection.setPreventAvalanche(key3, "data3", 3600);

        long expire1 = redisUtils.getExpire(key1);
        long expire2 = redisUtils.getExpire(key2);
        long expire3 = redisUtils.getExpire(key3);

        // 三个过期时间应该不同
        assertTrue(expire1 != expire2 || expire2 != expire3);

        System.out.println("✅ 缓存雪崩防护测试通过");
    }

    // ==================== 分布式锁测试 ====================

    @Test
    public void testDistributedLock() {
        String lockKey = "lock:test:001";
        String lockValue = "request-123";

        // 获取锁
        boolean locked = distributedLock.tryLock(lockKey, lockValue, 10);
        assertTrue(locked);

        // 尝试再次获取（应该失败）
        boolean lockedAgain = distributedLock.tryLock(lockKey, "request-456", 10);
        assertFalse(lockedAgain);

        // 释放锁
        boolean unlocked = distributedLock.unlock(lockKey, lockValue);
        assertTrue(unlocked);

        // 再次获取（应该成功）
        boolean lockedAfter = distributedLock.tryLock(lockKey, lockValue, 10);
        assertTrue(lockedAfter);

        // 清理
        distributedLock.unlock(lockKey, lockValue);

        System.out.println("✅ 分布式锁测试通过");
    }

    @Test
    public void testDistributedLockWithWait() {
        String lockKey = "lock:test:002";
        String lockValue1 = "request-001";
        String lockValue2 = "request-002";

        // 线程1持有锁
        distributedLock.tryLock(lockKey, lockValue1, 5);

        // 线程2尝试获取锁（带等待时间）
        long startTime = System.currentTimeMillis();
        boolean locked = distributedLock.tryLock(lockKey, lockValue2, 5, 2000);
        long elapsed = System.currentTimeMillis() - startTime;

        assertFalse(locked);
        assertTrue(elapsed >= 2000); // 应该等待了2秒

        // 清理
        distributedLock.unlock(lockKey, lockValue1);

        System.out.println("✅ 分布式锁（带等待时间）测试通过");
    }

    @Test
    public void testDistributedLockConcurrent() throws Exception {
        String lockKey = "lock:test:003";
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            executorService.submit(() -> {
                try {
                    String lockValue = "thread-" + threadNum;
                    boolean locked = distributedLock.tryLock(lockKey, lockValue, 10, 3000);
                    if (locked) {
                        successCount.incrementAndGet();
                        // 模拟工作
                        Thread.sleep(100);
                        distributedLock.unlock(lockKey, lockValue);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(15, TimeUnit.SECONDS);

        // 所有线程都应该成功获取锁（因为等待时间足够）
        assertEquals(threadCount, successCount.get());

        executorService.shutdown();
        System.out.println("✅ 分布式锁并发测试通过");
    }

    // ==================== 接口幂等性测试 ====================

    @Test
    public void testIdempotentHelper() {
        String key = "order:create:001";

        // 第一次请求（应该允许）
        boolean allowed1 = idempotentHelper.checkAndSet(key, 60);
        assertTrue(allowed1);

        // 第二次请求（应该拒绝）
        boolean allowed2 = idempotentHelper.checkAndSet(key, 60);
        assertFalse(allowed2);

        // 清理
        idempotentHelper.delete(key);

        // 再次请求（应该允许）
        boolean allowed3 = idempotentHelper.checkAndSet(key, 60);
        assertTrue(allowed3);

        System.out.println("✅ 接口幂等性测试通过");
    }

    @Test
    public void testIdempotentToken() {
        Long userId = 1001L;
        String operation = "createOrder";

        // 生成token
        String token = idempotentHelper.generateToken(userId, operation);
        assertNotNull(token);

        // 验证并消费token（第一次）
        boolean valid1 = idempotentHelper.validateAndConsumeToken(token);
        assertTrue(valid1);

        // 验证并消费token（第二次，应该失败）
        boolean valid2 = idempotentHelper.validateAndConsumeToken(token);
        assertFalse(valid2);

        System.out.println("✅ 幂等token测试通过");
    }

    // ==================== 限流测试 ====================

    @Test
    public void testRateLimiterFixedWindow() {
        String key = "api:user:list";
        int limit = 5;
        int windowSeconds = 10;

        int allowedCount = 0;
        for (int i = 0; i < limit + 2; i++) {
            boolean allowed = rateLimiter.allowFixedWindow(key, limit, windowSeconds);
            if (allowed) {
                allowedCount++;
            }
        }

        // 应该只有limit个请求通过
        assertEquals(limit, allowedCount);

        System.out.println("✅ 固定窗口限流测试通过");
    }

    @Test
    public void testRateLimiterSlidingWindow() {
        String key = "api:user:detail";
        int limit = 10;
        long windowMs = 1000; // 1秒窗口

        int allowedCount = 0;
        for (int i = 0; i < limit + 2; i++) {
            boolean allowed = rateLimiter.allowSlidingWindow(key, limit, windowMs);
            if (allowed) {
                allowedCount++;
            }
        }

        // 应该只有limit个请求通过
        assertEquals(limit, allowedCount);

        System.out.println("✅ 滑动窗口限流测试通过");
    }

    @Test
    public void testRateLimiterTokenBucket() {
        String key = "api:order:create";
        long capacity = 10; // 桶容量10
        long refillRate = 5; // 每秒生成5个令牌

        int allowedCount = 0;
        for (int i = 0; i < capacity + 2; i++) {
            boolean allowed = rateLimiter.allowTokenBucket(key, capacity, refillRate);
            if (allowed) {
                allowedCount++;
            }
        }

        // 应该只有capacity个请求通过
        assertEquals(capacity, allowedCount);

        // 等待令牌补充
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 1秒后应该补充了5个令牌
        boolean allowed = rateLimiter.allowTokenBucket(key, capacity, refillRate);
        assertTrue(allowed);

        System.out.println("✅ 令牌桶限流测试通过");
    }

    @Test
    public void testRateLimiterLeakyBucket() {
        String key = "api:payment:process";
        long capacity = 10; // 桶容量10
        long leakRate = 5; // 每秒漏5个

        int allowedCount = 0;
        for (int i = 0; i < capacity + 2; i++) {
            boolean allowed = rateLimiter.allowLeakyBucket(key, capacity, leakRate);
            if (allowed) {
                allowedCount++;
            }
        }

        // 应该只有capacity个请求通过
        assertEquals(capacity, allowedCount);

        System.out.println("✅ 漏桶限流测试通过");
    }

    // ==================== 综合测试 ====================

    @Test
    public void testComplexScenario() throws Exception {
        // 模拟一个复杂的业务场景：创建订单
        String userId = "user:1001";
        String orderId = "order:" + System.currentTimeMillis();

        // 1. 限流检查
        boolean rateLimited = rateLimiter.allowSlidingWindow(userId + ":createOrder", 10, 60000);
        assertTrue(rateLimited, "限流应该通过");

        // 2. 幂等性检查
        boolean idempotent = idempotentHelper.checkAndSet(orderId, 300);
        assertTrue(idempotent, "幂等性检查应该通过");

        // 3. 分布式锁（防止并发创建）
        String lockKey = "lock:create:order:" + userId;
        String lockValue = "order-" + System.currentTimeMillis();
        boolean locked = distributedLock.tryLock(lockKey, lockValue, 10);
        assertTrue(locked, "应该获取到锁");

        try {
            // 4. 缓存查询用户信息
            String userInfo = cacheProtection.getPreventBreakdown(
                    "user:info:" + userId,
                    String.class,
                    k -> "user-info-from-db",
                    3600
            );
            assertEquals("user-info-from-db", userInfo);

            // 5. 业务逻辑...
            Thread.sleep(100);

        } finally {
            // 6. 释放锁
            distributedLock.unlock(lockKey, lockValue);
        }

        System.out.println("✅ 综合场景测试通过");
    }
}

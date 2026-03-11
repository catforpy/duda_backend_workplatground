package com.duda.common.redis.ratelimit;

import com.duda.common.redis.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Redis 限流器
 *
 * 支持多种限流算法：
 * 1. 固定窗口算法
 * 2. 滑动窗口算法
 * 3. 令牌桶算法
 * 4. 漏桶算法
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Component
public class RateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    @Autowired
    private RedisUtils redisUtils;

    /**
     * Lua 脚本：滑动窗口限流
     * 移除时间窗口外的记录，统计当前窗口内的请求数
     */
    private static final String SLIDING_WINDOW_LUA =
            "local key = KEYS[1] " +
                    "local now = tonumber(ARGV[1]) " +
                    "local window = tonumber(ARGV[2]) " +
                    "local limit = tonumber(ARGV[3]) " +
                    "-- 清除时间窗口外的数据 " +
                    "redis.call('zremrangebyscore', key, '-inf', now - window) " +
                    "-- 获取当前窗口内的请求数 " +
                    "local count = redis.call('zcard', key) " +
                    "-- 判断是否超限 " +
                    "if count < limit then " +
                    "  redis.call('zadd', key, now, now) " +
                    "  redis.call('expire', key, window / 1000) " +
                    "  return 1 " +
                    "else " +
                    "  return 0 " +
                    "end";

    // ==================== 固定窗口算法 ====================

    /**
     * 固定窗口限流
     *
     * @param key 限流key
     * @param limit 限制数量
     * @param timeoutSeconds 时间窗口（秒）
     * @return 是否允许通过（true: 允许，false: 拒绝）
     */
    public boolean allowFixedWindow(String key, long limit, long timeoutSeconds) {
        String fullKey = "rate:fixed:" + key;

        // 获取当前计数
        Long currentCount = redisUtils.getRedisTemplate().opsForValue().increment(fullKey);

        if (currentCount == null) {
            // key 不存在，重新设置
            redisUtils.set(fullKey, 1L, timeoutSeconds);
            logger.debug("固定窗口限流：首次访问，key: {}, limit: {}", fullKey, limit);
            return true;
        }

        if (currentCount == 1) {
            // 第一次设置，设置过期时间
            redisUtils.getRedisTemplate().expire(fullKey, timeoutSeconds,
                    java.util.concurrent.TimeUnit.SECONDS);
        }

        if (currentCount > limit) {
            logger.debug("固定窗口限流：超限，key: {}, current: {}, limit: {}", fullKey, currentCount, limit);
            return false;
        }

        logger.debug("固定窗口限流：允许通过，key: {}, current: {}, limit: {}", fullKey, currentCount, limit);
        return true;
    }

    // ==================== 滑动窗口算法 ====================

    /**
     * 滑动窗口限流
     *
     * @param key 限流key
     * @param limit 限制数量
     * @param windowSizeMs 窗口大小（毫秒）
     * @return 是否允许通过（true: 允许，false: 拒绝）
     */
    public boolean allowSlidingWindow(String key, long limit, long windowSizeMs) {
        String fullKey = "rate:sliding:" + key;
        long now = System.currentTimeMillis();

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(SLIDING_WINDOW_LUA);
        redisScript.setResultType(Long.class);

        Long result = redisUtils.getRedisTemplate().execute(
                redisScript,
                Collections.singletonList(fullKey),
                String.valueOf(now),
                String.valueOf(windowSizeMs),
                String.valueOf(limit)
        );

        boolean allowed = result != null && result == 1L;
        if (!allowed) {
            logger.debug("滑动窗口限流：超限，key: {}, limit: {}, windowSizeMs: {}", fullKey, limit, windowSizeMs);
        }
        return allowed;
    }

    // ==================== 令牌桶算法 ====================

    /**
     * 令牌桶限流
     *
     * @param key 限流key
     * @param capacity 桶容量
     * @param refillRate 令牌生成速率（个/秒）
     * @return 是否允许通过（true: 允许，false: 拒绝）
     */
    public boolean allowTokenBucket(String key, long capacity, long refillRate) {
        String fullKey = "rate:token:" + key;
        long now = System.currentTimeMillis();
        long interval = 1000 / refillRate; // 每个令牌的间隔时间（毫秒）

        // Lua 脚本实现令牌桶
        String luaScript =
                "local key = KEYS[1] " +
                        "local now = tonumber(ARGV[1]) " +
                        "local capacity = tonumber(ARGV[2]) " +
                        "local interval = tonumber(ARGV[3]) " +
                        "-- 获取当前令牌数和上次填充时间 " +
                        "local info = redis.call('hmget', key, 'tokens', 'last_refill') " +
                        "local tokens = tonumber(info[1]) or capacity " +
                        "local last_refill = tonumber(info[2]) or now " +
                        "-- 计算需要填充的令牌数 " +
                        "local elapsed = now - last_refill " +
                        "local new_tokens = math.floor(elapsed / interval) " +
                        "-- 更新令牌数（不超过容量） " +
                        "tokens = math.min(capacity, tokens + new_tokens) " +
                        "-- 判断是否有足够令牌 " +
                        "if tokens >= 1 then " +
                        "  redis.call('hmset', key, 'tokens', tokens - 1, 'last_refill', now) " +
                        "  redis.call('expire', key, math.ceil(capacity / refillRate) + 1) " +
                        "  return 1 " +
                        "else " +
                        "  redis.call('hmset', key, 'tokens', tokens, 'last_refill', now) " +
                        "  redis.call('expire', key, math.ceil(capacity / refillRate) + 1) " +
                        "  return 0 " +
                        "end";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);

        Long result = redisUtils.getRedisTemplate().execute(
                redisScript,
                Collections.singletonList(fullKey),
                String.valueOf(now),
                String.valueOf(capacity),
                String.valueOf(interval)
        );

        boolean allowed = result != null && result == 1L;
        if (!allowed) {
            logger.debug("令牌桶限流：无可用令牌，key: {}, capacity: {}, refillRate: {}", fullKey, capacity, refillRate);
        }
        return allowed;
    }

    // ==================== 漏桶算法 ====================

    /**
     * 漏桶限流
     *
     * @param key 限流key
     * @param capacity 桶容量
     * @param leakRate 漏水速率（个/秒）
     * @return 是否允许通过（true: 允许，false: 拒绝）
     */
    public boolean allowLeakyBucket(String key, long capacity, long leakRate) {
        String fullKey = "rate:leaky:" + key;
        long now = System.currentTimeMillis();
        long interval = 1000 / leakRate; // 每个请求的间隔时间（毫秒）

        // Lua 脚本实现漏桶
        String luaScript =
                "local key = KEYS[1] " +
                        "local now = tonumber(ARGV[1]) " +
                        "local capacity = tonumber(ARGV[2]) " +
                        "local interval = tonumber(ARGV[3]) " +
                        "-- 获取当前水量和上次漏水时间 " +
                        "local info = redis.call('hmget', key, 'water', 'last_leak') " +
                        "local water = tonumber(info[1]) or 0 " +
                        "local last_leak = tonumber(info[2]) or now " +
                        "-- 计算漏掉的水量 " +
                        "local elapsed = now - last_leak " +
                        "local leaked = math.floor(elapsed / interval) " +
                        "-- 更新水量 " +
                        "water = math.max(0, water - leaked) " +
                        "-- 判断是否可以加水 " +
                        "if water < capacity then " +
                        "  redis.call('hmset', key, 'water', water + 1, 'last_leak', now) " +
                        "  redis.call('expire', key, math.ceil(capacity / leakRate) + 1) " +
                        "  return 1 " +
                        "else " +
                        "  redis.call('hmset', key, 'water', water, 'last_leak', now) " +
                        "  redis.call('expire', key, math.ceil(capacity / leakRate) + 1) " +
                        "  return 0 " +
                        "end";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);

        Long result = redisUtils.getRedisTemplate().execute(
                redisScript,
                Collections.singletonList(fullKey),
                String.valueOf(now),
                String.valueOf(capacity),
                String.valueOf(interval)
        );

        boolean allowed = result != null && result == 1L;
        if (!allowed) {
            logger.debug("漏桶限流：桶已满，key: {}, capacity: {}, leakRate: {}", fullKey, capacity, leakRate);
        }
        return allowed;
    }

    // ==================== 工具方法 ====================

    /**
     * 重置限流计数器
     *
     * @param key 限流key
     */
    public void reset(String key) {
        redisUtils.delete("rate:fixed:" + key);
        redisUtils.delete("rate:sliding:" + key);
        redisUtils.delete("rate:token:" + key);
        redisUtils.delete("rate:leaky:" + key);
        logger.debug("重置限流计数器，key: {}", key);
    }

    /**
     * 获取当前限流状态
     *
     * @param key 限流key
     * @param type 限流类型（fixed, sliding, token, leaky）
     * @return 当前计数或状态
     */
    public Object getStatus(String key, String type) {
        String fullKey = "rate:" + type + ":" + key;

        switch (type) {
            case "fixed":
            case "sliding":
                return redisUtils.get(fullKey);
            case "token":
            case "leaky":
                return redisUtils.getRedisTemplate().opsForHash().entries(fullKey);
            default:
                throw new IllegalArgumentException("不支持的限流类型: " + type);
        }
    }
}

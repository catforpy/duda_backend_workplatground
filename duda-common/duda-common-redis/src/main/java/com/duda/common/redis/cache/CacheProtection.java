package com.duda.common.redis.cache;

import com.duda.common.redis.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存防护工具类
 *
 * 防止以下问题：
 * 1. 缓存穿透：查询不存在的数据，导致每次都查数据库
 *    - 解决方案：缓存空值 + 布隆过滤器
 *
 * 2. 缓存击穿：热点key过期，大量请求同时穿透到数据库
 *    - 解决方案：互斥锁（分布式锁）
 *
 * 3. 缓存雪崩：大量key同时过期，导致数据库压力激增
 *    - 解决方案：随机过期时间 + 多级缓存
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Component
public class CacheProtection {

    private static final Logger logger = LoggerFactory.getLogger(CacheProtection.class);

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 本地缓存锁（防止缓存击穿）
     */
    private final ConcurrentHashMap<String, Object> localLocks = new ConcurrentHashMap<>();

    /**
     * 空值的标记
     */
    private static final String NULL_VALUE = "NULL_VALUE";

    /**
     * 空值的缓存时间（秒）- 5分钟
     */
    private static final long NULL_VALUE_CACHE_SECONDS = 300L;

    /**
     * 分布式锁的默认超时时间（秒）- 10秒
     */
    private static final long LOCK_TIMEOUT_SECONDS = 10L;

    /**
     * 防止缓存穿透 - 查询数据（缓存空值）
     *
     * @param cacheKey 缓存key
     * @param clazz 返回类型
     * @param dbLoader 数据库加载函数
     * @param expireSeconds 过期时间（秒）
     * @param <T> 泛型类型
     * @return 数据
     */
    public <T> T getPreventPenetration(String cacheKey, Class<T> clazz, Function<String, T> dbLoader, long expireSeconds) {
        // 1. 先查缓存
        T cachedData = redisUtils.get(cacheKey, clazz);
        if (cachedData != null) {
            // 检查是否是空值标记
            if (NULL_VALUE.equals(cachedData)) {
                logger.debug("命中空值缓存，key: {}", cacheKey);
                return null;
            }
            logger.debug("缓存命中，key: {}", cacheKey);
            return cachedData;
        }

        // 2. 缓存未命中，查询数据库
        logger.debug("缓存未命中，查询数据库，key: {}", cacheKey);
        T dbData = dbLoader.apply(cacheKey);

        // 3. 将数据写入缓存（包括空值）
        if (dbData == null) {
            // 缓存空值，防止穿透
            redisUtils.set(cacheKey, NULL_VALUE, NULL_VALUE_CACHE_SECONDS);
            logger.debug("数据不存在，缓存空值，key: {}", cacheKey);
        } else {
            // 缓存真实数据，添加随机过期时间防止雪崩
            long randomExpire = expireSeconds + (long) (Math.random() * 300); // 基础时间 + 随机0-300秒
            redisUtils.set(cacheKey, dbData, randomExpire);
            logger.debug("数据已写入缓存，key: {}, expireSeconds: {}", cacheKey, randomExpire);
        }

        return dbData;
    }

    /**
     * 防止缓存击穿 - 查询数据（互斥锁）
     *
     * @param cacheKey 缓存key
     * @param clazz 返回类型
     * @param dbLoader 数据库加载函数
     * @param expireSeconds 过期时间（秒）
     * @param <T> 泛型类型
     * @return 数据
     */
    public <T> T getPreventBreakdown(String cacheKey, Class<T> clazz, Function<String, T> dbLoader, long expireSeconds) {
        // 1. 先查缓存
        T cachedData = redisUtils.get(cacheKey, clazz);
        if (cachedData != null) {
            logger.debug("缓存命中，key: {}", cacheKey);
            return cachedData;
        }

        // 2. 缓存未命中，使用分布式锁
        String lockKey = "lock:" + cacheKey;
        String lockValue = String.valueOf(System.currentTimeMillis());

        try {
            // 尝试获取锁
            boolean locked = tryLock(lockKey, lockValue, LOCK_TIMEOUT_SECONDS);
            if (locked) {
                logger.debug("获取分布式锁成功，key: {}", lockKey);

                // 双重检查（其他线程可能已经更新了缓存）
                cachedData = redisUtils.get(cacheKey, clazz);
                if (cachedData != null) {
                    logger.debug("双重检查：缓存已更新，key: {}", cacheKey);
                    return cachedData;
                }

                // 查询数据库
                logger.debug("查询数据库，key: {}", cacheKey);
                T dbData = dbLoader.apply(cacheKey);
                if (dbData == null) {
                    logger.warn("数据库中也不存在该数据，key: {}", cacheKey);
                    return null;
                }

                // 写入缓存
                long randomExpire = expireSeconds + (long) (Math.random() * 300);
                redisUtils.set(cacheKey, dbData, randomExpire);
                logger.debug("数据已写入缓存，key: {}, expireSeconds: {}", cacheKey, randomExpire);

                return dbData;
            } else {
                // 获取锁失败，等待片刻后重试获取缓存
                logger.debug("获取分布式锁失败，等待重试，key: {}", lockKey);
                Thread.sleep(100);

                // 再次尝试获取缓存
                cachedData = redisUtils.get(cacheKey, clazz);
                if (cachedData != null) {
                    logger.debug("重试：缓存命中，key: {}", cacheKey);
                    return cachedData;
                }

                // 还是没命中，返回null或抛出异常
                logger.warn("重试后缓存仍未命中，key: {}", cacheKey);
                return null;
            }
        } catch (InterruptedException e) {
            logger.error("获取锁时被中断，key: {}", lockKey, e);
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            logger.error("防止缓存击穿失败，key: {}", cacheKey, e);
            return null;
        } finally {
            // 释放锁
            releaseLock(lockKey, lockValue);
        }
    }

    /**
     * 防止缓存雪崩 - 设置随机过期时间
     *
     * @param key 缓存key
     * @param value 缓存值
     * @param baseExpireSeconds 基础过期时间（秒）
     */
    public void setPreventAvalanche(String key, Object value, long baseExpireSeconds) {
        // 添加随机时间（0-300秒）防止同时过期
        long randomExpire = baseExpireSeconds + (long) (Math.random() * 300);
        redisUtils.set(key, value, randomExpire);
        logger.debug("设置缓存（防雪崩），key: {}, randomExpire: {}秒", key, randomExpire);
    }

    /**
     * 尝试获取分布式锁（使用 SETNX 实现）
     *
     * @param lockKey 锁的key
     * @param lockValue 锁的值（用于释放时验证）
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否获取成功
     */
    private boolean tryLock(String lockKey, String lockValue, long timeoutSeconds) {
        // 使用 Lua 脚本实现 SETNX + EXPIRE 的原子操作
        String luaScript =
                "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
                        "redis.call('expire', KEYS[1], ARGV[2]) " +
                        "return 1 " +
                        "else " +
                        "return 0 " +
                        "end";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);

        Long result = redisUtils.getRedisTemplate().execute(
                redisScript,
                Collections.singletonList(lockKey),
                lockValue,
                String.valueOf(timeoutSeconds)
        );

        return result != null && result == 1L;
    }

    /**
     * 释放分布式锁（使用 Lua 脚本确保只释放自己持有的锁）
     *
     * @param lockKey 锁的key
     * @param lockValue 锁的值
     */
    private void releaseLock(String lockKey, String lockValue) {
        String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "return redis.call('del', KEYS[1]) " +
                        "else " +
                        "return 0 " +
                        "end";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);

        redisUtils.getRedisTemplate().execute(
                redisScript,
                Collections.singletonList(lockKey),
                lockValue
        );

        logger.debug("释放分布式锁，key: {}", lockKey);
    }
}

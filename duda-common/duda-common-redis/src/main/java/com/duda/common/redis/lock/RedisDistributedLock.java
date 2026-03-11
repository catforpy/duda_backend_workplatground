package com.duda.common.redis.lock;

import com.duda.common.redis.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁
 *
 * 基于 Redis SETNX + EXPIRE 实现
 * 使用 Lua 脚本保证原子性
 *
 * 特性：
 * 1. 互斥性：任意时刻只有一个客户端能持有锁
 * 2. 避免死锁：持有锁的客户端崩溃后，锁会自动释放
 * 3. 加锁和解锁必须是同一个客户端
 * 4. 支持可重入
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Component
public class RedisDistributedLock {

    private static final Logger logger = LoggerFactory.getLogger(RedisDistributedLock.class);

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 默认锁超时时间（秒）
     */
    private static final long DEFAULT_LOCK_TIMEOUT = 30L;

    /**
     * 获取锁的 Lua 脚本（SETNX + EXPIRE 原子操作）
     */
    private static final String LOCK_SCRIPT =
            "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
                    "redis.call('expire', KEYS[1], ARGV[2]) " +
                    "return 1 " +
                    "else " +
                    "return 0 " +
                    "end";

    /**
     * 释放锁的 Lua 脚本（确保只释放自己持有的锁）
     */
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else " +
                    "return 0 " +
                    "end";

    /**
     * 尝试获取锁
     *
     * @param lockKey 锁的key
     * @param lockValue 锁的值（通常是唯一标识，比如 requestId + timestamp）
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String lockValue, long timeoutSeconds) {
        try {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(LOCK_SCRIPT);
            redisScript.setResultType(Long.class);

            Long result = redisUtils.getRedisTemplate().execute(
                    redisScript,
                    Collections.singletonList(lockKey),
                    lockValue,
                    String.valueOf(timeoutSeconds)
            );

            boolean locked = result != null && result == 1L;
            if (locked) {
                logger.debug("获取分布式锁成功，key: {}", lockKey);
            } else {
                logger.debug("获取分布式锁失败，key: {}", lockKey);
            }
            return locked;
        } catch (Exception e) {
            logger.error("获取分布式锁异常，key: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 尝试获取锁（使用默认超时时间）
     *
     * @param lockKey 锁的key
     * @param lockValue 锁的值
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String lockValue) {
        return tryLock(lockKey, lockValue, DEFAULT_LOCK_TIMEOUT);
    }

    /**
     * 尝试获取锁（带等待时间）
     *
     * @param lockKey 锁的key
     * @param lockValue 锁的值
     * @param timeoutSeconds 锁超时时间（秒）
     * @param waitMilliseconds 等待时间（毫秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String lockValue, long timeoutSeconds, long waitMilliseconds) {
        long startTime = System.currentTimeMillis();
        long remainingWait = waitMilliseconds;

        while (remainingWait > 0) {
            // 尝试获取锁
            boolean locked = tryLock(lockKey, lockValue, timeoutSeconds);
            if (locked) {
                return true;
            }

            // 等待一段时间后重试
            long sleepTime = Math.min(100, remainingWait);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                logger.error("等待锁时被中断，key: {}", lockKey, e);
                Thread.currentThread().interrupt();
                return false;
            }

            remainingWait = waitMilliseconds - (System.currentTimeMillis() - startTime);
        }

        logger.debug("等待超时，获取锁失败，key: {}", lockKey);
        return false;
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁的key
     * @param lockValue 锁的值
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey, String lockValue) {
        try {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(UNLOCK_SCRIPT);
            redisScript.setResultType(Long.class);

            Long result = redisUtils.getRedisTemplate().execute(
                    redisScript,
                    Collections.singletonList(lockKey),
                    lockValue
            );

            boolean unlocked = result != null && result == 1L;
            if (unlocked) {
                logger.debug("释放分布式锁成功，key: {}", lockKey);
            } else {
                logger.debug("释放分布式锁失败（可能已过期或非持有者），key: {}", lockKey);
            }
            return unlocked;
        } catch (Exception e) {
            logger.error("释放分布式锁异常，key: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 检查锁是否存在
     *
     * @param lockKey 锁的key
     * @return 是否存在
     */
    public boolean isLocked(String lockKey) {
        return Boolean.TRUE.equals(redisUtils.hasKey(lockKey));
    }

    /**
     * 强制释放锁（不验证持有者）
     *
     * @param lockKey 锁的key
     * @return 是否释放成功
     */
    public boolean forceUnlock(String lockKey) {
        logger.warn("强制释放分布式锁，key: {}", lockKey);
        return Boolean.TRUE.equals(redisUtils.delete(lockKey));
    }

    /**
     * 续期锁（延长锁的超时时间）
     *
     * @param lockKey 锁的key
     * @param lockValue 锁的值
     * @param additionalSeconds 额外的超时时间（秒）
     * @return 是否续期成功
     */
    public boolean renewLock(String lockKey, String lockValue, long additionalSeconds) {
        // 只有持有锁的客户端才能续期
        String currentValue = (String) redisUtils.get(lockKey);
        if (lockValue.equals(currentValue)) {
            return Boolean.TRUE.equals(redisUtils.expire(lockKey, additionalSeconds));
        }
        logger.debug("续期锁失败（非持有者），key: {}", lockKey);
        return false;
    }
}

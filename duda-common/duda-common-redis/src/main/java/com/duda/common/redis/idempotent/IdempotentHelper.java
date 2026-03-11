package com.duda.common.redis.idempotent;

import com.duda.common.redis.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 接口幂等性工具类
 *
 * 用于防止重复提交，确保同一个请求只被处理一次
 *
 * 使用场景：
 * 1. 表单重复提交
 * 2. 前端重复点击
 * 3. 网络重试导致的重复请求
 * 4. 支付订单等需要幂等的场景
 *
 * 实现原理：
 * 1. 请求时生成唯一标识（如：userId + requestId）
 * 2. 将标识存入 Redis，设置过期时间
 * 3. 后续请求先检查标识是否已存在
 * 4. 存在则拒绝，不存在则允许并设置标识
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Component
public class IdempotentHelper {

    private static final Logger logger = LoggerFactory.getLogger(IdempotentHelper.class);

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 默认幂等 key 的前缀
     */
    private static final String DEFAULT_PREFIX = "idempotent:";

    /**
     * 默认幂等 token 的过期时间（秒）- 5分钟
     */
    private static final long DEFAULT_EXPIRE_SECONDS = 300L;

    /**
     * 检查并设置幂等 token
     *
     * @param key 幂等标识
     * @return 是否允许执行（true: 允许，false: 拒绝）
     */
    public boolean checkAndSet(String key) {
        return checkAndSet(key, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 检查并设置幂等 token
     *
     * @param key 幂等标识
     * @param expireSeconds 过期时间（秒）
     * @return 是否允许执行（true: 允许，false: 拒绝）
     */
    public boolean checkAndSet(String key, long expireSeconds) {
        String fullKey = DEFAULT_PREFIX + key;

        // 检查是否已存在
        boolean exists = Boolean.TRUE.equals(redisUtils.hasKey(fullKey));
        if (exists) {
            logger.debug("幂等检查：重复请求，key: {}", fullKey);
            return false;
        }

        // 不存在，设置 token
        redisUtils.set(fullKey, "1", expireSeconds);
        logger.debug("幂等检查：首次请求，key: {}, expireSeconds: {}", fullKey, expireSeconds);
        return true;
    }

    /**
     * 生成幂等 token（前端提交前先获取）
     *
     * @param userId 用户ID
     * @param operation 操作类型
     * @return token
     */
    public String generateToken(Long userId, String operation) {
        String token = System.currentTimeMillis() + ":" + userId + ":" + operation;
        String key = "token:" + token;
        redisUtils.set(DEFAULT_PREFIX + key, "1", DEFAULT_EXPIRE_SECONDS);
        logger.debug("生成幂等token，userId: {}, operation: {}, token: {}", userId, operation, token);
        return token;
    }

    /**
     * 验证并消费幂等 token
     *
     * @param token token
     * @return 是否有效（true: 有效，false: 无效或已使用）
     */
    public boolean validateAndConsumeToken(String token) {
        String key = "token:" + token;
        String fullKey = DEFAULT_PREFIX + key;

        // 检查 token 是否存在
        String value = (String) redisUtils.get(fullKey);
        if (value == null) {
            logger.debug("验证幂等token：token不存在或已过期，token: {}", token);
            return false;
        }

        // 消费 token（删除）
        redisUtils.delete(fullKey);
        logger.debug("验证幂等token：token有效并已消费，token: {}", token);
        return true;
    }

    /**
     * 删除幂等 token（主动取消）
     *
     * @param key 幂等标识
     */
    public void delete(String key) {
        String fullKey = DEFAULT_PREFIX + key;
        redisUtils.delete(fullKey);
        logger.debug("删除幂等token，key: {}", fullKey);
    }

    /**
     * 检查幂等状态（不设置）
     *
     * @param key 幂等标识
     * @return 是否已执行（true: 已执行，false: 未执行）
     */
    public boolean check(String key) {
        String fullKey = DEFAULT_PREFIX + key;
        return Boolean.TRUE.equals(redisUtils.hasKey(fullKey));
    }

    /**
     * 设置幂等标识（用于手动标记）
     *
     * @param key 幂等标识
     * @param expireSeconds 过期时间（秒）
     */
    public void set(String key, long expireSeconds) {
        String fullKey = DEFAULT_PREFIX + key;
        redisUtils.set(fullKey, "1", expireSeconds);
        logger.debug("设置幂等标识，key: {}, expireSeconds: {}", fullKey, expireSeconds);
    }

    /**
     * 设置幂等标识（使用默认过期时间）
     *
     * @param key 幂等标识
     */
    public void set(String key) {
        set(key, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 获取幂等标识的剩余有效时间
     *
     * @param key 幂等标识
     * @return 剩余时间（秒），-1表示不存在或永久有效
     */
    public long getRemainingTime(String key) {
        String fullKey = DEFAULT_PREFIX + key;
        return redisUtils.getExpire(fullKey);
    }
}

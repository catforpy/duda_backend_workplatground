package com.duda.common.redis.key;

import com.duda.common.redis.RedisKeyBuilder;
import org.springframework.context.annotation.Configuration;

/**
 * 用户服务 Redis Key 构建器
 *
 * 使用方法：
 * <pre>
 * &#64;Autowired
 * private UserRedisKeyBuilder redisKeyBuilder;
 *
 * // 构建用户信息缓存key
 * String key = redisKeyBuilder.buildUserInfoKey(userId);
 * redisTemplate.opsForValue().set(key, userDTO);
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Configuration
public class UserRedisKeyBuilder extends RedisKeyBuilder {

    private static final String USER = "user";
    private static final String INFO = "info";
    private static final String TOKEN = "token";
    private static final String ONLINE = "online";
    private static final String BLACKLIST = "blacklist";

    /**
     * 构建用户信息缓存key
     * 格式: duda:user:info:{userId}
     */
    public String buildUserInfoKey(Long userId) {
        return getPrefix() + getSplitItem() + USER + getSplitItem() + INFO + getSplitItem() + userId;
    }

    /**
     * 构建用户Token缓存key
     * 格式: duda:user:token:{token}
     */
    public String buildUserTokenKey(String token) {
        return getPrefix() + getSplitItem() + USER + getSplitItem() + TOKEN + getSplitItem() + token;
    }

    /**
     * 构建用户在线状态key
     * 格式: duda:user:online:{userId}
     */
    public String buildUserOnlineKey(Long userId) {
        return getPrefix() + getSplitItem() + USER + getSplitItem() + ONLINE + getSplitItem() + userId;
    }

    /**
     * 构建用户Token黑名单key
     * 格式: duda:user:blacklist:{token}
     */
    public String buildUserBlacklistKey(String token) {
        return getPrefix() + getSplitItem() + USER + getSplitItem() + BLACKLIST + getSplitItem() + token;
    }

    /**
     * 构建用户信息集合key（用于存储用户ID列表）
     * 格式: duda:user:info:set
     */
    public String buildUserInfoSetKey() {
        return getPrefix() + getSplitItem() + USER + getSplitItem() + INFO + getSplitItem() + "set";
    }
}

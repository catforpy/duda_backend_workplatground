package com.duda.common.redis;

import org.springframework.beans.factory.annotation.Value;

/**
 * Redis键生成器基类
 * 提供统一的键前缀和分隔符
 *
 * 使用方法：
 * <pre>
 * &#64;Component
 * public class UserRedisKeyBuilder extends RedisKeyBuilder {
 *     private static final String USER = "user";
 *
 *     public String buildUserInfoKey(Long userId) {
 *         return getPrefix() + getSplitItem() + USER + getSplitItem() + userId;
 *     }
 * }
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
public class RedisKeyBuilder {

    /**
     * 从配置文件中获取应用名称（spring.application.name）
     */
    @Value("${spring.application.name:duda}")
    private String applicationName;

    /**
     * 键的分隔符（固定为冒号）
     */
    private static final String SPLIT_ITEM = ":";

    /**
     * 获取分隔符
     */
    public String getSplitItem() {
        return SPLIT_ITEM;
    }

    /**
     * 获取键前缀（即应用名称）
     */
    public String getPrefix() {
        return applicationName;
    }
}

package com.duda.common.redis.key;

import com.duda.common.redis.RedisKeyBuilder;
import org.springframework.context.annotation.Configuration;

/**
 * API密钥服务 Redis Key 构建器
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Configuration
public class ApiKeyRedisKeyBuilder extends RedisKeyBuilder {

    private static final String API_KEY = "apikey";
    private static final String DEFAULT = "default";
    private static final String LIST = "list";

    /**
     * 构建用户默认API密钥缓存key（根据userId）
     * 格式: duda:apikey:default:{userId}
     */
    public String buildDefaultApiKeyKey(Long userId) {
        return getPrefix() + getSplitItem() + API_KEY + getSplitItem() + DEFAULT + getSplitItem() + userId;
    }

    /**
     * 构建用户默认API密钥缓存key（根据租户ID和用户ID）
     * 格式: duda:apikey:default:{tenantId}:{userId}
     */
    public String buildDefaultApiKeyKey(Long tenantId, Long userId) {
        return getPrefix() + getSplitItem() + API_KEY + getSplitItem() + DEFAULT + getSplitItem() + tenantId + getSplitItem() + userId;
    }

    /**
     * 构建用户默认API密钥缓存key（根据username）
     * 格式: duda:apikey:default:username:{username}
     */
    public String buildDefaultApiKeyByUsernameKey(String username) {
        return getPrefix() + getSplitItem() + API_KEY + getSplitItem() + DEFAULT + getSplitItem() + "username" + getSplitItem() + username;
    }

    /**
     * 构建用户API密钥列表缓存key（根据username）
     * 格式: duda:apikey:list:{username}
     */
    public String buildApiKeyListKey(String username) {
        return getPrefix() + getSplitItem() + API_KEY + getSplitItem() + LIST + getSplitItem() + username;
    }

    /**
     * 构建单个API密钥缓存key
     * 格式: duda:apikey:{keyId}
     */
    public String buildApiKeyKey(Long keyId) {
        return getPrefix() + getSplitItem() + API_KEY + getSplitItem() + keyId;
    }
}

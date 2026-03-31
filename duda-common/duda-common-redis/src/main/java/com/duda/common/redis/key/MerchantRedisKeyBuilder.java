package com.duda.common.redis.key;

import com.duda.common.redis.RedisKeyBuilder;
import org.springframework.context.annotation.Configuration;

/**
 * 商户服务 Redis Key 构建器
 *
 * 使用方法：
 * <pre>
 * &#64;Autowired
 * private MerchantRedisKeyBuilder redisKeyBuilder;
 *
 * // 构建商户信息缓存key
 * String key = redisKeyBuilder.buildMerchantInfoKey(merchantId);
 * redisTemplate.opsForValue().set(key, merchantDTO);
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Configuration
public class MerchantRedisKeyBuilder extends RedisKeyBuilder {

    private static final String MERCHANT = "merchant";
    private static final String INFO = "info";
    private static final String CODE = "code";
    private static final String LIST = "list";
    private static final String COUNT = "count";
    private static final String USER = "user";

    /**
     * 构建商户信息缓存key
     * 格式: duda:merchant:info:{merchantId}
     */
    public String buildMerchantInfoKey(Long merchantId) {
        return getPrefix() + getSplitItem() + MERCHANT + getSplitItem() + INFO + getSplitItem() + merchantId;
    }

    /**
     * 构建商户编码缓存key
     * 格式: duda:merchant:code:{tenantId}:{merchantCode}
     */
    public String buildMerchantCodeKey(Long tenantId, String merchantCode) {
        return getPrefix() + getSplitItem() + MERCHANT + getSplitItem() + CODE + getSplitItem() + tenantId + getSplitItem() + merchantCode;
    }

    /**
     * 构建租户商户列表缓存key
     * 格式: duda:merchant:list:tenant:{tenantId}:{status}
     */
    public String buildMerchantListKey(Long tenantId, String status) {
        return getPrefix() + getSplitItem() + MERCHANT + getSplitItem() + LIST + getSplitItem() + "tenant" + getSplitItem() + tenantId + getSplitItem() + status;
    }

    /**
     * 构建租户商户统计缓存key
     * 格式: duda:merchant:count:tenant:{tenantId}
     */
    public String buildMerchantCountKey(Long tenantId) {
        return getPrefix() + getSplitItem() + MERCHANT + getSplitItem() + COUNT + getSplitItem() + "tenant" + getSplitItem() + tenantId;
    }

    /**
     * 构建商户用户列表缓存key
     * 格式: duda:merchant:user:list:{merchantId}
     */
    public String buildMerchantUserListKey(Long merchantId) {
        return getPrefix() + getSplitItem() + MERCHANT + getSplitItem() + USER + getSplitItem() + "list" + getSplitItem() + merchantId;
    }

    /**
     * 构建商户用户统计缓存key
     * 格式: duda:merchant:user:count:{merchantId}
     */
    public String buildMerchantUserCountKey(Long merchantId) {
        return getPrefix() + getSplitItem() + MERCHANT + getSplitItem() + USER + getSplitItem() + "count" + getSplitItem() + merchantId;
    }

    /**
     * 构建商户用户关系缓存key
     * 格式: duda:merchant:user:{merchantId}:{userId}
     */
    public String buildMerchantUserKey(Long merchantId, Long userId) {
        return getPrefix() + getSplitItem() + MERCHANT + getSplitItem() + USER + getSplitItem() + merchantId + getSplitItem() + userId;
    }

    /**
     * 构建平台用户的商户列表缓存key
     * 格式: duda:merchant:user:platform:{platformUserId}
     */
    public String buildPlatformUserMerchantsKey(Long platformUserId) {
        return getPrefix() + getSplitItem() + MERCHANT + getSplitItem() + USER + getSplitItem() + "platform" + getSplitItem() + platformUserId;
    }
}

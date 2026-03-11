package com.duda.common.redis.key;

import com.duda.common.redis.RedisKeyBuilder;
import org.springframework.context.annotation.Configuration;

/**
 * 认证服务 Redis Key 构建器
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Configuration
public class AuthRedisKeyBuilder extends RedisKeyBuilder {

    private static final String AUTH = "auth";
    private static final String TOKEN = "token";
    private static final String CAPTCHA = "captcha";
    private static final String LOGIN_LIMIT = "login_limit";
    private static final String BLACKLIST = "blacklist";

    /**
     * 构建Token缓存key
     * 格式: duda:auth:token:{token}
     */
    public String buildTokenKey(String token) {
        return getPrefix() + getSplitItem() + AUTH + getSplitItem() + TOKEN + getSplitItem() + token;
    }

    /**
     * 构建验证码key
     * 格式: duda:auth:captcha:{uuid}
     */
    public String buildCaptchaKey(String uuid) {
        return getPrefix() + getSplitItem() + AUTH + getSplitItem() + CAPTCHA + getSplitItem() + uuid;
    }

    /**
     * 构建登录限制key（防刷）
     * 格式: duda:auth:login_limit:{ip}:{minute}
     */
    public String buildLoginLimitKey(String ip, String minute) {
        return getPrefix() + getSplitItem() + AUTH + getSplitItem() + LOGIN_LIMIT + getSplitItem() + ip + getSplitItem() + minute;
    }

    /**
     * 构建Token黑名单key
     * 格式: duda:auth:blacklist:{token}
     */
    public String buildTokenBlacklistKey(String token) {
        return getPrefix() + getSplitItem() + AUTH + getSplitItem() + BLACKLIST + getSplitItem() + token;
    }
}

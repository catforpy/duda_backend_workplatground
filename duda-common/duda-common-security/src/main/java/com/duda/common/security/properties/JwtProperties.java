package com.duda.common.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Component
@ConfigurationProperties(prefix = "jwt.config")
public class JwtProperties {

    /**
     * 密钥（建议生产环境从环境变量读取）
     */
    private String secret = "duda-nexus-jwt-secret-key-2026-must-be-at-least-256-bits-long-for-hs256-algorithm";

    /**
     * Access Token有效期（秒），默认15分钟
     */
    private Long accessTokenExpiration = 900L;

    /**
     * Refresh Token有效期（秒），默认7天
     */
    private Long refreshTokenExpiration = 604800L;

    /**
     * Token前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Token在Header中的key
     */
    private String headerKey = "Authorization";

    /**
     * Refresh Token在Redis中的前缀
     */
    private String refreshTokenPrefix = "auth:refresh:";

    /**
     * Access Token黑名单前缀
     */
    private String tokenBlacklistPrefix = "auth:blacklist:";

    // Getters and Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(Long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(Long refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getHeaderKey() {
        return headerKey;
    }

    public void setHeaderKey(String headerKey) {
        this.headerKey = headerKey;
    }

    public String getRefreshTokenPrefix() {
        return refreshTokenPrefix;
    }

    public void setRefreshTokenPrefix(String refreshTokenPrefix) {
        this.refreshTokenPrefix = refreshTokenPrefix;
    }

    public String getTokenBlacklistPrefix() {
        return tokenBlacklistPrefix;
    }

    public void setTokenBlacklistPrefix(String tokenBlacklistPrefix) {
        this.tokenBlacklistPrefix = tokenBlacklistPrefix;
    }
}

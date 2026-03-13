package com.duda.common.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性 - 支持不同用户类型
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * 通用Token配置
     */
    private Common common = new Common();

    /**
     * 不同用户类型的专属配置
     */
    private UserType userType = new UserType();

    // ==================== 根据用户类型获取配置 ====================

    /**
     * 获取指定用户类型的密钥
     *
     * @param userType 用户类型
     * @return 密钥
     */
    public String getUserTypeSecret(String userType) {
        switch (userType) {
            case "platform-admin":
                return this.userType.getPlatformAdmin().getSecret();
            case "service_provider":
                return this.userType.getServiceProvider().getSecret();
            case "platform_account":
                return this.userType.getPlatformAccount().getSecret();
            case "backend_admin":
                return this.userType.getBackendAdmin().getSecret();
            default:
                return this.userType.getPlatformAdmin().getSecret();
        }
    }

    /**
     * 获取指定用户类型的Access Token过期时间
     *
     * @param userType 用户类型
     * @return 过期时间（秒）
     */
    public Long getUserTypeAccessTokenExpiration(String userType) {
        switch (userType) {
            case "platform-admin":
                return this.userType.getPlatformAdmin().getAccessExpiration();
            case "service_provider":
                return this.userType.getServiceProvider().getAccessExpiration();
            case "platform_account":
                return this.userType.getPlatformAccount().getAccessExpiration();
            case "backend_admin":
                return this.userType.getBackendAdmin().getAccessExpiration();
            default:
                return 900L; // 默认15分钟
        }
    }

    /**
     * 获取指定用户类型的Refresh Token过期时间
     *
     * @param userType 用户类型
     * @return 过期时间（秒）
     */
    public Long getUserTypeRefreshTokenExpiration(String userType) {
        switch (userType) {
            case "platform-admin":
                return this.userType.getPlatformAdmin().getRefreshExpiration();
            case "service_provider":
                return this.userType.getServiceProvider().getRefreshExpiration();
            case "platform_account":
                return this.userType.getPlatformAccount().getRefreshExpiration();
            case "backend_admin":
                return this.userType.getBackendAdmin().getRefreshExpiration();
            default:
                return 604800L; // 默认7天
        }
    }

    // ==================== 便捷方法 - 访问Common配置 ====================

    public String getTokenPrefix() {
        return common.getTokenPrefix();
    }

    public String getHeaderKey() {
        return common.getHeaderKey();
    }

    public String getRefreshTokenPrefix() {
        return common.getRefreshTokenPrefix();
    }

    public String getTokenBlacklistPrefix() {
        return common.getTokenBlacklistPrefix();
    }

    // ==================== Getters and Setters ====================

    public Common getCommon() {
        return common;
    }

    public void setCommon(Common common) {
        this.common = common;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    // ==================== 嵌套类：Common ====================

    /**
     * 通用Token配置
     */
    public static class Common {
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

    // ==================== 嵌套类：UserType ====================

    /**
     * 不同用户类型的专属配置
     */
    public static class UserType {
        /**
         * 平台管理员配置
         */
        private PlatformAdmin platformAdmin = new PlatformAdmin();

        /**
         * 服务商配置
         */
        private ServiceProvider serviceProvider = new ServiceProvider();

        /**
         * 平台账号配置
         */
        private PlatformAccount platformAccount = new PlatformAccount();

        /**
         * 后台管理员配置
         */
        private BackendAdmin backendAdmin = new BackendAdmin();

        public PlatformAdmin getPlatformAdmin() {
            return platformAdmin;
        }

        public void setPlatformAdmin(PlatformAdmin platformAdmin) {
            this.platformAdmin = platformAdmin;
        }

        public ServiceProvider getServiceProvider() {
            return serviceProvider;
        }

        public void setServiceProvider(ServiceProvider serviceProvider) {
            this.serviceProvider = serviceProvider;
        }

        public PlatformAccount getPlatformAccount() {
            return platformAccount;
        }

        public void setPlatformAccount(PlatformAccount platformAccount) {
            this.platformAccount = platformAccount;
        }

        public BackendAdmin getBackendAdmin() {
            return backendAdmin;
        }

        public void setBackendAdmin(BackendAdmin backendAdmin) {
            this.backendAdmin = backendAdmin;
        }
    }

    // ==================== 平台管理员配置 ====================

    /**
     * 平台管理员配置
     */
    public static class PlatformAdmin {
        /**
         * 密钥
         */
        private String secret = "duda-platform-admin-secret-key-2026-must-be-at-least-256-bits-for-hs256";

        /**
         * Access Token过期时间（秒），默认15分钟
         */
        private Long accessExpiration = 900L;

        /**
         * Refresh Token过期时间（秒），默认7天
         */
        private Long refreshExpiration = 604800L;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Long getAccessExpiration() {
            return accessExpiration;
        }

        public void setAccessExpiration(Long accessExpiration) {
            this.accessExpiration = accessExpiration;
        }

        public Long getRefreshExpiration() {
            return refreshExpiration;
        }

        public void setRefreshExpiration(Long refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
        }
    }

    // ==================== 服务商配置 ====================

    /**
     * 服务商配置
     */
    public static class ServiceProvider {
        /**
         * 密钥
         */
        private String secret = "duda-service-provider-secret-key-2026-must-be-at-least-256-bits-for-hs256";

        /**
         * Access Token过期时间（秒），默认2小时
         */
        private Long accessExpiration = 7200L;

        /**
         * Refresh Token过期时间（秒），默认30天
         */
        private Long refreshExpiration = 2592000L;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Long getAccessExpiration() {
            return accessExpiration;
        }

        public void setAccessExpiration(Long accessExpiration) {
            this.accessExpiration = accessExpiration;
        }

        public Long getRefreshExpiration() {
            return refreshExpiration;
        }

        public void setRefreshExpiration(Long refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
        }
    }

    // ==================== 平台账号配置 ====================

    /**
     * 平台账号配置
     */
    public static class PlatformAccount {
        /**
         * 密钥
         */
        private String secret = "duda-platform-account-secret-key-2026-must-be-at-least-256-bits-for-hs256";

        /**
         * Access Token过期时间（秒），默认7天
         */
        private Long accessExpiration = 604800L;

        /**
         * Refresh Token过期时间（秒），默认60天
         */
        private Long refreshExpiration = 5184000L;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Long getAccessExpiration() {
            return accessExpiration;
        }

        public void setAccessExpiration(Long accessExpiration) {
            this.accessExpiration = accessExpiration;
        }

        public Long getRefreshExpiration() {
            return refreshExpiration;
        }

        public void setRefreshExpiration(Long refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
        }
    }

    // ==================== 后台管理员配置 ====================

    /**
     * 后台管理员配置
     */
    public static class BackendAdmin {
        /**
         * 密钥
         */
        private String secret = "duda-backend-admin-secret-key-2026-must-be-at-least-256-bits-for-hs256";

        /**
         * Access Token过期时间（秒），默认30分钟
         */
        private Long accessExpiration = 1800L;

        /**
         * Refresh Token过期时间（秒），默认7天
         */
        private Long refreshExpiration = 604800L;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Long getAccessExpiration() {
            return accessExpiration;
        }

        public void setAccessExpiration(Long accessExpiration) {
            this.accessExpiration = accessExpiration;
        }

        public Long getRefreshExpiration() {
            return refreshExpiration;
        }

        public void setRefreshExpiration(Long refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
        }
    }
}

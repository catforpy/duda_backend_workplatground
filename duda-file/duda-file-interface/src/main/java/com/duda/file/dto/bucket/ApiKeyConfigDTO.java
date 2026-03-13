package com.duda.file.dto.bucket;

import com.duda.file.enums.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API密钥配置DTO
 * 用于存储云存储服务的访问凭证
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyConfigDTO {

    /**
     * 存储类型
     */
    private StorageType storageType;

    /**
     * 访问密钥ID (AccessKey ID)
     */
    private String accessKeyId;

    /**
     * 访问密钥Secret (AccessKey Secret)
     * 注意：传输和存储时应加密
     */
    private String accessKeySecret;

    /**
     * 安全令牌 (Security Token)
     * 使用STS临时凭证时必填
     */
    private String securityToken;

    /**
     * 访问域名 (Endpoint)
     * 例如: oss-cn-hangzhou.aliyuncs.com
     */
    private String endpoint;

    /**
     * 区域 (Region)
     * 例如: cn-hangzhou
     */
    private String region;

    /**
     * 是否为内部Endpoint
     */
    private Boolean internalEndpoint;

    /**
     * 应用ID (用于某些云厂商)
     * 例如: 腾讯云的AppId
     */
    private String appId;

    /**
     * 会话令牌名称 (KMS使用)
     */
    private String sessionTokenName;

    /**
     * 密钥是否已加密
     */
    private Boolean encrypted;

    /**
     * 密钥过期时间 (时间戳)
     * 临时凭证时使用
     */
    private Long expirationTime;

    /**
     * 扩展配置 (JSON格式)
     * 用于存储特定云厂商的额外配置
     */
    private String extraConfig;

    /**
     * 检查密钥是否过期
     */
    public boolean isExpired() {
        if (expirationTime == null) {
            return false;
        }
        return System.currentTimeMillis() > expirationTime;
    }

    /**
     * 检查是否为临时凭证
     */
    public boolean isTemporaryCredentials() {
        return securityToken != null && !securityToken.isEmpty();
    }

    /**
     * 获取完整的Endpoint URL
     */
    public String getFullEndpoint() {
        if (endpoint == null || endpoint.isEmpty()) {
            return null;
        }
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        // 默认使用HTTPS
        return "https://" + endpoint;
    }
}

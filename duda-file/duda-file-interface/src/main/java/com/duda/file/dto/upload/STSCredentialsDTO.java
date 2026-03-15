package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * STS临时凭证DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class STSCredentialsDTO implements Serializable {

    /**
     * 访问密钥ID
     */
    private String accessKeyId;

    /**
     * 访问密钥Secret
     */
    private String accessKeySecret;

    /**
     * 安全令牌
     */
    private String securityToken;

    /**
     * 过期时间
     */
    private LocalDateTime expiration;

    /**
     * 有效期(秒)
     */
    private Long durationSeconds;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 角色ARN
     */
    private String roleArn;

    /**
     * 角色会话名
     */
    private String roleSessionName;

    /**
     * 策略文档
     */
    private String policy;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 临时凭证可用的存储空间列表
     * 如果为空表示可以使用所有存储空间
     */
    private List<String> allowedBuckets;

    /**
     * 临时凭证可用的前缀列表
     * 如果为空表示可以使用所有前缀
     */
    private List<String> allowedPrefixes;

    /**
     * 临时凭证是否只能用于HTTPS
     */
    private Boolean httpsOnly;

    /**
     * 临时凭证的权限类型
     * - ReadOnly: 只读
     * - ReadWrite: 读写
     * - FullControl: 完全控制
     */
    private String permissionType;

    /**
     * 扩展信息
     */
    private java.util.Map<String, Object> extra;

    /**
     * 判断凭证是否已过期
     */
    public boolean isExpired() {
        if (expiration == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiration);
    }

    /**
     * 判断凭证是否即将过期(1小时内)
     */
    public boolean isExpiringSoon() {
        if (expiration == null) {
            return false;
        }
        return LocalDateTime.now().plusHours(1).isAfter(expiration);
    }

    /**
     * 获取剩余有效时间(秒)
     */
    public Long getRemainingSeconds() {
        if (expiration == null) {
            return null;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiration).getSeconds();
    }

    /**
     * 检查是否有权限访问指定Bucket
     */
    public boolean hasPermission(String bucketName) {
        if (allowedBuckets == null || allowedBuckets.isEmpty()) {
            return true;
        }
        return allowedBuckets.contains(bucketName);
    }

    /**
     * 检查是否有权限访问指定前缀
     */
    public boolean hasPermission(String bucketName, String objectKey) {
        if (!hasPermission(bucketName)) {
            return false;
        }
        if (allowedPrefixes == null || allowedPrefixes.isEmpty()) {
            return true;
        }
        for (String prefix : allowedPrefixes) {
            if (objectKey.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}

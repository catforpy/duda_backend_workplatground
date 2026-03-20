package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Bucket配置实体
 * 对应bucket_config表
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * Bucket名称（全局唯一）
     */
    private String bucketName;

    /**
     * Bucket显示名称（用户自定义）
     */
    private String bucketDisplayName;

    /**
     * 存储类型：aliyun-oss/tencent-cos/qiniu-kodo/minio
     */
    private String storageType;

    /**
     * 地域编码：cn-hangzhou/ap-guangzhou等
     */
    private String region;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 关联的API密钥ID (关联 user_api_keys.id)
     */
    private Long apiKeyId;

    /**
     * 用户类型：platform-admin/service_provider/platform_account
     */
    private String userType;

    /**
     * 租户ID（如果是多租户场景）
     */
    private Long tenantId;

    /**
     * 存储类型：STANDARD/IA/ARCHIVE/COLD_ARCHIVE
     */
    private String storageClass;

    /**
     * 数据冗余类型：LRS/ZRS
     */
    private String dataRedundancyType;

    /**
     * 权限类型：PRIVATE/PUBLIC_READ/PUBLIC_READ_WRITE
     */
    private String aclType;

    /**
     * 最大文件大小（字节，默认10GB）
     */
    private Long maxFileSize;

    /**
     * 最大文件数量
     */
    private Integer maxFileCount;

    /**
     * 最大存储容量（字节）
     */
    private Long maxStorageSize;

    /**
     * 允许的文件类型（逗号分隔）
     */
    private String allowedFileTypes;

    /**
     * 禁止的文件类型（逗号分隔）
     */
    private String blockedFileTypes;

    /**
     * 自定义域名
     */
    private String domainName;

    /**
     * 是否启用CDN：0-否 1-是
     */
    private Boolean cdnEnabled;

    /**
     * CDN域名
     */
    private String cdnDomain;

    /**
     * 是否开启版本控制：0-否 1-是
     */
    private Boolean versioningEnabled;

    /**
     * 是否开启CORS：0-否 1-是
     */
    private Boolean corsEnabled;

    /**
     * 是否启用水印：0-否 1-是
     */
    private Boolean watermarkEnabled;

    /**
     * 是否开启加密：0-否 1-是
     */
    private Boolean encryptionEnabled;

    /**
     * 是否开启生命周期：0-否 1-是
     */
    private Boolean lifecycleEnabled;

    /**
     * 是否开启传输加速：0-否 1-是
     */
    private Boolean transferAccelerationEnabled;

    /**
     * 当前文件数量
     */
    private Integer currentFileCount;

    /**
     * 当前已用存储（字节）
     */
    private Long currentStorageSize;

    /**
     * 存储配额（字节）
     */
    private Long storageUsedQuota;

    /**
     * AccessKey ID（AES加密）
     */
    private String accessKeyId;

    /**
     * AccessKey Secret（AES加密）
     */
    private String accessKeySecret;

    /**
     * Secret Key（AES加密）
     */
    private String secretKey;

    /**
     * 访问端点
     */
    private String endpoint;

    /**
     * STS角色ARN
     */
    private String stsRoleArn;

    /**
     * STS外部ID
     */
    private String stsExternalId;

    /**
     * 状态: ACTIVE-正常, DISABLED-禁用, DELETED-已删除
     */
    private String status;

    /**
     * 是否自动续费：0-否 1-是
     */
    private Boolean autoRenewEnabled;

    /**
     * 标签（JSON格式）
     */
    private String tags;

    /**
     * Bucket分类
     */
    private String category;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建者用户ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新者用户ID
     */
    private Long updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 删除时间
     */
    private LocalDateTime deletedTime;

    /**
     * 是否删除：0-否 1-是
     */
    private Boolean isDeleted;

    // ==================== OSS高级配置字段（JSON格式，与数据库一致） ====================

    /**
     * 生命周期规则配置（JSON格式）
     * 数据库字段：lifecycle_config
     */
    private String lifecycleConfig;

    /**
     * CORS规则配置（JSON格式）
     * 数据库字段：cors_config
     */
    private String corsConfig;

    /**
     * 防盗链配置（JSON格式）
     * 数据库字段：referer_config
     */
    private String refererConfig;

    /**
     * Bucket策略文档（JSON格式）
     * 数据库字段：bucket_policy
     */
    private String bucketPolicy;

    /**
     * 跨区域复制配置（JSON格式）
     * 数据库字段：replication_config
     */
    private String replicationConfig;

    /**
     * 访问跟踪配置（JSON格式）
     * 数据库字段：access_monitor_config
     */
    private String accessMonitorConfig;

    /**
     * 存储空间清单配置（JSON格式）
     * 数据库字段：inventory_config
     */
    private String inventoryConfig;

    /**
     * 静态网站托管配置（JSON格式）
     * 数据库字段：website_config
     */
    private String websiteConfig;

    /**
     * 日志转存配置（JSON格式）
     * 数据库字段：logging_config
     */
    private String loggingConfig;

    /**
     * 合规保留策略配置（JSON格式）
     * 数据库字段：worm_config
     */
    private String wormConfig;
}

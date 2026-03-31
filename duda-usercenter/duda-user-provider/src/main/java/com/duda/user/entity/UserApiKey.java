package com.duda.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户API密钥实体类
 * 对应数据库表：user_api_keys
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_api_keys")
public class UserApiKey implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 密钥名称（用户自定义）
     */
    private String keyName;

    /**
     * 密钥类型：aliyun_oss/tencent_cos/qiniu_kodo/minio
     */
    private String keyType;

    /**
     * AccessKey ID（AES加密存储）
     */
    private String accessKeyId;

    /**
     * AccessKey Secret（AES加密存储）
     */
    private String accessKeySecret;

    /**
     * STS角色ARN（可选）
     */
    private String stsRoleArn;

    /**
     * STS外部ID（可选）
     */
    private String stsExternalId;

    /**
     * 默认区域
     */
    private String region;

    /**
     * 是否为默认密钥：0-否 1-是
     */
    private Integer isDefault;

    /**
     * 是否激活：0-禁用 1-启用
     */
    private Integer isActive;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedTime;

    /**
     * 最后验证时间
     */
    private LocalDateTime lastVerifiedTime;

    /**
     * 验证状态：pending/success/failed
     */
    private String verificationStatus;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新人
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
    private Integer isDeleted;
}

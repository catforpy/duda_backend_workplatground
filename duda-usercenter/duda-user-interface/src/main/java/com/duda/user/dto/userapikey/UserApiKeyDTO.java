package com.duda.user.dto.userapikey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户API密钥DTO
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户API密钥信息")
public class UserApiKeyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "密钥ID")
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 密钥名称
     */
    @Schema(description = "密钥名称")
    private String keyName;

    /**
     * 密钥类型
     */
    @Schema(description = "密钥类型")
    private String keyType;

    /**
     * AccessKey ID（加密后的密文）
     * 注意：对外接口返回时应隐藏
     */
    @Schema(description = "AccessKey ID（加密）")
    private String accessKeyId;

    /**
     * AccessKey Secret（加密后的密文）
     * 注意：对外接口返回时应隐藏
     */
    @Schema(description = "AccessKey Secret（加密）")
    private String accessKeySecret;

    /**
     * AccessKey ID（明文）
     * 注意：仅内部服务调用时返回
     */
    @Schema(description = "AccessKey ID（明文，内部使用）")
    private String plainAccessKeyId;

    /**
     * AccessKey Secret（明文）
     * 注意：仅内部服务调用时返回
     */
    @Schema(description = "AccessKey Secret（明文，内部使用）")
    private String plainAccessKeySecret;

    /**
     * STS角色ARN
     */
    @Schema(description = "STS角色ARN")
    private String stsRoleArn;

    /**
     * STS外部ID
     */
    @Schema(description = "STS外部ID")
    private String stsExternalId;

    /**
     * 默认区域
     */
    @Schema(description = "默认区域")
    private String region;

    /**
     * 是否为默认密钥
     */
    @Schema(description = "是否为默认密钥")
    private Boolean isDefault;

    /**
     * 是否激活
     */
    @Schema(description = "是否激活")
    private Boolean isActive;

    /**
     * 最后使用时间
     */
    @Schema(description = "最后使用时间")
    private LocalDateTime lastUsedTime;

    /**
     * 最后验证时间
     */
    @Schema(description = "最后验证时间")
    private LocalDateTime lastVerifiedTime;

    /**
     * 验证状态
     */
    @Schema(description = "验证状态")
    private String verificationStatus;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}

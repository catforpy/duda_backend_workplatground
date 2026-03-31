package com.duda.user.dto.userapikey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 添加用户API密钥请求DTO（简化版）
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "添加用户API密钥请求")
public class AddUserApiKeyReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户ID（必填）
     */
    @Schema(description = "租户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "租户ID不能为空")
    private Long tenantId;

    /**
     * 用户名（必填）
     */
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "testuser")
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * AccessKey ID（明文，必填）
     */
    @Schema(description = "AccessKey ID（明文）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "AccessKey ID不能为空")
    private String accessKeyId;

    /**
     * AccessKey Secret（明文，必填）
     */
    @Schema(description = "AccessKey Secret（明文）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "AccessKey Secret不能为空")
    private String accessKeySecret;

    // ==================== 以下字段暂时不必填，使用默认值 ====================

    /**
     * 密钥名称（可选，默认：用户名-云存储密钥）
     */
    @Schema(description = "密钥名称（可选，默认自动生成）")
    private String keyName;

    /**
     * 密钥类型（可选，默认：aliyun_oss）
     */
    @Schema(description = "密钥类型（可选，默认aliyun_oss）")
    private String keyType;

    /**
     * STS角色ARN（可选）
     */
    @Schema(description = "STS角色ARN（可选）")
    private String stsRoleArn;

    /**
     * STS外部ID（可选）
     */
    @Schema(description = "STS外部ID（可选）")
    private String stsExternalId;

    /**
     * 默认区域（可选，默认：cn-hangzhou）
     */
    @Schema(description = "默认区域（可选，默认cn-hangzhou）")
    private String region;

    /**
     * 描述（可选）
     */
    @Schema(description = "描述（可选）")
    private String description;
}

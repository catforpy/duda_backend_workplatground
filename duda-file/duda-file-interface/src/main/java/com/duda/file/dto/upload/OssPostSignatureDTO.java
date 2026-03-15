package com.duda.file.dto.upload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * OSS POST签名响应DTO
 *
 * @author DudaNexus
 * @since 2025-03-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OSS POST签名响应")
public class OssPostSignatureDTO implements Serializable {

    @Schema(description = "签名版本", example = "OSS4-HMAC-SHA256")
    private String version;

    @Schema(description = "上传策略（Base64编码）")
    private String policy;

    @Schema(description = "OSS凭证字符串")
    private String xOssCredential;

    @Schema(description = "OSS日期时间", example = "20250315T115124Z")
    private String xOssDate;

    @Schema(description = "签名值")
    private String signature;

    @Schema(description = "STS安全令牌（如果使用STS）")
    private String securityToken;

    @Schema(description = "上传目录前缀", example = "uploads/")
    private String dir;

    @Schema(description = "OSS服务地址", example = "https://bucket.oss-cn-hangzhou.aliyuncs.com")
    private String host;

    @Schema(description = "过期时间（秒）", example = "3600")
    private Long expireTime;
}

package com.duda.file.dto;

import lombok.Builder;
import lombok.Data;

/**
 * STS凭证（统一DTO）
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Data
@Builder
public class STSCredential {

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * Security Token
     */
    private String securityToken;

    /**
     * 过期时间戳
     */
    private Long expiration;

    /**
     * 区域
     */
    private String region;
}

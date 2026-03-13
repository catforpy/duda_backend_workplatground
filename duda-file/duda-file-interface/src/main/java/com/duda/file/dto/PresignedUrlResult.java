package com.duda.file.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 预签名URL结果（统一DTO）
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Data
@Builder
public class PresignedUrlResult {

    /**
     * 预签名URL
     */
    private String url;

    /**
     * 过期时间戳
     */
    private Long expiration;
}

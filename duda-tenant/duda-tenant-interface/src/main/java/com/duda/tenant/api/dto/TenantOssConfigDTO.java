package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OSS配置表DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class TenantOssConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private String ossProvider;
    private String bucketName;
    private String endpoint;
    private String status;
    private LocalDateTime createdAt;
}

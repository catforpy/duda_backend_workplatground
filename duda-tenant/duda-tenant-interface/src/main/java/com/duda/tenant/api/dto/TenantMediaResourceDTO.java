package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 媒体资源表DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class TenantMediaResourceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private String resourceType;
    private String resourceUrl;
    private String fileName;
    private Long fileSize;
    private String status;
    private LocalDateTime createdAt;
}

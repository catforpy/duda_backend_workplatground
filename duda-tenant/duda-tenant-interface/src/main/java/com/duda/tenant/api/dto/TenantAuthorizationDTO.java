package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 授权管理表DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class TenantAuthorizationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private Long merchantId;
    private String authorizationCode;
    private String authorizationType;
    private String permissions;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private LocalDateTime createdAt;
}

package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 合作管理表DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class TenantCooperationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private Long operatorTenantId;
    private String cooperationCode;
    private String cooperationType;
    private LocalDateTime startDate;
    private String status;
    private LocalDateTime createdAt;
}

package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 行业模块表DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class TenantIndustryModuleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private String industryType;
    private String moduleName;
    private Boolean isEnabled;
    private LocalDateTime createdAt;
}

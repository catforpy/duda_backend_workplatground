package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提成记录表DTO（已废弃）
 *
 * @author Claude Code
 * @since 2026-03-30
 * @deprecated 使用TenantCommissionDetailDTO替代
 */
@Data
@Deprecated
public class TenantCommissionRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long salesAgentId;
    private Long tenantId;
    private String commissionType;
    private BigDecimal commissionAmount;
    private String status;
    private LocalDateTime createdAt;
}

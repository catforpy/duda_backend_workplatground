package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账周期表DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class TenantSettlementPeriodDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private Long merchantId;
    private String periodNo;
    private String periodType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer orderCount;
    private BigDecimal totalAmount;
    private BigDecimal platformFee;
    private BigDecimal tenantFee;
    private BigDecimal commissionFee;
    private BigDecimal otherFee;
    private BigDecimal merchantSettleAmount;
    private String status;
    private LocalDateTime settledAt;
    private Long settledBy;
    private LocalDateTime transferredAt;
    private String transferFailReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

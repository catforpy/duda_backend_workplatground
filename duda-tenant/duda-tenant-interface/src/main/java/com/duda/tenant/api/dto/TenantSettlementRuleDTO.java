package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算规则表DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class TenantSettlementRuleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private Long merchantId;
    private String settlementCycle;
    private Integer settlementDay;
    private String settlementTime;
    private BigDecimal platformFeeRate;
    private BigDecimal tenantFeeRate;
    private BigDecimal commissionFeeRate;
    private BigDecimal reserveFeeRate;
    private BigDecimal otherFeeRate;
    private BigDecimal minSettlementAmount;
    private Boolean autoSettle;
    private BigDecimal autoSettleThreshold;
    private String status;
    private LocalDateTime effectiveDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

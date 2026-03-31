package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分佣明细表DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class TenantCommissionDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private Long orderId;
    private String orderNo;
    private Long settlementPeriodId;
    private BigDecimal orderAmount;
    private LocalDateTime orderTime;
    private Long commissionRuleId;
    private String ruleCode;
    private String ruleName;
    private String commissionType;
    private Long beneficiaryId;
    private String beneficiaryName;
    private BigDecimal commissionAmount;
    private BigDecimal commissionRate;
    private String status;
    private LocalDateTime settledAt;
    private String settledMethod;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账订单明细表DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class TenantSettlementOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long settlementPeriodId;
    private Long tenantId;
    private Long merchantId;
    private Long orderId;
    private String orderNo;
    private BigDecimal orderAmount;
    private BigDecimal platformFee;
    private BigDecimal tenantFee;
    private BigDecimal commissionFee;
    private BigDecimal merchantAmount;
    private LocalDateTime createdAt;
}

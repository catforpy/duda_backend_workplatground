package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算流水表DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class TenantSettlementRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long settlementPeriodId;
    private Long tenantId;
    private Long merchantId;
    private String transferNo;
    private String transferType;
    private BigDecimal transferAmount;
    private String transferAccount;
    private String wechatBatchId;
    private String wechatDetailId;
    private String status;
    private LocalDateTime transferInitiatedAt;
    private LocalDateTime transferCompletedAt;
    private String failReason;
    private Integer retryCount;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

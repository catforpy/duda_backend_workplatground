package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算流水表实体
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
@TableName("tenant_settlement_records")
public class TenantSettlementRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
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

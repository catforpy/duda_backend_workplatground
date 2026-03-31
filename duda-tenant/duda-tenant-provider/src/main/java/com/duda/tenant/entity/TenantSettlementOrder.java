package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账订单明细表实体
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
@TableName("tenant_settlement_orders")
public class TenantSettlementOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
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

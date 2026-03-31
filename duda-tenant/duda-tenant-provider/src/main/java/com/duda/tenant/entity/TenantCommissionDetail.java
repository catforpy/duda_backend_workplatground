package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分佣明细表实体
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
@TableName("tenant_commission_details")
public class TenantCommissionDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
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

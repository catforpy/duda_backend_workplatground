package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分佣规则表实体
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
@TableName("tenant_commission_rules")
public class TenantCommissionRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;
    private String ruleCode;
    private String ruleName;
    private String ruleType;
    private String commissionType;
    private Long beneficiaryId;
    private String beneficiaryName;
    private String beneficiaryAccount;
    private BigDecimal commissionValue;
    private String commissionValueType;
    private String tierConfig;
    private Integer priority;
    private String conditionType;
    private String conditionConfig;
    private BigDecimal minOrderAmount;
    private BigDecimal maxCommissionAmount;
    private LocalDateTime effectiveStart;
    private LocalDateTime effectiveEnd;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

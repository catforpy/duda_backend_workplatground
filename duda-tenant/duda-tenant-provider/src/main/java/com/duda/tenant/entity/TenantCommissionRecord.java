package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提成记录表实体（已废弃）
 *
 * @author Claude Code
 * @since 2026-03-30
 * @deprecated 使用TenantCommissionDetail替代
 */
@Data
@TableName("tenant_commission_records")
@Deprecated
public class TenantCommissionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long salesAgentId;
    private Long tenantId;
    private String commissionType;
    private BigDecimal commissionAmount;
    private String status;
    private LocalDateTime createdAt;
}

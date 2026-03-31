package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算规则表实体
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
@TableName("tenant_settlement_rules")
public class TenantSettlementRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 小程序拥有者ID
     */
    private Long tenantId;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 对账周期：daily-日结/weekly-周结/monthly-月结
     */
    private String settlementCycle;

    /**
     * 结算日(月结时:1-31, 周结时:1-7代表周一到周日)
     */
    private Integer settlementDay;

    /**
     * 结算时间(默认凌晨2点)
     */
    private String settlementTime;

    /**
     * 平台抽成比例(5%)
     */
    private BigDecimal platformFeeRate;

    /**
     * 小程序A抽成比例(10%)
     */
    private BigDecimal tenantFeeRate;

    /**
     * 销售商提成比例(10%)
     */
    private BigDecimal commissionFeeRate;

    /**
     * 预留保证金比例
     */
    private BigDecimal reserveFeeRate;

    /**
     * 其他费用比例
     */
    private BigDecimal otherFeeRate;

    /**
     * 最低结算金额(低于此金额不结算)
     */
    private BigDecimal minSettlementAmount;

    /**
     * 是否自动结算(自动转账)
     */
    private Boolean autoSettle;

    /**
     * 自动结算阈值(低于此金额需手动确认)
     */
    private BigDecimal autoSettleThreshold;

    /**
     * 状态：active-激活/suspended-暂停
     */
    private String status;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

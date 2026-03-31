package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租户统计实体
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
@TableName("tenant_statistics")
public class TenantStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 统计小时（0-23），NULL表示全天统计
     */
    private Integer statHour;

    /**
     * 数据来源（auto-自动统计，manual-手动录入）
     */
    private String dataSource;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 用户数量
     */
    private Integer userCount;

    /**
     * 商户数量
     */
    private Integer merchantCount;

    /**
     * 小程序数量
     */
    private Integer miniProgramCount;

    /**
     * 已使用存储空间（字节）
     */
    private Long storageUsedSize;

    /**
     * API调用次数
     */
    private Integer apiCallCount;

    /**
     * 订单数量
     */
    private Integer orderCount;

    /**
     * 订单金额
     */
    private java.math.BigDecimal orderAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}

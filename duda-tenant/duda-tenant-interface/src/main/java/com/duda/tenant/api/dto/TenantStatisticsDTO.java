package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 租户统计DTO
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
public class TenantStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 统计日期
     */
    private LocalDate statisticsDate;

    /**
     * 用户总数
     */
    private Integer totalUsers;

    /**
     * 活跃用户数
     */
    private Integer activeUsers;

    /**
     * 新增用户数
     */
    private Integer newUsers;

    /**
     * 管理员数量
     */
    private Integer adminCount;

    /**
     * 已用存储空间（字节）
     */
    private Long usedStorageSize;

    /**
     * 存储使用率（百分比）
     */
    private Double storageUsageRate;

    /**
     * API调用总次数
     */
    private Integer totalApiCalls;

    /**
     * API成功调用次数
     */
    private Integer successApiCalls;

    /**
     * API失败调用次数
     */
    private Integer failedApiCalls;

    /**
     * 订单总数
     */
    private Integer totalOrders;

    /**
     * 成功订单数
     */
    private Integer successOrders;

    /**
     * 退款订单数
     */
    private Integer refundOrders;

    /**
     * 总收入（分）
     */
    private Long totalRevenue;
}

package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户订阅DTO
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Data
public class TenantSubscriptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订阅编号（如：SUB-20260331-001）
     */
    private String subscriptionCode;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 套餐ID
     */
    private Long packageId;

    /**
     * 订阅状态：ACTIVE-生效中/SUSPENDED-暂停/CANCELLED-取消/EXPIRED-过期/PENDING-待激活
     */
    private String subscriptionStatus;

    /**
     * 订阅开始时间
     */
    private LocalDateTime startTime;

    /**
     * 订阅到期时间（NULL表示永久）
     */
    private LocalDateTime endTime;

    /**
     * 是否自动续费：0-否 1-是
     */
    private Integer autoRenew;

    /**
     * 已续费次数
     */
    private Integer renewCount;

    /**
     * 最后续费时间
     */
    private LocalDateTime lastRenewTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消操作人
     */
    private Long cancelBy;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

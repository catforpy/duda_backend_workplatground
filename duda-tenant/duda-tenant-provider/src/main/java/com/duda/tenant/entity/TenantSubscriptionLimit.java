package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订阅限制条件Entity
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Data
@TableName("tenant_subscription_limits")
public class TenantSubscriptionLimit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订阅ID
     */
    private Long subscriptionId;

    /**
     * 限制类型（如：max_users/api_calls_per_day/storage_size等）
     */
    private String limitKey;

    /**
     * 限制值（支持数字、JSON数组、布尔值等）
     */
    private String limitValue;

    /**
     * 单位：day/month/year/gb/mb/times/boolean/json等
     */
    private String limitUnit;

    /**
     * 限制条件显示名称（如：最大用户数/API调用次数限制）
     */
    private String limitLabel;

    /**
     * 限制条件描述
     */
    private String limitDesc;

    /**
     * 当前已使用量
     */
    private Long currentUsed;

    /**
     * 当前使用百分比（0-100）
     */
    private BigDecimal currentUsedPercent;

    /**
     * 上次重置时间
     */
    private LocalDateTime lastResetTime;

    /**
     * 周期类型：daily-每日/monthly-每月/yearly-每年/lifetime-终身
     */
    private String periodType;

    /**
     * 当前周期开始时间
     */
    private LocalDateTime periodStart;

    /**
     * 当前周期结束时间
     */
    private LocalDateTime periodEnd;

    /**
     * 告警阈值（如：使用量达到80%时告警）
     */
    private Integer alertThreshold;

    /**
     * 是否已发送告警：0-否 1-是
     */
    private Integer alertSent;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

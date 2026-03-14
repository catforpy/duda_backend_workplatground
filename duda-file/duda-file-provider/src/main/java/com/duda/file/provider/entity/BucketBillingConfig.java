package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bucket费用配置实体
 * 对应bucket_billing_config表
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketBillingConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 计费周期：daily-日结/weekly-周结/monthly-月结
     */
    private String billingCycle;

    /**
     * 存储单价（元/GB/天）
     */
    private BigDecimal unitPrice;

    /**
     * 流量单价（元/GB）
     */
    private BigDecimal trafficUnitPrice;

    /**
     * 免费配额（字节）
     */
    private Long freeQuota;

    /**
     * 余额不足预警阈值（元）
     */
    private BigDecimal lowBalanceThreshold;

    /**
     * 配额预警阈值（百分比）
     */
    private Integer quotaWarningThreshold;

    /**
     * 是否允许透支：0-否 1-是
     */
    private Boolean overdraftAllowed;

    /**
     * 最大透支金额（元）
     */
    private BigDecimal maxOverdraft;

    /**
     * 是否启用通知：0-否 1-是
     */
    private Boolean notificationEnabled;

    /**
     * 通知邮箱
     */
    private String notificationEmail;

    /**
     * 通知手机号
     */
    private String notificationMobile;

    /**
     * Webhook通知URL
     */
    private String webhookUrl;

    /**
     * 账户余额（元）
     */
    private BigDecimal balance;

    /**
     * 状态：ACTIVE-正常/SUSPENDED-暂停/ARREARS-欠费
     */
    private String status;

    /**
     * 暂停原因
     */
    private String suspendReason;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private Long updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 是否删除：0-否 1-是
     */
    private Boolean isDeleted;

    /**
     * 判断余额是否不足
     */
    public boolean isLowBalance() {
        if (balance == null || lowBalanceThreshold == null) {
            return false;
        }
        return balance.compareTo(lowBalanceThreshold) < 0;
    }

    /**
     * 判断是否欠费
     */
    public boolean isArrears() {
        return "ARREARS".equals(status);
    }

    /**
     * 判断是否暂停
     */
    public boolean isSuspended() {
        return "SUSPENDED".equals(status);
    }
}

package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bucket费用配置DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketBillingConfigDTO {

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
     * 是否允许透支
     */
    private Boolean overdraftAllowed;

    /**
     * 最大透支金额（元）
     */
    private BigDecimal maxOverdraft;

    /**
     * 是否启用通知
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
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 是否余额不足
     */
    public Boolean getLowBalance() {
        if (balance == null || lowBalanceThreshold == null) {
            return false;
        }
        return balance.compareTo(lowBalanceThreshold) < 0;
    }

    /**
     * 是否欠费
     */
    public Boolean getArrears() {
        return "ARREARS".equals(status);
    }

    /**
     * 是否暂停
     */
    public Boolean getSuspended() {
        return "SUSPENDED".equals(status);
    }

    /**
     * 获取人类可读的余额
     */
    public String getHumanReadableBalance() {
        if (balance == null) {
            return "¥0.00";
        }
        return String.format("¥%.2f", balance);
    }
}

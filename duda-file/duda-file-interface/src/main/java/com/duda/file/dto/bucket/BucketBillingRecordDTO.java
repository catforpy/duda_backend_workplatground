package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bucket费用账单DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketBillingRecordDTO {

    /**
     * 账单ID
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
     * 计费周期：daily/weekly/monthly
     */
    private String billingCycle;

    /**
     * 周期开始日期
     */
    private LocalDate cycleStart;

    /**
     * 周期结束日期
     */
    private LocalDate cycleEnd;

    /**
     * 平均存储量（字节）
     */
    private Long avgStorageSize;

    /**
     * 总流量（字节）
     */
    private Long totalTraffic;

    /**
     * 总请求次数
     */
    private Integer totalRequests;

    /**
     * 文件数量
     */
    private Integer totalFiles;

    /**
     * 存储费用（元）
     */
    private BigDecimal storageFee;

    /**
     * 流量费用（元）
     */
    private BigDecimal trafficFee;

    /**
     * 请求费用（元）
     */
    private BigDecimal requestFee;

    /**
     * 其他费用（元）
     */
    private BigDecimal otherFee;

    /**
     * 优惠金额（元）
     */
    private BigDecimal discountFee;

    /**
     * 总费用（元）
     */
    private BigDecimal totalFee;

    /**
     * 支付状态：UNPAID-未支付/PAID-已支付/OVERDUE-逾期
     */
    private String paymentStatus;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 交易流水号
     */
    private String transactionId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 是否已支付
     */
    public Boolean getPaid() {
        return "PAID".equals(paymentStatus);
    }

    /**
     * 是否逾期
     */
    public Boolean getOverdue() {
        return "OVERDUE".equals(paymentStatus);
    }

    /**
     * 是否未支付
     */
    public Boolean getUnpaid() {
        return "UNPAID".equals(paymentStatus);
    }

    /**
     * 获取人类可读的总流量
     */
    public String getHumanReadableTotalTraffic() {
        if (totalTraffic == null) {
            return "unknown";
        }
        if (totalTraffic < 1024) {
            return totalTraffic + " B";
        } else if (totalTraffic < 1024 * 1024) {
            return String.format("%.2f KB", totalTraffic / 1024.0);
        } else if (totalTraffic < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", totalTraffic / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", totalTraffic / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 获取人类可读的总费用
     */
    public String getHumanReadableTotalFee() {
        if (totalFee == null) {
            return "¥0.00";
        }
        return String.format("¥%.2f", totalFee);
    }
}

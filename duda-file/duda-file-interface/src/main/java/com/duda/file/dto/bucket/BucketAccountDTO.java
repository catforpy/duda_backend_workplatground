package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bucket账户信息DTO
 * 对应bucket_account表
 *
 * @author duda
 * @date 2025-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketAccountDTO implements Serializable {

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
     * 当前余额（元）
     */
    private BigDecimal currentBalance;

    /**
     * 冻结余额（元）
     */
    private BigDecimal frozenBalance;

    /**
     * 可用余额（元）= 当前余额 - 冻结余额
     */
    private BigDecimal availableBalance;

    /**
     * 总充值金额（元）
     */
    private BigDecimal totalRecharge;

    /**
     * 总消费金额（元）
     */
    private BigDecimal totalConsumption;

    /**
     * 信用额度（元）
     */
    private BigDecimal creditLine;

    /**
     * 状态：ACTIVE-正常/FROZEN-冻结/CLOSED-关闭
     */
    private String status;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 租户ID
     */
    private Long tenantId;

    // ==================== 业务方法 ====================

    /**
     * 判断账户是否正常
     */
    public Boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * 判断账户是否冻结
     */
    public Boolean isFrozen() {
        return "FROZEN".equals(status);
    }

    /**
     * 判断账户是否已关闭
     */
    public Boolean isClosed() {
        return "CLOSED".equals(status);
    }

    /**
     * 获取人类可读的余额
     */
    public String getHumanReadableBalance() {
        if (availableBalance == null) {
            return "¥0.00";
        }
        return String.format("¥%.2f", availableBalance);
    }
}

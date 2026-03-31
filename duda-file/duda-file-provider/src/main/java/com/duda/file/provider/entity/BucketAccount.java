package com.duda.file.provider.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bucket账户余额实体
 * 对应bucket_account表
 *
 * @author duda
 * @date 2025-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("bucket_account")
public class BucketAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
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
    @Version
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
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * 判断账户是否冻结
     */
    public boolean isFrozen() {
        return "FROZEN".equals(status);
    }

    /**
     * 判断账户是否已关闭
     */
    public boolean isClosed() {
        return "CLOSED".equals(status);
    }

    /**
     * 判断余额是否充足
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        if (availableBalance == null || amount == null) {
            return false;
        }
        return availableBalance.compareTo(amount) >= 0;
    }

    /**
     * 计算账户总余额（可用余额 + 信用额度）
     */
    public BigDecimal getTotalBalance() {
        BigDecimal available = availableBalance != null ? availableBalance : BigDecimal.ZERO;
        BigDecimal credit = creditLine != null ? creditLine : BigDecimal.ZERO;
        return available.add(credit);
    }
}

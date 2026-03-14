package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户余额充值结果DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeBalanceResultDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 充值金额（元）
     */
    private BigDecimal amount;

    /**
     * 充值前余额（元）
     */
    private BigDecimal balanceBefore;

    /**
     * 充值后余额（元）
     */
    private BigDecimal balanceAfter;

    /**
     * 交易流水号
     */
    private String transactionId;

    /**
     * 充值时间
     */
    private LocalDateTime rechargeTime;

    /**
     * 支付方式
     */
    private String paymentMethod;
}

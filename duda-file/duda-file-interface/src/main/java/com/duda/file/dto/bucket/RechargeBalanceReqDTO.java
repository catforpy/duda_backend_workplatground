package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 账户余额充值请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeBalanceReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 充值金额（元）
     */
    private BigDecimal amount;

    /**
     * 支付方式
     * - alipay: 支付宝
     * - wechat: 微信
     * - bank_transfer: 银行转账
     * - manual: 手动充值
     */
    private String paymentMethod;

    /**
     * 交易流水号（如果已完成支付）
     */
    private String transactionId;

    /**
     * 备注
     */
    private String remark;
}

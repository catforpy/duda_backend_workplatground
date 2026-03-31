package com.duda.common.mq.message;

import com.duda.common.mq.BaseMqMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单支付成功消息
 *
 * 用于：
 * - 更新库存
 * - 发送支付成功通知
 * - 触发发货流程
 * - 更新会员积分
 * - 生成财务记录
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPaidMsg extends BaseMqMsg {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单金额
     */
    private BigDecimal totalAmount;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 支付时间
     */
    private String paymentTime;

    /**
     * 订单类型（tenant_order/supply_order）
     */
    private String orderType;
}

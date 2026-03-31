package com.duda.common.mq.message;

import com.duda.common.mq.BaseMqMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单取消消息
 *
 * 用于：
 * - 释放库存
 * - 发送取消通知
 * - 退款处理
 * - 恢复优惠券
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderCancelledMsg extends BaseMqMsg {

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
     * 取消原因
     */
    private String reason;

    /**
     * 取消时间
     */
    private String cancelTime;

    /**
     * 订单类型（tenant_order/supply_order）
     */
    private String orderType;

    /**
     * 是否需要退款
     */
    private Boolean needRefund;
}

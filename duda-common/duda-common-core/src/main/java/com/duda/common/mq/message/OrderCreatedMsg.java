package com.duda.common.mq.message;

import com.duda.common.mq.BaseMqMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单创建消息
 *
 * 用于：
 * - 发送订单创建通知
 * - 预扣减库存
 * - 订单超时检测
 * - 风控检测
 *
 * @author DudaNexus
 * @since 2026-03-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedMsg extends BaseMqMsg {

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
     * 创建时间
     */
    private String createTime;

    /**
     * 订单类型（tenant_order/supply_order）
     */
    private String orderType;

    /**
     * 订单超时时间（分钟）
     */
    private Integer timeoutMinutes;
}

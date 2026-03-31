package com.duda.tenant.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租户订单VO
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
public class TenantOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 套餐ID
     */
    private Long packageId;

    /**
     * 套餐编码
     */
    private String packageCode;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 订单类型
     */
    private String orderType;

    /**
     * 订单类型描述
     */
    private String orderTypeDesc;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 实际金额
     */
    private BigDecimal actualAmount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 支付状态
     */
    private String paymentStatus;

    /**
     * 支付状态描述
     */
    private String paymentStatusDesc;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 购买月数
     */
    private Integer durationMonths;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

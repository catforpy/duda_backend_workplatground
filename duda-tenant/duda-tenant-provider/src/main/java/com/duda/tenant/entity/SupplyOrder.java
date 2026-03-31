package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应链订单实体
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
@TableName("supply_orders")
public class SupplyOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 供应链商品ID
     */
    private Long supplyProductId;

    /**
     * 商品编码
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 分销商租户ID
     */
    private Long distributorTenantId;

    /**
     * 分销商订单号
     */
    private String distributorOrderNo;

    /**
     * 供应商租户ID（NULL表示平台自营）
     */
    private Long supplierTenantId;

    /**
     * 供应商用户ID ✨新增
     */
    private Long supplierUserId;

    /**
     * 客户用户ID（最终购买者）✨新增
     */
    private Long customerUserId;

    /**
     * 供应商租户名称
     */
    private String supplierTenantName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 商品规格（如：颜色、内存）
     */
    private String productSpec;

    /**
     * 商品单价
     */
    private BigDecimal productPrice;

    /**
     * 购买数量
     */
    private Integer productQuantity;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 供应价金额
     */
    private BigDecimal supplyPrice;

    /**
     * 佣金金额
     */
    private BigDecimal commissionAmount;

    /**
     * 客户姓名
     */
    private String customerName;

    /**
     * 客户手机号
     */
    private String customerPhone;

    /**
     * 客户收货地址
     */
    private String customerAddress;

    /**
     * 发货方式
     */
    private String shippingMode;

    /**
     * 发货状态：pending-待发货/shipping-已发货/delivered-已签收
     */
    private String shippingStatus;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 物流单号
     */
    private String logisticsNo;

    /**
     * 发货时间
     */
    private LocalDateTime shippingTime;

    /**
     * 签收时间
     */
    private LocalDateTime deliveredTime;

    /**
     * 结算状态：pending-待结算/settled-已结算
     */
    private String settlementStatus;

    /**
     * 对账周期ID
     */
    private Long settlementPeriodId;

    /**
     * 结算时间
     */
    private LocalDateTime settledAt;

    /**
     * 订单状态：pending-待付款/paid-已支付/shipped-已发货/completed-完成/refunded-退款
     */
    private String orderStatus;

    /**
     * 下单时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

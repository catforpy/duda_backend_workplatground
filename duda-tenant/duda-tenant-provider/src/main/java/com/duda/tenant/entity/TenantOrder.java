package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租户订单实体
 *
 * 数据库表: tenant_orders
 * 文档位置: /Volumes/DudaDate/FlutterClasses/ESC/init_duda_tenant_完整手册.md
 * 修复说明: 执行 /Volumes/DudaDate/FlutterClasses/ESC/fix_tenant_orders_table.sql 后此Entity完全匹配
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
@TableName("tenant_orders")
public class TenantOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 租户编码(冗余)
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
     * 套餐编码(冗余)
     */
    private String packageCode;

    /**
     * 套餐名称(冗余)
     */
    private String packageName;

    /**
     * 订单类型(new/renew/upgrade)
     */
    private String orderType;

    /**
     * 订单金额(元)
     */
    private BigDecimal orderAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal actualAmount;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 支付状态(unpaid/paid/cancelled)
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
     * 第三方支付流水号
     */
    private String paymentNo;

    /**
     * 套餐开始时间
     */
    private LocalDateTime startTime;

    /**
     * 套餐结束时间
     */
    private LocalDateTime endTime;

    /**
     * 订阅时长(月)
     */
    private Integer durationMonths;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 是否需要发票
     */
    private Integer invoiceRequired;

    /**
     * 发票状态
     */
    private String invoiceStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标记
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

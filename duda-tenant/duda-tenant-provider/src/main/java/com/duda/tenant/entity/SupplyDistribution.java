package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应链分销记录实体
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
@TableName("supply_distributions")
public class SupplyDistribution implements Serializable {

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
     * 供应商用户ID ✨新增
     */
    private Long supplierUserId;

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
     * 分销商租户编码
     */
    private String distributorTenantCode;

    /**
     * 分销商租户名称
     */
    private String distributorTenantName;

    /**
     * 分销商本地商品ID
     */
    private Long localProductId;

    /**
     * 分销商本地商品名称
     */
    private String localProductName;

    /**
     * 销售价
     */
    private BigDecimal salePrice;

    /**
     * 供应价（快照）
     */
    private BigDecimal supplyPrice;

    /**
     * 佣金比例（快照）
     */
    private BigDecimal commissionRate;

    /**
     * 佣金金额（快照）
     */
    private BigDecimal commissionAmount;

    /**
     * 库存模式：sync-同步供应商/local-本地库存
     */
    private String stockMode;

    /**
     * 本地库存数量
     */
    private Integer localStockCount;

    /**
     * 发货方式
     */
    private String shippingMode;

    /**
     * 发货地址（快照）
     */
    private String shippingAddress;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 销售数量
     */
    private Integer salesCount;

    /**
     * 销售总额
     */
    private BigDecimal totalSalesAmount;

    /**
     * 状态：active-已上架/paused-已暂停/terminated-已终止
     */
    private String status;

    /**
     * 终止时间
     */
    private LocalDateTime terminatedAt;

    /**
     * 终止原因
     */
    private String terminatedReason;

    /**
     * 上架时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

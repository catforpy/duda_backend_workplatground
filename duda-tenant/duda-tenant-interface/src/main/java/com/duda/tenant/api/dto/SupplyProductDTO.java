package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应链商品DTO
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
public class SupplyProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 商品编码
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品类型：supplier-供应商商品/platform-平台自营
     */
    private String productType;

    /**
     * 供应商租户ID
     */
    private Long supplierTenantId;

    /**
     * 供应商用户ID ✨新增
     */
    private Long supplierUserId;

    /**
     * 供应商类型：tenant-租户/user-用户/platform-平台 ✨新增
     */
    private String supplierType;

    /**
     * 供应商租户编码
     */
    private String supplierTenantCode;

    /**
     * 供应商租户名称
     */
    private String supplierTenantName;

    /**
     * 商品封面图
     */
    private String productCover;

    /**
     * 商品图片（JSON数组）
     */
    private String productImages;

    /**
     * 商品描述
     */
    private String productDesc;

    /**
     * 商品详情
     */
    private String productDetail;

    /**
     * 原价/市场价
     */
    private BigDecimal originalPrice;

    /**
     * 供应价
     */
    private BigDecimal supplyPrice;

    /**
     * 建议零售价
     */
    private BigDecimal suggestPrice;

    /**
     * 佣金比例
     */
    private BigDecimal commissionRate;

    /**
     * 固定佣金金额
     */
    private BigDecimal commissionAmount;

    /**
     * 平台抽成比例
     */
    private BigDecimal platformCommissionRate;

    /**
     * 库存数量
     */
    private Integer stockCount;

    /**
     * 库存同步模式
     */
    private String stockSyncMode;

    /**
     * 库存预警值
     */
    private Integer warningStock;

    /**
     * 发货方式
     */
    private String shippingMode;

    /**
     * 发货地址
     */
    private String shippingAddress;

    /**
     * 平台分类ID
     */
    private Long categoryId;

    /**
     * 分类路径
     */
    private String categoryPath;

    /**
     * 标签
     */
    private String tags;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 被分销次数
     */
    private Integer distributionCount;

    /**
     * 销售数量
     */
    private Integer salesCount;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 审核状态
     */
    private String reviewStatus;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核原因
     */
    private String reviewReason;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

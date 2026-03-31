package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应链商品实体
 *
 * @author Claude Code
 * @since 2026-03-30
 */
@Data
@TableName("supply_products")
public class SupplyProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品编码（全局唯一）
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
     * 供应商用户ID（跨小程序唯一）✨新增
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
     * 供应商原始商品ID
     */
    private Long supplierProductId;

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
     * 商品详情（富文本）
     */
    private String productDetail;

    /**
     * 原价/市场价
     */
    private BigDecimal originalPrice;

    /**
     * 供应价（分销商进货价）
     */
    private BigDecimal supplyPrice;

    /**
     * 建议零售价
     */
    private BigDecimal suggestPrice;

    /**
     * 分销佣金比例（10%）
     */
    private BigDecimal commissionRate;

    /**
     * 固定佣金金额（优先于比例）
     */
    private BigDecimal commissionAmount;

    /**
     * 平台抽成比例（供应商商品时的平台服务费）
     */
    private BigDecimal platformCommissionRate;

    /**
     * 库存数量
     */
    private Integer stockCount;

    /**
     * 库存同步模式：realtime-实时/manual-手动
     */
    private String stockSyncMode;

    /**
     * 库存预警值
     */
    private Integer warningStock;

    /**
     * 发货方式：supplier-供应商代发/platform-平台代发
     */
    private String shippingMode;

    /**
     * 发货地址
     */
    private String shippingAddress;

    /**
     * 发货省份
     */
    private String shippingProvince;

    /**
     * 发货城市
     */
    private String shippingCity;

    /**
     * 发货区县
     */
    private String shippingDistrict;

    /**
     * 平台分类ID
     */
    private Long categoryId;

    /**
     * 分类路径（如：手机/数码/苹果）
     */
    private String categoryPath;

    /**
     * 标签（逗号分隔）
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
     * 评分（0-5）
     */
    private BigDecimal rating;

    /**
     * 审核状态：pending-待审核/approved-已通过/rejected-已拒绝
     */
    private String reviewStatus;

    /**
     * 审核员ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核原因
     */
    private String reviewReason;

    /**
     * 状态：draft-草稿/pending_review-待审核/on_sale-在架/off_sale-下架
     */
    private String status;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

package com.duda.tenant.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租户套餐VO
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
public class TenantPackageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 套餐ID
     */
    private Long id;

    /**
     * 套餐编码
     */
    private String packageCode;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 套餐类型
     */
    private String packageType;

    /**
     * 套餐类型描述
     */
    private String packageTypeDesc;

    /**
     * 最大用户数
     */
    private Integer maxUsers;

    /**
     * 最大存储空间（字节）
     */
    private Long maxStorageSize;

    /**
     * 最大存储空间（GB）
     */
    private Double maxStorageSizeGB;

    /**
     * 每日最大API调用次数
     */
    private Integer maxApiCallsPerDay;

    /**
     * 月付价格
     */
    private BigDecimal priceMonthly;

    /**
     * 年付价格
     */
    private BigDecimal priceYearly;

    /**
     * 功能列表（JSON格式）
     */
    private String features;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

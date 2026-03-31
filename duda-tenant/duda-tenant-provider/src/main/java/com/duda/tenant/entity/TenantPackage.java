package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租户套餐实体
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
@TableName("tenant_packages")
public class TenantPackage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 套餐ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套餐编码（全局唯一）
     */
    private String packageCode;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 套餐类型（PLATFORM-平台套餐, TENANT-租户套餐）
     */
    private String packageType;

    /**
     * 关联租户ID（租户套餐必填，平台套餐为空）
     */
    private Long tenantId;

    /**
     * 目标用户类型（ALL-全部用户, RENTAL-租赁用户, PARTNER-合作伙伴）
     */
    private String targetUserType;

    /**
     * 最大用户数（-1表示无限制）
     */
    private Integer maxUsers;

    /**
     * 最大存储空间（字节）
     */
    private Long maxStorageSize;

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
    private Integer isActive;

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

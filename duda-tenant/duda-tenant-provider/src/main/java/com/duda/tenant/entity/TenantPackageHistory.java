package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户套餐变更历史实体
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
@TableName("tenant_package_history")
public class TenantPackageHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 历史ID
     */
    @TableId(type = IdType.AUTO)
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
     * 变更前套餐ID
     */
    private Long oldPackageId;

    /**
     * 变更前套餐编码
     */
    private String oldPackageCode;

    /**
     * 变更前套餐名称
     */
    private String oldPackageName;

    /**
     * 变更后套餐ID
     */
    private Long newPackageId;

    /**
     * 变更后套餐编码
     */
    private String newPackageCode;

    /**
     * 变更后套餐名称
     */
    private String newPackageName;

    /**
     * 变更类型（upgrade/downgrade/renew）
     */
    private String changeType;

    /**
     * 变更原因
     */
    private String changeReason;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 扩展字段（JSON格式）
     */
    private String extendFields;

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

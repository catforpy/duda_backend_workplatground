package com.duda.tenant.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户VO
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
public class TenantVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 租户类型
     */
    private String tenantType;

    /**
     * 租户类型描述
     */
    private String tenantTypeDesc;

    /**
     * 当前套餐ID
     */
    private Long packageId;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 套餐到期时间
     */
    private LocalDateTime packageExpireTime;

    /**
     * 租户状态
     */
    private String tenantStatus;

    /**
     * 租户状态描述
     */
    private String tenantStatusDesc;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 是否已过期
     */
    private Boolean isExpired;

    /**
     * 最大用户数限制
     */
    private Integer maxUsers;

    /**
     * 最大管理员数量限制
     */
    private Integer maxAdmins;

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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

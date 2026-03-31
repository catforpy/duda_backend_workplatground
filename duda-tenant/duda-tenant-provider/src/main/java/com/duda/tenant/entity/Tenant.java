package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.duda.common.tenant.enums.TenantStatusEnum;
import com.duda.common.tenant.enums.TenantTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户实体
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
@TableName("tenants")
public class Tenant implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户编码（全局唯一）
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
     * 当前套餐ID
     */
    private Long packageId;

    /**
     * 套餐到期时间
     */
    private LocalDateTime packageExpireTime;

    /**
     * 租户状态
     */
    private String tenantStatus;

    /**
     * 过期时间（NULL表示永久有效）
     */
    private LocalDateTime expireTime;

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
     * 扩展字段（JSON格式）
     */
    private String extendFields;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

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

package com.duda.tenant.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建租户VO
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
public class TenantCreateVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 初始套餐ID
     */
    private Long packageId;

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
}

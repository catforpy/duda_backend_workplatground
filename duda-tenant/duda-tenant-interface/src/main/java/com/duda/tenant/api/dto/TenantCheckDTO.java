package com.duda.tenant.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 租户检查结果DTO
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
public class TenantCheckDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否有效
     */
    private Boolean isValid;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 租户状态
     */
    private String tenantStatus;

    /**
     * 是否过期
     */
    private Boolean isExpired;

    /**
     * 是否暂停
     */
    private Boolean isSuspended;

    /**
     * 错误信息
     */
    private String errorMessage;
}

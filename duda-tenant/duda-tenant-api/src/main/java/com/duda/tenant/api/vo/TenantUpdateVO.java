package com.duda.tenant.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新租户VO
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
public class TenantUpdateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private Long id;

    /**
     * 租户名称
     */
    private String tenantName;

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

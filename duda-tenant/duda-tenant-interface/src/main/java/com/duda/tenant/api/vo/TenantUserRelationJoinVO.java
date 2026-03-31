package com.duda.tenant.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户加入小程序请求VO
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Data
public class TenantUserRelationJoinVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 用户角色（tenant_admin/tenant_user）
     */
    private String userRole;

    /**
     * 部门
     */
    private String department;

    /**
     * 职位
     */
    private String position;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 邀请人姓名
     */
    private String inviterName;

    /**
     * 扩展字段（JSON格式）
     */
    private String extendFields;
}

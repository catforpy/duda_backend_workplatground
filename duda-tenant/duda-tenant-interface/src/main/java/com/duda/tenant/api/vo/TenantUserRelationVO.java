package com.duda.tenant.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户用户关系VO
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Data
public class TenantUserRelationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关系ID
     */
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
     * 租户名称
     */
    private String tenantName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String username;

    /**
     * 用户角色（tenant_admin/tenant_user）
     */
    private String userRole;

    /**
     * 用户角色描述
     */
    private String userRoleDesc;

    /**
     * 部门
     */
    private String department;

    /**
     * 职位
     */
    private String position;

    /**
     * 是否启用
     */
    private Integer isEnabled;

    /**
     * 加入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 邀请人姓名
     */
    private String inviterName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

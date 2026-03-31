package com.duda.tenant.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户用户关系DTO
 *
 * @author Claude Code
 * @since 2026-03-31
 */
@Data
public class TenantUserRelationDTO implements Serializable {

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
     * 用户ID
     */
    private Long userId;

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
     * 扩展字段（JSON格式）
     */
    private String extendFields;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

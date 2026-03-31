package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户用户关系实体
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
@TableName("tenant_user_relations")
public class TenantUserRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关系ID
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
     * 用户ID
     */
    private Long userId;

    /**
     * 用户分片ID（0-99）
     */
    private Integer userShard;

    /**
     * 角色编码：TENANT_ADMIN-租户管理员/TENANT_USER-租户用户
     */
    private String roleCode;

    /**
     * 是否为主租户（1-是，0-否）
     */
    private Integer isPrimary;

    /**
     * 状态：active-激活/inactive-未激活
     */
    private String status;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

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

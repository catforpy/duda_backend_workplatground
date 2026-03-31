package com.duda.user.entity.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 开放API密钥实体
 *
 * 说明：为PHP后端等第三方系统提供API访问密钥
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
@TableName("open_api_keys")
public class OpenApiKeys implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    // ========== 基本信息 ==========

    /**
     * 租户ID（多租户隔离）
     */
    private Long tenantId;

    /**
     * 应用ID（全局唯一，如：php_backend_001）
     */
    private String appId;

    /**
     * 应用密钥（SHA256加密）
     */
    private String appSecret;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用代码（英文标识）
     */
    private String appCode;

    // ========== 应用类型 ==========

    /**
     * 应用类型：backend-后端系统, frontend-前端应用, third_party-第三方应用, mobile-移动应用
     */
    private String appType;

    /**
     * 应用分类：internal-内部, external-外部, partner-合作伙伴
     */
    private String appCategory;

    /**
     * 应用所有者
     */
    private String appOwner;

    /**
     * 应用所有者ID
     */
    private Long appOwnerId;

    /**
     * 应用描述
     */
    private String appDescription;

    // ========== 权限控制（JSON格式）==========

    /**
     * 权限列表（JSON格式）：["user:read", "user:info", "auth:check-phone", "auth:bind-account"]
     */
    private String permissions;

    /**
     * 权限范围：read-只读, write-读写, admin-管理员
     */
    private String permissionScope;

    /**
     * 允许访问的资源（JSON格式）
     */
    private String allowedResources;

    // ========== 限流配置 ==========

    /**
     * 是否启用限流：0-否 1-是
     */
    private Integer rateLimitEnabled;

    /**
     * 每秒请求次数限制
     */
    private Integer rateLimitPerSecond;

    /**
     * 每分钟请求次数限制
     */
    private Integer rateLimitPerMinute;

    /**
     * 每小时请求次数限制
     */
    private Integer rateLimitPerHour;

    /**
     * 每天请求次数限制
     */
    private Integer rateLimitPerDay;

    // ========== IP白名单/黑名单（JSON格式）==========

    /**
     * 是否启用IP白名单：0-否 1-是
     */
    private Integer ipWhitelistEnabled;

    /**
     * IP白名单（JSON格式）
     */
    private String ipWhitelist;

    /**
     * IP黑名单（JSON格式）
     */
    private String ipBlacklist;

    // ========== 访问统计 ==========

    /**
     * 总请求次数
     */
    private Long totalRequests;

    /**
     * 成功请求次数
     */
    private Long successRequests;

    /**
     * 失败请求次数
     */
    private Long failedRequests;

    /**
     * 最后请求时间
     */
    private LocalDateTime lastRequestTime;

    /**
     * 最后请求IP
     */
    private String lastRequestIp;

    // ========== 状态管理 ==========

    /**
     * 状态：0-禁用 1-启用 2-审核中 3-已拒绝
     */
    private Integer status;

    /**
     * 审核状态：pending-待审核, approved-已通过, rejected-已拒绝
     */
    private String auditStatus;

    // ========== 有效期 ==========

    /**
     * 过期时间（NULL表示永不过期）
     */
    private LocalDateTime expireTime;

    /**
     * 是否已发送过期通知：0-否 1-是
     */
    private Integer expireNotificationSent;

    // ========== 时间信息 ==========

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    // ========== 乐观锁和逻辑删除 ==========

    /**
     * 版本号（乐观锁）
     */
    @Version
    private Integer version;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
}

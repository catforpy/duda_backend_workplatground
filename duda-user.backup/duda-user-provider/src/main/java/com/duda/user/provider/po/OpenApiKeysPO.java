package com.duda.user.provider.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.duda.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 开放API密钥PO
 *
 * 表名: open_api_keys
 * 说明: 开放API密钥表，用于第三方系统调用开放API时的身份认证
 * 租户隔离: 是（通过tenant_id字段）
 * 乐观锁: 是（version字段）
 *
 * @author Claude
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("open_api_keys")
public class OpenApiKeysPO extends BaseEntity {

    /**
     * API密钥ID（主键）
     */
    private Long id;

    /**
     * 租户ID（租户隔离字段）
     */
    private Long tenantId;

    /**
     * 应用ID（全局唯一）
     * 示例: php_backend_001, java_backend_002
     */
    private String appId;

    /**
     * 应用密钥（SHA256哈希值）
     */
    private String appSecret;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用编码（英文字母、数字、下划线）
     */
    private String appCode;

    /**
     * 应用类型
     * backend-后端, frontend-前端, third_party-第三方, mobile-移动端
     */
    private String appType;

    /**
     * 应用分类
     * internal-内部应用, external-外部应用, partner-合作伙伴
     */
    private String appCategory;

    /**
     * 应用所有者
     */
    private String appOwner;

    /**
     * 应用所有者ID（关联user_nexus.users_XX.id）
     */
    private Long appOwnerId;

    /**
     * 应用描述
     */
    private String appDescription;

    /**
     * 应用图标URL
     */
    private String appIcon;

    /**
     * 应用URL
     */
    private String appUrl;

    /**
     * 应用回调URL
     */
    private String appCallbackUrl;

    /**
     * 权限列表（JSON数组格式）
     * 示例: ["user:read", "user:info", "auth:check-phone", "auth:bind-account"]
     */
    @TableField(typeHandler = com.duda.common.database.handler.JsonTypeHandler.class)
    private Object permissions;

    /**
     * 权限范围
     * read-只读, write-读写, admin-管理
     */
    private String permissionScope;

    /**
     * 允许访问的资源列表（JSON数组格式）
     * 示例: ["users", "orders", "products"]
     */
    @TableField(typeHandler = com.duda.common.database.handler.JsonTypeHandler.class)
    private Object allowedResources;

    /**
     * 拒绝访问的资源列表（JSON数组格式）
     * 示例: ["admin", "config"]
     */
    @TableField(typeHandler = com.duda.common.database.handler.JsonTypeHandler.class)
    private Object deniedResources;

    /**
     * 是否启用速率限制
     * 0-否, 1-是
     */
    private Byte rateLimitEnabled;

    /**
     * 每秒速率限制
     */
    private Integer rateLimitPerSecond;

    /**
     * 每分钟速率限制
     */
    private Integer rateLimitPerMinute;

    /**
     * 每小时速率限制
     */
    private Integer rateLimitPerHour;

    /**
     * 每天速率限制
     */
    private Integer rateLimitPerDay;

    /**
     * 突发速率限制
     */
    private Integer burstLimit;

    /**
     * 是否启用IP白名单
     * 0-否, 1-是
     */
    private Byte ipWhitelistEnabled;

    /**
     * IP白名单（JSON数组格式）
     * 示例: ["192.168.1.100", "192.168.1.101", "192.168.1.*"]
     */
    @TableField(typeHandler = com.duda.common.database.handler.JsonTypeHandler.class)
    private Object ipWhitelist;

    /**
     * IP黑名单（JSON数组格式）
     * 示例: ["xxx.xxx.xxx.xxx"]
     */
    @TableField(typeHandler = com.duda.common.database.handler.JsonTypeHandler.class)
    private Object ipBlacklist;

    /**
     * IP白名单类型
     * allow-白名单模式, deny-黑名单模式
     */
    private String ipWhitelistType;

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
    private Date lastRequestTime;

    /**
     * 最后请求IP
     */
    private String lastRequestIp;

    /**
     * 最后请求路径
     */
    private String lastRequestPath;

    /**
     * 平均响应时间（毫秒）
     */
    private Integer avgResponseTime;

    /**
     * 今日请求次数
     */
    private Integer todayRequests;

    /**
     * 今日流量（字节）
     */
    private Long todayTraffic;

    /**
     * 本月请求次数
     */
    private Integer monthRequests;

    /**
     * 本月流量（字节）
     */
    private Long monthTraffic;

    /**
     * 总流量（字节）
     */
    private Long totalTraffic;

    /**
     * 状态
     * 0-禁用, 1-启用, 2-暂停, 3-删除
     */
    private Byte status;

    /**
     * 审核状态
     * pending-待审核, approved-已通过, rejected-已拒绝
     */
    private String auditStatus;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 审核人ID
     */
    private Long auditBy;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 过期时间（NULL表示永久有效）
     */
    private Date expireTime;

    /**
     * 过期通知是否已发送
     * 0-否, 1-是
     */
    private Byte expireNotificationSent;

    /**
     * 是否启用密钥轮换
     * 0-否, 1-是
     */
    private Byte secretRotationEnabled;

    /**
     * 密钥轮换周期（天）
     */
    private Integer secretRotationDays;

    /**
     * 最后密钥轮换时间
     */
    private Date lastSecretRotation;

    /**
     * 是否要求签名
     * 0-否, 1-是
     */
    private Byte requireSignature;

    /**
     * 签名算法
     * HMAC-SHA256, HMAC-SHA512
     */
    private String signatureAlgorithm;

    /**
     * 扩展配置（JSON格式）
     */
    @TableField(typeHandler = com.duda.common.database.handler.JsonTypeHandler.class)
    private Object extConfig;

    /**
     * 描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标记
     * 0-正常, 1-已删除
     */
    private Byte deleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 版本号（乐观锁）
     */
    @Version
    private Integer version;
}

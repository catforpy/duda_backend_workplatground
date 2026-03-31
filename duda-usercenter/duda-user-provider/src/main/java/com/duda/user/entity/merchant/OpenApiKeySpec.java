package com.duda.user.entity.merchant;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 开放API密钥Entity
 *
 * 对应数据库表：open_api_keys
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Data
@TableName("open_api_keys")
public class OpenApiKeySpec implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 应用ID（全局唯一）
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
     * 应用代码
     */
    private String appCode;

    /**
     * 应用类型
     */
    private String appType;

    /**
     * 应用分类
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
     * 权限列表（JSON格式）
     */
    private String permissions;

    /**
     * 权限范围
     */
    private String permissionScope;

    /**
     * 允许访问的资源（JSON格式）
     */
    private String allowedResources;

    /**
     * 禁止访问的资源（JSON格式）
     */
    private String deniedResources;

    /**
     * 是否启用限流
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

    /**
     * 突发流量限制
     */
    private Integer burstLimit;

    /**
     * 是否启用IP白名单
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

    /**
     * 最后请求路径
     */
    private String lastRequestPath;

    /**
     * 平均响应时间（毫秒）
     */
    private Integer avgResponseTime;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 审核状态
     */
    private String auditStatus;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人ID
     */
    private Long auditBy;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 扩展配置（JSON格式）
     */
    private String extConfig;

    /**
     * 应用描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除
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

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}

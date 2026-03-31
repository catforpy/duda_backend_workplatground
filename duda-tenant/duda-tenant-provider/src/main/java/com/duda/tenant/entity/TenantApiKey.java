package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户API密钥实体
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
@TableName("tenant_api_keys")
public class TenantApiKey implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * API密钥ID
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
     * API密钥名称
     */
    private String keyName;

    /**
     * API密钥（加密存储）
     */
    private String apiKey;

    /**
     * API密钥（加密存储）
     */
    private String apiSecret;

    /**
     * 密钥类型（read_write/read_only）
     */
    private String keyType;

    /**
     * 权限列表（JSON格式）
     */
    private String permissions;

    /**
     * IP白名单（JSON数组）
     */
    private String ipWhitelist;

    /**
     * 每日调用次数限制
     */
    private Integer dailyCallLimit;

    /**
     * 今日已调用次数
     */
    private Integer todayCallCount;

    /**
     * 总调用次数
     */
    private Long totalCallCount;

    /**
     * 最后调用时间
     */
    private LocalDateTime lastCallTime;

    /**
     * 过期时间（NULL表示永久有效）
     */
    private LocalDateTime expireTime;

    /**
     * 是否启用
     */
    private Integer isEnabled;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 扩展字段（JSON格式）
     */
    private String extendFields;

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

package com.duda.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户API密钥使用日志实体
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
@TableName("tenant_api_key_usage_logs")
public class TenantApiKeyUsageLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
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
     * API密钥ID
     */
    private Long apiKeyId;

    /**
     * API密钥名称
     */
    private String keyName;

    /**
     * 请求方法（GET/POST/PUT/DELETE）
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应状态码
     */
    private Integer responseStatus;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 请求结果（success/failed）
     */
    private String requestResult;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

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

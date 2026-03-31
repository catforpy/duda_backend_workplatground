package com.duda.user.dto.merchant;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * API调用日志DTO
 *
 * 说明：记录所有API调用，用于监控、审计和分析
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Data
public class ApiCallLogDTO implements Serializable {

    /**
     * 主键ID（雪花算法）
     */
    private Long id;

    // ========== 关联信息 ==========

    /**
     * 应用ID（关联open_api_keys表）
     */
    private String appId;

    /**
     * 请求ID（全局唯一，用于追踪）
     */
    private String requestId;

    /**
     * 追踪ID（用于分布式追踪）
     */
    private String traceId;

    // ========== 请求信息 ==========

    /**
     * 请求方法：GET/POST/PUT/DELETE/PATCH
     */
    private String requestMethod;

    /**
     * 请求协议：HTTP/HTTPS
     */
    private String requestProtocol;

    /**
     * 请求路径（如：/api/user/info）
     */
    private String requestPath;

    /**
     * 请求参数（Query参数，JSON格式）
     */
    private String requestParams;

    /**
     * 请求体（JSON格式）
     */
    private String requestBody;

    /**
     * 请求大小（字节）
     */
    private Long requestSize;

    // ========== 响应信息 ==========

    /**
     * HTTP状态码：200/400/401/403/404/500等
     */
    private Integer responseStatus;

    /**
     * 响应体大小（字节）
     */
    private Long responseSize;

    /**
     * 响应耗时（毫秒）
     */
    private Integer responseTime;

    // ========== 客户端信息 ==========

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 设备类型：ios/android/web/miniapp/unknown
     */
    private String deviceType;

    // ========== 认证信息 ==========

    /**
     * 认证类型：api_key/jwt/oauth2/none
     */
    private String authType;

    /**
     * 用户ID（如果有）
     */
    private Long userId;

    /**
     * API密钥ID（关联open_api_keys.id）
     */
    private Long apiKeyId;

    // ========== 结果信息 ==========

    /**
     * 调用结果：success-成功, failed-失败, error-错误, timeout-超时
     */
    private String callResult;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    // ========== 时间信息 ==========

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 响应时间点
     */
    private LocalDateTime responseTimestamp;
}

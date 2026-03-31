package com.duda.user.entity.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * API调用日志Entity
 *
 * 对应数据库表：api_call_logs
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Data
@TableName("api_call_logs")
public class ApiCallLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 请求ID（全局唯一）
     */
    private String requestId;

    /**
     * 追踪ID
     */
    private String traceId;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求协议
     */
    private String requestProtocol;

    /**
     * 请求域名
     */
    private String requestDomain;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 完整请求URI
     */
    private String requestUri;

    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;

    /**
     * 请求体（JSON格式）
     */
    private String requestBody;

    /**
     * 请求头（JSON格式）
     */
    private String requestHeaders;

    /**
     * 请求大小（字节）
     */
    private Long requestSize;

    /**
     * HTTP状态码
     */
    private Integer responseStatus;

    /**
     * 响应体（JSON格式）
     */
    private String responseBody;

    /**
     * 响应大小（字节）
     */
    private Long responseSize;

    /**
     * 响应头（JSON格式）
     */
    private String responseHeaders;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 客户端端口
     */
    private Integer clientPort;

    /**
     * 客户端国家
     */
    private String clientCountry;

    /**
     * 客户端省份
     */
    private String clientProvince;

    /**
     * 客户端城市
     */
    private String clientCity;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 浏览器类型
     */
    private String browserType;

    /**
     * 操作系统类型
     */
    private String osType;

    /**
     * 认证类型
     */
    private String authType;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * API密钥ID
     */
    private Long apiKeyId;

    /**
     * 调用结果
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

    /**
     * 错误堆栈
     */
    private String errorStack;

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 响应时间点
     */
    private LocalDateTime responseTime;

    /**
     * 响应耗时（毫秒）
     */
    private Integer duration;

    /**
     * 备注
     */
    private String remark;
}

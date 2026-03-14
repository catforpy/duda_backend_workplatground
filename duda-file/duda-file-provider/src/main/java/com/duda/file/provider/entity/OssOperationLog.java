package com.duda.file.provider.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OSS操作日志实体
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("oss_operation_log")
public class OssOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 操作类型：upload/download/delete/copy/rename/config/etc
     */
    private String operationType;

    /**
     * 操作分类：file/bucket/config/policy
     */
    private String operationCategory;

    /**
     * 对象Key（文件操作时记录）
     */
    private String objectKey;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;

    /**
     * 响应数据（JSON格式）
     */
    private String responseData;

    /**
     * 操作状态：SUCCESS/FAILED/PARTIAL
     */
    private String status;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型（MIME）
     */
    private String fileType;

    /**
     * 文件ETag
     */
    private String fileEtag;

    /**
     * 文件版本ID
     */
    private String fileVersionId;

    /**
     * 配置字段名
     */
    private String configField;

    /**
     * 旧值（JSON格式）
     */
    private String oldValue;

    /**
     * 新值（JSON格式）
     */
    private String newValue;

    /**
     * 操作者类型：SYSTEM/USER/API
     */
    private String operatorType;

    /**
     * 操作者ID
     */
    private Long operatorId;

    /**
     * 操作者名称
     */
    private String operatorName;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 请求ID（阿里云返回）
     */
    private String requestId;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 操作开始时间
     */
    private LocalDateTime startTime;

    /**
     * 操作结束时间
     */
    private LocalDateTime endTime;

    /**
     * 操作耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 额外信息（JSON格式）
     */
    private String extraInfo;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}

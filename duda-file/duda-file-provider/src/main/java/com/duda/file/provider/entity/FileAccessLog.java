package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件访问日志实体
 * 对应file_access_log表
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAccessLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 操作类型: UPLOAD, DOWNLOAD, DELETE, COPY, RENAME
     */
    private String operation;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户分片编号
     */
    private Integer userShard;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 结果状态: SUCCESS, FAILED
     */
    private String resultStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时(毫秒)
     */
    private Long durationMs;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}

package com.duda.file.dto.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件访问日志DTO
 * 对应file_access_log表
 *
 * @author duda
 * @date 2025-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAccessLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 操作类型：UPLOAD/DOWNLOAD/DELETE/COPY/RENAME
     */
    private String operation;

    // ==================== 用户信息 ====================

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户分片编号
     */
    private Integer userShard;

    // ==================== 客户端信息 ====================

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

    // ==================== 文件信息 ====================

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 结果状态：SUCCESS/FAILED
     */
    private String resultStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    // ==================== 时间统计 ====================

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 租户ID
     */
    private Long tenantId;

    // ==================== 业务方法 ====================

    /**
     * 判断操作是否成功
     */
    public Boolean isSuccess() {
        return "SUCCESS".equals(resultStatus);
    }

    /**
     * 判断操作是否失败
     */
    public Boolean isFailed() {
        return "FAILED".equals(resultStatus);
    }

    /**
     * 判断是否为上传操作
     */
    public Boolean isUpload() {
        return "UPLOAD".equals(operation);
    }

    /**
     * 判断是否为下载操作
     */
    public Boolean isDownload() {
        return "DOWNLOAD".equals(operation);
    }

    /**
     * 判断是否为删除操作
     */
    public Boolean isDelete() {
        return "DELETE".equals(operation);
    }

    /**
     * 获取人类可读的耗时
     */
    public String getHumanReadableDuration() {
        if (durationMs == null) {
            return "unknown";
        }
        if (durationMs < 1000) {
            return durationMs + " ms";
        } else {
            return String.format("%.2f s", durationMs / 1000.0);
        }
    }

    /**
     * 获取人类可读的文件大小
     */
    public String getHumanReadableFileSize() {
        if (fileSize == null) {
            return "unknown";
        }
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 计算吞吐量（字节/秒）
     */
    public Double getThroughputBytesPerSecond() {
        if (fileSize == null || durationMs == null || durationMs == 0) {
            return 0.0;
        }
        return (fileSize * 1000.0) / durationMs;
    }

    /**
     * 获取人类可读的吞吐量
     */
    public String getHumanReadableThroughput() {
        Double throughput = getThroughputBytesPerSecond();
        if (throughput == 0.0) {
            return "unknown";
        }
        if (throughput < 1024) {
            return String.format("%.2f B/s", throughput);
        } else if (throughput < 1024 * 1024) {
            return String.format("%.2f KB/s", throughput / 1024);
        } else if (throughput < 1024 * 1024 * 1024) {
            return String.format("%.2f MB/s", throughput / (1024 * 1024));
        } else {
            return String.format("%.2f GB/s", throughput / (1024.0 * 1024 * 1024));
        }
    }
}

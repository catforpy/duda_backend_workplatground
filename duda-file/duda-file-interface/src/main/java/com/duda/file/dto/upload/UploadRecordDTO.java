package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 上传记录DTO
 * 对应upload_record表
 *
 * @author duda
 * @date 2025-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadRecordDTO implements Serializable {

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
     * 分片上传ID（断点续传用）
     */
    private String uploadId;

    // ==================== 用户信息 ====================

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 用户分片编号
     */
    private Integer userShard;

    // ==================== 文件信息 ====================

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件MD5
     */
    private String fileMd5;

    /**
     * MIME类型
     */
    private String contentType;

    /**
     * 上传方式：simple/multipart/append/form/sts/presigned
     */
    private String uploadMethod;

    /**
     * 分片数量
     */
    private Integer partCount;

    /**
     * 分片大小
     */
    private Long partSize;

    /**
     * 已上传分片数
     */
    private Integer uploadedParts;

    /**
     * 上传状态：INIT/IN_PROGRESS/COMPLETED/FAILED/CANCELLED
     */
    private String uploadStatus;

    // ==================== 时间统计 ====================

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    // ==================== 客户端信息 ====================

    /**
     * 上传IP
     */
    private String uploadIp;

    /**
     * 客户端类型：web/app/mini_program
     */
    private String clientType;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 租户ID
     */
    private Long tenantId;

    // ==================== 业务方法 ====================

    /**
     * 判断上传是否完成
     */
    public Boolean isCompleted() {
        return "COMPLETED".equals(uploadStatus);
    }

    /**
     * 判断上传是否失败
     */
    public Boolean isFailed() {
        return "FAILED".equals(uploadStatus);
    }

    /**
     * 判断上传是否进行中
     */
    public Boolean isInProgress() {
        return "IN_PROGRESS".equals(uploadStatus);
    }

    /**
     * 获取上传进度百分比
     */
    public Double getProgressPercentage() {
        if (partCount == null || partCount == 0) {
            return 0.0;
        }
        if (uploadedParts == null) {
            return 0.0;
        }
        return (uploadedParts * 100.0) / partCount;
    }

    /**
     * 获取上传耗时（秒）
     */
    public Long getDurationSeconds() {
        if (startTime == null || completeTime == null) {
            return null;
        }
        return java.time.Duration.between(startTime, completeTime).getSeconds();
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
}

package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 断点续传记录DTO
 * 对应resume_upload_record表
 *
 * @author duda
 * @date 2025-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeUploadRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 断点续传记录ID（UUID）
     */
    private String recordId;

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 本地文件路径
     */
    private String localFilePath;

    // ==================== 文件信息 ====================

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 分片大小
     */
    private Long partSize;

    /**
     * 总分片数
     */
    private Integer totalPartCount;

    /**
     * 已上传分片列表（JSON字符串）
     * 格式："[1,2,3,4,5]"
     */
    private String uploadedParts;

    /**
     * 状态：INIT/IN_PROGRESS/PAUSED/COMPLETED/FAILED/CANCELLED
     */
    private String uploadStatus;

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
     * 客户端类型
     */
    private String clientType;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 并发线程数
     */
    private Integer concurrentThreads;

    // ==================== 时间统计 ====================

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

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
     * 判断是否已完成
     */
    public Boolean isCompleted() {
        return "COMPLETED".equals(uploadStatus);
    }

    /**
     * 判断是否进行中
     */
    public Boolean isInProgress() {
        return "IN_PROGRESS".equals(uploadStatus);
    }

    /**
     * 判断是否已暂停
     */
    public Boolean isPaused() {
        return "PAUSED".equals(uploadStatus);
    }

    /**
     * 判断是否失败
     */
    public Boolean isFailed() {
        return "FAILED".equals(uploadStatus);
    }

    /**
     * 获取已上传分片数量
     */
    public Integer getUploadedPartsCount() {
        if (uploadedParts == null || uploadedParts.isEmpty()) {
            return 0;
        }
        // JSON 数组格式："[1,2,3,4,5]"
        String parts = uploadedParts.replaceAll("[\\[\\]\"]", "").trim();
        if (parts.isEmpty()) {
            return 0;
        }
        return parts.split(",").length;
    }

    /**
     * 获取上传进度百分比
     */
    public Double getProgressPercentage() {
        if (totalPartCount == null || totalPartCount == 0) {
            return 0.0;
        }
        return (getUploadedPartsCount() * 100.0) / totalPartCount;
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

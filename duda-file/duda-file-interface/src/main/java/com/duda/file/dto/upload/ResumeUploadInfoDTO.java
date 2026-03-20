package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import java.io.Serializable;
import lombok.Builder;
import java.io.Serializable;
import lombok.Data;
import java.io.Serializable;
import lombok.NoArgsConstructor;
import java.io.Serializable;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;
import java.io.Serializable;
import java.util.Map;
import java.io.Serializable;

/**
 * 断点续传信息DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeUploadInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 断点续传记录ID
     */
    private String recordId;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 上传ID(分片上传时)
     */
    private String uploadId;

    /**
     * 本地文件路径
     */
    private String localFilePath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 分片大小(字节)
     */
    private Long partSize;

    /**
     * 总分片数
     */
    private Integer totalPartCount;

    /**
     * 已上传分片数
     */
    private Integer uploadedPartCount;

    /**
     * 已上传大小(字节)
     */
    private Long uploadedSize;

    /**
     * 上传状态
     * - INIT: 初始化
     * - IN_PROGRESS: 上传中
     * - PAUSED: 已暂停
     * - COMPLETED: 已完成
     * - FAILED: 失败
     * - CANCELLED: 已取消
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 上传进度(0-100)
     */
    private Double progress;

    /**
     * 已上传分片列表
     * key: partNumber
     * value: 分片信息
     */
    private Map<Integer, PartInfo> uploadedParts;

    /**
     * 未上传分片列表
     */
    private List<Integer> remainingParts;

    /**
     * 错误信息(失败时)
     */
    private String errorMessage;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 扩展信息
     */
    private Map<String, Object> extra;

    /**
     * 分片信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartInfo {
        /**
         * 分片号
         */
        private Integer partNumber;

        /**
         * 分片偏移量(字节)
         */
        private Long offset;

        /**
         * 分片大小(字节)
         */
        private Long partSize;

        /**
         * 分片ETag
         */
        private String eTag;

        /**
         * 上传时间
         */
        private LocalDateTime uploadTime;

        /**
         * 是否已上传
         */
        private Boolean uploaded;

        /**
         * CRC64校验值
         */
        private Long crc64;
    }

    /**
     * 是否可以恢复
     */
    public boolean isResumable() {
        return "PAUSED".equals(status) || "FAILED".equals(status) || "IN_PROGRESS".equals(status);
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    /**
     * 是否正在进行
     */
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }

    /**
     * 是否已暂停
     */
    public boolean isPaused() {
        return "PAUSED".equals(status);
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * 计算上传进度
     */
    public double calculateProgress() {
        if (fileSize == null || fileSize == 0) {
            return 0.0;
        }
        long uploaded = uploadedSize != null ? uploadedSize : 0;
        return (double) uploaded / fileSize * 100;
    }

    /**
     * 获取下一个未上传的分片号
     */
    public Integer getNextPartNumber() {
        if (remainingParts != null && !remainingParts.isEmpty()) {
            return remainingParts.get(0);
        }
        if (uploadedParts != null) {
            for (int i = 1; i <= totalPartCount; i++) {
                PartInfo part = uploadedParts.get(i);
                if (part == null || !part.getUploaded()) {
                    return i;
                }
            }
        }
        return null;
    }

    /**
     * 获取上传进度百分比
     */
    public String getProgressText() {
        if (progress == null) {
            progress = calculateProgress();
        }
        return String.format("%.2f%%", progress);
    }
}

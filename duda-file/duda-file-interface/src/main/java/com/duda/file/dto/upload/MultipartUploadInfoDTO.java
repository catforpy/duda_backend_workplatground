package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 分片上传信息DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipartUploadInfoDTO {

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 上传ID
     * 用于标识和操作分片上传任务
     */
    private String uploadId;

    /**
     * 上传状态
     * - INIT: 已初始化
     * - IN_PROGRESS: 上传中
     * - COMPLETED: 已完成
     * - ABORTED: 已取消
     * - EXPIRED: 已过期
     */
    private String status;

    /**
     * 总分片数
     */
    private Integer totalPartCount;

    /**
     * 已上传分片数
     */
    private Integer uploadedPartCount;

    /**
     * 分片大小(字节)
     */
    private Long partSize;

    /**
     * 已上传大小(字节)
     */
    private Long uploadedSize;

    /**
     * 总大小(字节)
     */
    private Long totalSize;

    /**
     * 初始化时间
     */
    private LocalDateTime initTime;

    /**
     * 上传完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 分片信息
     * key: partNumber
     * value: PartInfo
     */
    private Map<Integer, PartInfo> parts;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 创建者ID
     */
    private String ownerId;

    /**
     * 存储类型
     */
    private String storageClass;

    /**
     * 是否过期
     */
    private Boolean expired;

    /**
     * 过期时间
     */
    private LocalDateTime expiryTime;

    /**
     * 上传进度百分比
     */
    public Double getProgress() {
        if (totalPartCount == null || totalPartCount == 0) {
            return 0.0;
        }
        int uploaded = uploadedPartCount != null ? uploadedPartCount : 0;
        return (double) uploaded / totalPartCount * 100;
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    /**
     * 是否可以进行
     */
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }

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
         * 分片大小
         */
        private Long partSize;

        /**
         * 分片ETag
         */
        private String eTag;

        /**
         * 是否已上传
         */
        private Boolean uploaded;

        /**
         * 上传时间
         */
        private LocalDateTime uploadTime;

        /**
         * CRC64校验值
         */
        private Long crc64;
    }
}

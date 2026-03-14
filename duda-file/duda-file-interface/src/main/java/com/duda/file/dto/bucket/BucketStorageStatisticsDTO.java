package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Bucket存储统计DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketStorageStatisticsDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 统计日期列表
     */
    private List<StorageStat> dailyStats;

    /**
     * 总文件数
     */
    private Integer totalFileCount;

    /**
     * 总存储大小（字节）
     */
    private Long totalStorageSize;

    /**
     * 存储配额（字节）
     */
    private Long storageQuota;

    /**
     * 使用率（百分比）
     */
    private BigDecimal usagePercentage;

    /**
     * 标准存储大小（字节）
     */
    private Long standardSize;

    /**
     * 低频存储大小（字节）
     */
    private Long iaSize;

    /**
     * 归档存储大小（字节）
     */
    private Long archiveSize;

    /**
     * 冷归档存储大小（字节）
     */
    private Long coldArchiveSize;

    /**
     * 每日统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StorageStat {
        /**
         * 统计日期
         */
        private LocalDate statDate;

        /**
         * 文件数量
         */
        private Integer fileCount;

        /**
         * 存储大小（字节）
         */
        private Long storageSize;

        /**
         * 使用率（百分比）
         */
        private BigDecimal usagePercentage;

        /**
         * 标准存储大小（字节）
         */
        private Long standardSize;

        /**
         * 低频存储大小（字节）
         */
        private Long iaSize;

        /**
         * 归档存储大小（字节）
         */
        private Long archiveSize;

        /**
         * 冷归档存储大小（字节）
         */
        private Long coldArchiveSize;
    }

    /**
     * 获取人类可读的总存储大小
     */
    public String getHumanReadableTotalStorageSize() {
        return formatBytes(totalStorageSize);
    }

    /**
     * 获取人类可读的存储配额
     */
    public String getHumanReadableStorageQuota() {
        return formatBytes(storageQuota);
    }

    private String formatBytes(Long bytes) {
        if (bytes == null) {
            return "unknown";
        }
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}

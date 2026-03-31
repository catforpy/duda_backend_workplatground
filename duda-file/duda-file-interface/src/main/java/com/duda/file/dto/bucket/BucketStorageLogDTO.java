package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Bucket存储日志DTO
 * 对应bucket_storage_log表
 *
 * @author duda
 * @date 2025-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketStorageLogDTO implements Serializable {

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
     * 用户ID
     */
    private Long userId;

    /**
     * 租户ID
     */
    private Long tenantId;

    // ==================== 存储统计 ====================

    /**
     * 文件数量
     */
    private Integer fileCount;

    /**
     * 存储大小（字节）
     */
    private Long storageSize;

    /**
     * 存储配额（字节）
     */
    private Long storageQuota;

    /**
     * 使用率（百分比）
     */
    private BigDecimal usagePercentage;

    // ==================== 存储类型统计 ====================

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

    // ==================== 统计时间 ====================

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 统计小时（0-23，NULL表示日统计）
     */
    private Integer statHour;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    // ==================== 业务方法 ====================

    /**
     * 获取人类可读的存储大小
     */
    public String getHumanReadableStorageSize() {
        return formatBytes(storageSize);
    }

    /**
     * 获取人类可读的标准存储大小
     */
    public String getHumanReadableStandardSize() {
        return formatBytes(standardSize);
    }

    /**
     * 获取人类可读的IA存储大小
     */
    public String getHumanReadableIaSize() {
        return formatBytes(iaSize);
    }

    private String formatBytes(Long bytes) {
        if (bytes == null) {
            return "0 B";
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

package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Bucket存储统计日志实体
 * 对应bucket_storage_log表
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketStorageLog implements Serializable {

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

    /**
     * 获取人类可读的存储大小
     */
    public String getHumanReadableSize() {
        if (storageSize == null) {
            return "unknown";
        }
        if (storageSize < 1024) {
            return storageSize + " B";
        } else if (storageSize < 1024 * 1024) {
            return String.format("%.2f KB", storageSize / 1024.0);
        } else if (storageSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", storageSize / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", storageSize / (1024.0 * 1024 * 1024));
        }
    }
}

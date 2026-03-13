package com.duda.file.manager.support;

import com.duda.file.common.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 配额验证器
 * 用于验证Bucket和用户的存储配额
 *
 * @author duda
 * @date 2025-03-13
 */
@Slf4j
@Component
public class QuotaValidator {

    /**
     * 验证Bucket配额
     *
     * @param bucketName Bucket名称
     * @param currentSize 当前已使用大小(字节)
     * @param currentCount 当前文件数量
     * @param maxSize 最大存储容量(字节,null表示不限制)
     * @param maxCount 最大文件数量(null表示不限制)
     * @param uploadSize 上传文件大小(字节)
     * @throws StorageException 超出配额时抛出
     */
    public void validateBucketQuota(String bucketName, Long currentSize, Long currentCount,
                                   Long maxSize, Integer maxCount, Long uploadSize) throws StorageException {
        // 验证存储容量
        if (maxSize != null && maxSize > 0) {
            long newSize = (currentSize != null ? currentSize : 0) + uploadSize;
            if (newSize > maxSize) {
                throw new StorageException("QUOTA_EXCEEDED",
                        String.format("Bucket storage quota exceeded: %s (max: %d bytes, used: %d bytes, upload: %d bytes)",
                                bucketName, maxSize, currentSize, uploadSize));
            }
        }

        // 验证文件数量
        if (maxCount != null && maxCount > 0) {
            long newCount = (currentCount != null ? currentCount : 0) + 1;
            if (newCount > maxCount) {
                throw new StorageException("QUOTA_EXCEEDED",
                        String.format("Bucket file count quota exceeded: %s (max: %d files, used: %d files)",
                                bucketName, maxCount, currentCount));
            }
        }
    }

    /**
     * 验证用户配额
     *
     * @param userId 用户ID
     * @param currentUserSize 用户当前已使用大小(字节)
     * @param currentUserCount 用户当前文件数量
     * @param userMaxSize 用户最大存储容量(字节,null表示不限制)
     * @param userMaxCount 用户最大文件数量(null表示不限制)
     * @param uploadSize 上传文件大小(字节)
     * @throws StorageException 超出配额时抛出
     */
    public void validateUserQuota(Long userId, Long currentUserSize, Long currentUserCount,
                                Long userMaxSize, Integer userMaxCount, Long uploadSize) throws StorageException {
        // 验证用户存储容量
        if (userMaxSize != null && userMaxSize > 0) {
            long newSize = (currentUserSize != null ? currentUserSize : 0) + uploadSize;
            if (newSize > userMaxSize) {
                throw new StorageException("USER_QUOTA_EXCEEDED",
                        String.format("User storage quota exceeded: user %d (max: %d bytes, used: %d bytes, upload: %d bytes)",
                                userId, userMaxSize, currentUserSize, uploadSize));
            }
        }

        // 验证用户文件数量
        if (userMaxCount != null && userMaxCount > 0) {
            long newCount = (currentUserCount != null ? currentUserCount : 0) + 1;
            if (newCount > userMaxCount) {
                throw new StorageException("USER_QUOTA_EXCEEDED",
                        String.format("User file count quota exceeded: user %d (max: %d files, used: %d files)",
                                userId, userMaxCount, currentUserCount));
            }
        }
    }

    /**
     * 验证单文件大小限制
     *
     * @param fileSize 文件大小(字节)
     * @param maxSize 最大文件大小(字节,null表示不限制)
     * @throws StorageException 超过限制时抛出
     */
    public void validateFileSize(Long fileSize, Long maxSize) throws StorageException {
        if (maxSize != null && maxSize > 0 && fileSize != null && fileSize > maxSize) {
            throw new StorageException("FILE_SIZE_EXCEEDED",
                    String.format("File size exceeded: %d bytes (max: %d bytes)", fileSize, maxSize));
        }
    }

    /**
     * 计算配额使用率
     *
     * @param current 当前使用量
     * @param max 最大配额
     * @return 使用率百分比(0-100)
     */
    public Double calculateUsageRate(Long current, Long max) {
        if (max == null || max == 0) {
            return 0.0;
        }
        long currentVal = current != null ? current : 0;
        return (double) currentVal / max * 100;
    }

    /**
     * 检查是否接近配额限制
     *
     * @param current 当前使用量
     * @param max 最大配额
     * @param threshold 阈值百分比(0-100)
     * @return 是否接近配额限制
     */
    public boolean isNearQuotaLimit(Long current, Long max, double threshold) {
        if (max == null || max == 0) {
            return false;
        }
        double usageRate = calculateUsageRate(current, max);
        return usageRate >= threshold;
    }

    /**
     * 获取配额警告信息
     *
     * @param current 当前使用量
     * @param max 最大配额
     * @return 警告信息,如果未超过阈值则返回null
     */
    public String getQuotaWarning(Long current, Long max) {
        if (max == null || max == 0) {
            return null;
        }

        double usageRate = calculateUsageRate(current, max);

        if (usageRate >= 90) {
            return String.format("警告: 存储空间使用率已达%.1f%% (%d/%d字节)", usageRate, current, max);
        } else if (usageRate >= 80) {
            return String.format("提醒: 存储空间使用率已达%.1f%% (%d/%d字节)", usageRate, current, max);
        }

        return null;
    }

    /**
     * 格式化字节大小
     *
     * @param bytes 字节数
     * @return 格式化后的字符串
     */
    public String formatBytes(Long bytes) {
        if (bytes == null) {
            return "unknown";
        }
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else if (bytes < 1024L * 1024 * 1024 * 1024) {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        } else {
            return String.format("%.2f TB", bytes / (1024.0 * 1024 * 1024 * 1024));
        }
    }
}

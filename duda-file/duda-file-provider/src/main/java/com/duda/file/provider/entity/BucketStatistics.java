package com.duda.file.provider.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bucket统计信息实体
 * 对应bucket_statistics表
 *
 * @author duda
 * @date 2025-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("bucket_statistics")
public class BucketStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 地域
     */
    private String region;

    /**
     * 存储类型：STANDARD/IA/ARCHIVE
     */
    private String storageType;

    // ==================== 文件统计 ====================

    /**
     * 总文件数量
     */
    private Integer totalFileCount;

    /**
     * 总存储容量（字节）
     */
    private Long totalStorageSize;

    /**
     * 图片数量
     */
    private Integer imageCount;

    /**
     * 视频数量
     */
    private Integer videoCount;

    /**
     * 文档数量
     */
    private Integer documentCount;

    /**
     * 其他文件数量
     */
    private Integer otherCount;

    // ==================== 流量统计 ====================

    /**
     * 总流量（字节）
     */
    private Long totalTrafficBytes;

    /**
     * 上传流量（字节）
     */
    private Long uploadTrafficBytes;

    /**
     * 下载流量（字节）
     */
    private Long downloadTrafficBytes;

    // ==================== 费用统计 ====================

    /**
     * 存储费用（元）
     */
    private BigDecimal storageFee;

    /**
     * 流量费用（元）
     */
    private BigDecimal trafficFee;

    /**
     * 请求费用（元）
     */
    private BigDecimal requestFee;

    /**
     * 总费用（元）
     */
    private BigDecimal totalFee;

    // ==================== 时间统计 ====================

    /**
     * 最后同步时间
     */
    private LocalDateTime lastSyncTime;

    /**
     * 最后上传时间
     */
    private LocalDateTime lastUploadTime;

    /**
     * 最后下载时间
     */
    private LocalDateTime lastDownloadTime;

    /**
     * 最后删除时间
     */
    private LocalDateTime lastDeleteTime;

    /**
     * 标签（JSON格式）
     */
    private String tags;

    /**
     * 状态：ACTIVE/SUSPENDED/DELETED
     */
    private String status;

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
     * 获取总文件数（包含所有类型）
     */
    public Integer getTotalCount() {
        if (totalFileCount != null) {
            return totalFileCount;
        }
        // 如果totalFileCount为空，计算各类型文件数总和
        int sum = 0;
        if (imageCount != null) sum += imageCount;
        if (videoCount != null) sum += videoCount;
        if (documentCount != null) sum += documentCount;
        if (otherCount != null) sum += otherCount;
        return sum;
    }

    /**
     * 判断是否为活跃状态
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * 获取人类可读的存储大小
     */
    public String getHumanReadableStorageSize() {
        return formatBytes(totalStorageSize);
    }

    /**
     * 获取人类可读的总流量
     */
    public String getHumanReadableTotalTraffic() {
        return formatBytes(totalTrafficBytes);
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

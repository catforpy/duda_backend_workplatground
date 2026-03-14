package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Bucket流量统计日志实体
 * 对应bucket_traffic_log表
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketTrafficLog implements Serializable {

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
     * 上传流量（字节）
     */
    private Long uploadTraffic;

    /**
     * 下载流量（字节）
     */
    private Long downloadTraffic;

    /**
     * 总流量（字节）
     */
    private Long totalTraffic;

    /**
     * 总请求次数
     */
    private Integer requestCount;

    /**
     * 上传次数
     */
    private Integer uploadCount;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * HEAD请求次数
     */
    private Integer headCount;

    /**
     * 其他请求次数
     */
    private Integer otherCount;

    /**
     * 图片流量（字节）
     */
    private Long imageTraffic;

    /**
     * 视频流量（字节）
     */
    private Long videoTraffic;

    /**
     * 文档流量（字节）
     */
    private Long documentTraffic;

    /**
     * 其他流量（字节）
     */
    private Long otherTraffic;

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
     * 获取人类可读的流量大小
     */
    public String getHumanReadableTraffic() {
        return formatBytes(totalTraffic);
    }

    /**
     * 获取人类可读的上传流量
     */
    public String getHumanReadableUploadTraffic() {
        return formatBytes(uploadTraffic);
    }

    /**
     * 获取人类可读的下载流量
     */
    public String getHumanReadableDownloadTraffic() {
        return formatBytes(downloadTraffic);
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

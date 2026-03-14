package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
    import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Bucket流量统计DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketTrafficStatisticsDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 统计日期列表
     */
    private List<TrafficStat> dailyStats;

    /**
     * 总上传流量（字节）
     */
    private Long totalUploadTraffic;

    /**
     * 总下载流量（字节）
     */
    private Long totalDownloadTraffic;

    /**
     * 总流量（字节）
     */
    private Long totalTraffic;

    /**
     * 总请求次数
     */
    private Integer totalRequestCount;

    /**
     * 上传次数
     */
    private Integer totalUploadCount;

    /**
     * 下载次数
     */
    private Integer totalDownloadCount;

    /**
     * 每日统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrafficStat {
        /**
         * 统计日期
         */
        private LocalDate statDate;

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
         * 请求次数
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
    }

    /**
     * 获取人类可读的总流量
     */
    public String getHumanReadableTotalTraffic() {
        return formatBytes(totalTraffic);
    }

    /**
     * 获取人类可读的上传流量
     */
    public String getHumanReadableTotalUploadTraffic() {
        return formatBytes(totalUploadTraffic);
    }

    /**
     * 获取人类可读的下载流量
     */
    public String getHumanReadableTotalDownloadTraffic() {
        return formatBytes(totalDownloadTraffic);
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

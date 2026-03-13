package com.duda.file.dto.bucket;

import lombok.Builder;
import lombok.Data;

/**
 * Bucket用量统计DTO
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Data
@Builder
public class BucketStatisticsDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 当前文件数量
     */
    private Long fileCount;

    /**
     * 当前存储使用量（字节）
     */
    private Long storageUsed;

    /**
     * 存储配额（字节）
     */
    private Long storageQuota;

    /**
     * 存储使用率（百分比）
     */
    private Double usagePercentage;

    /**
     * 今日上传文件数
     */
    private Long todayUploadCount;

    /**
     * 今日上传流量（字节）
     */
    private Long todayUploadTraffic;

    /**
     * 今日下载流量（字节）
     */
    private Long todayDownloadTraffic;

    /**
     * 本月请求次数
     */
    private Long monthRequestCount;

    /**
     * 最后更新时间
     */
    private Long lastUpdateTime;
}

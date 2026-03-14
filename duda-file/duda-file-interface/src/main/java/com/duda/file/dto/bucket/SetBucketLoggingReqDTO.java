package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设置Bucket日志转存请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketLoggingReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 是否启用日志转存
     */
    private Boolean enabled;

    /**
     * 目标Bucket名称（日志存储的Bucket）
     */
    private String targetBucket;

    /**
     * 日志文件前缀（可选）
     * 如：log-
     */
    private String logPrefix;

    /**
     * 日志存储类型（可选）
     * 如：IA（低频访问）
     */
    private String logStorageClass;
}

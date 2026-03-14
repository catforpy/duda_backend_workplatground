package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设置Bucket存储空间清单请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketInventoryReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 清单规则ID
     */
    private String ruleId;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 清单的目标Bucket
     */
    private String destinationBucket;

    /**
     * 清单文件的前缀
     */
    private String inventoryPrefix;

    /**
     * 清单频率
     * - Daily: 每日
     * - Weekly: 每周
     */
    private String schedule;

    /**
     * 清单包含的字段
     * - Size: 文件大小
     * - LastModifiedDate: 最后修改时间
     * - ETag: ETag
     * - StorageClass: 存储类型
     * - IsMultipartUploaded: 是否分片上传
     * - EncryptionStatus: 加密状态
     */
    private java.util.List<String> fields;

    /**
     * 是否包含Object版本
     */
    private Boolean includeObjectVersions;

    /**
     * 过滤器（可选）
     */
    private InventoryFilter filter;

    /**
     * 清单过滤器
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryFilter {
        /**
         * 前缀（只匹配该前缀的对象）
         */
        private String prefix;

        /**
         * 最后修改时间开始
         */
        private Long lastModifiedBeginTime;

        /**
         * 最后修改时间结束
         */
        private Long lastModifiedEndTime;

        /**
         * 最小文件大小（字节）
         */
        private Long lowerSizeBound;

        /**
         * 最大文件大小（字节）
         */
        private Long upperSizeBound;

        /**
         * 存储类型（如：Standard, IA, Archive）
         */
        private String storageClass;
    }
}

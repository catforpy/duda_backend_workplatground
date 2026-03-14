package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设置Bucket跨区域复制请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketReplicationReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 复制规则列表
     */
    private List<ReplicationRule> rules;

    /**
     * 跨区域复制规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplicationRule {

        /**
         * 规则ID
         */
        private String ruleId;

        /**
         * 是否启用
         */
        private Boolean enabled;

        /**
         * 目标Bucket名称
         */
        private String destinationBucket;

        /**
         * 目标Bucket所在区域
         */
        private String destinationRegion;

        /**
         * 复制前缀（可选，不填则复制整个Bucket）
         */
        private String prefix;

        /**
         * 是否复制历史数据
         */
        private Boolean historicalObjectReplication;

        /**
     * 同步策略
     * - WMS: 写时复制
     * - STANDARD: 标准复制
     */
        private String syncStrategy;
    }
}

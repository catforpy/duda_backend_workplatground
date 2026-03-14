package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设置Bucket生命周期规则请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketLifecycleReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 生命周期规则列表
     */
    private List<LifecycleRule> rules;

    /**
     * 生命周期规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LifecycleRule {

        /**
         * 规则ID（唯一标识）
         */
        private String ruleId;

        /**
         * 是否启用
         */
        private Boolean enabled;

        /**
         * 匹配前缀（可选，不填则应用于整个Bucket）
         */
        private String prefix;

        /**
         * 动作类型
         * - EXPIRATION: 过期删除
         * - TRANSITION: 转换存储类型
         */
        private Action action;

        /**
         * 动作详情
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Action {

            /**
             * 过期天数（适用于EXPIRATION）
             */
            private Integer days;

            /**
             * 过期日期（适用于EXPIRATION，指定具体日期）
             */
            private String date;

            /**
             * 转换的目标存储类型（适用于TRANSITION）
             * - IA: 低频访问
             * - ARCHIVE: 归档
             * - COLD_ARCHIVE: 冷归档
             */
            private String storageClass;

            /**
             * 转换天数（适用于TRANSITION）
             */
            private Integer transitionDays;
        }
    }
}

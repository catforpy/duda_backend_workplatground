package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设置Bucket静态网站托管请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketWebsiteReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 索引页面（如：index.html）
     */
    private String indexDocument;

    /**
     * 错误页面（如：error.html）
     */
    private String errorDocument;

    /**
     * 是否支持重定向
     */
    private Boolean supportRedirect;

    /**
     * 重定向规则（如果支持重定向）
     */
    private RedirectRule redirectRule;

    /**
     * 重定向规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedirectRule {
        /**
         * 重定向条件
         */
        private RedirectCondition condition;

        /**
         * 重定向类型
         * - Mirror: 镜像回源
         * - External: 外部重定向
         */
        private String redirectType;

        /**
         * 目标地址
         */
        private String redirectLocation;

        /**
         * HTTP状态码（如：301, 302）
         */
        private Integer httpStatusCode;
    }

    /**
     * 重定向条件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedirectCondition {
        /**
         * 键前缀（匹配条件）
         */
        private String keyPrefixEquals;

        /**
         * HTTP错误码（如：404）
         */
        private Integer httpErrorCodeReturnedEquals;
    }
}

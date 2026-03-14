package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设置Bucket CORS规则请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketCORSReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * CORS规则列表
     */
    private List<CORSRule> rules;

    /**
     * CORS规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CORSRule {

        /**
         * 允许的源（如：https://example.com, *）
         */
        private String allowedOrigin;

        /**
         * 允许的HTTP方法（如：GET, POST, PUT, DELETE, HEAD）
         */
        private List<String> allowedMethods;

        /**
         * 允许的请求头（如：*）
         */
        private String allowedHeaders;

        /**
         * 暴露的响应头（如：ETag）
         */
        private String exposeHeaders;

        /**
         * 缓存时间（秒）
         */
        private Integer maxAgeSeconds;
    }
}

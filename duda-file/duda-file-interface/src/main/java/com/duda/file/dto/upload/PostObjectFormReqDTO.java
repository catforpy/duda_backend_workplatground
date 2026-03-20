package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import java.io.Serializable;
import lombok.Builder;
import java.io.Serializable;
import lombok.Data;
import java.io.Serializable;
import lombok.NoArgsConstructor;
import java.io.Serializable;

import java.util.Map;
import java.io.Serializable;

/**
 * PostObject表单上传请求DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostObjectFormReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     * 可以包含变量:${filename}
     */
    private String objectKey;

    /**
     * 过期时间(秒)
     * 范围: 1-604800 (1秒-7天)
     * 默认: 3600 (1小时)
     */
    private Integer expiration;

    /**
     * 文件大小限制(字节)
     * min: 最小值
     * max: 最大值
     */
    private SizeLimit sizeLimit;

    /**
     * 允许的文件类型(MIME类型)
     */
    private String[] allowedContentTypes;

    /**
     * 不允许的文件类型(MIME类型)
     */
    private String[] disallowedContentTypes;

    /**
     * 回调配置
     */
    private CallbackConfig callbackConfig;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户自定义元数据
     */
    private Map<String, String> userMetadata;

    /**
     * 是否使用HTTPS
     */
    private Boolean httpsOnly;

    /**
     * 自定义表单字段
     */
    private Map<String, String> customFields;

    /**
     * 扩展参数
     */
    private Map<String, Object> extra;

    /**
     * 文件大小限制
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SizeLimit {
        /**
         * 最小文件大小(字节)
         */
        private Long min;

        /**
         * 最大文件大小(字节)
         */
        private Long max;
    }

    /**
     * 回调配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CallbackConfig {
        /**
         * 回调URL
         */
        private String callbackUrl;

        /**
         * 回调Body
         * 支持变量:${bucket}, ${object}, ${etag}, ${size}, ${mimeType}
         */
        private String callbackBody;

        /**
         * 回调Body类型
         * - application/x-www-form-urlencoded
         * - application/json
         */
        private String callbackBodyType;

        /**
         * 回调变量
         */
        private Map<String, String> callbackVar;
    }

    /**
     * 构建默认请求
     */
    public static PostObjectFormReqDTO buildDefault(String bucketName, String objectKey, Long userId) {
        return PostObjectFormReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .userId(userId)
                .expiration(3600)
                .httpsOnly(true)
                .build();
    }

    /**
     * 构建带大小限制的请求
     */
    public static PostObjectFormReqDTO buildWithSizeLimit(String bucketName, String objectKey, Long userId,
                                                           Long minSize, Long maxSize) {
        return PostObjectFormReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .userId(userId)
                .expiration(3600)
                .sizeLimit(SizeLimit.builder().min(minSize).max(maxSize).build())
                .httpsOnly(true)
                .build();
    }

    /**
     * 验证请求参数
     */
    public boolean validate() {
        if (bucketName == null || bucketName.isEmpty()) {
            return false;
        }
        if (objectKey == null || objectKey.isEmpty()) {
            return false;
        }
        if (expiration != null && (expiration < 1 || expiration > 604800)) {
            return false;
        }
        return true;
    }
}

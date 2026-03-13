package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 预签名URL请求DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlReqDTO {

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 过期时间(秒)
     * 范围: 1-604800 (1秒-7天)
     * 默认: 3600 (1小时)
     */
    private Integer expiration;

    /**
     * HTTP方法
     * - GET: 下载
     * - PUT: 上传
     * - POST: 表单上传
     * - DELETE: 删除
     * - HEAD: 获取元数据
     */
    private String method;

    /**
     * 响应头限制
     * 只有包含这些响应头的请求才会成功
     */
    private Map<String, String> responseHeaders;

    /**
     * 用户自定义元数据
     * 仅对PUT方法有效
     */
    private Map<String, String> userMetadata;

    /**
     * Content-Type
     * 仅对PUT方法有效
     */
    private String contentType;

    /**
     * 是否使用HTTPS
     */
    private Boolean httpsOnly;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 查询参数
     * 会添加到URL的查询字符串中
     */
    private Map<String, String> queryParameters;

    /**
     * 扩展参数
     */
    private Map<String, Object> extra;

    /**
     * HTTP方法枚举
     */
    public enum HttpMethod {
        GET("GET", "下载"),
        PUT("PUT", "上传"),
        POST("POST", "表单上传"),
        DELETE("DELETE", "删除"),
        HEAD("HEAD", "获取元数据");

        private final String code;
        private final String description;

        HttpMethod(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 构建上传URL请求
     */
    public static PresignedUrlReqDTO buildForUpload(String bucketName, String objectKey, Long userId, int expiration) {
        return PresignedUrlReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .userId(userId)
                .method(HttpMethod.PUT.getCode())
                .expiration(expiration)
                .httpsOnly(true)
                .build();
    }

    /**
     * 构建下载URL请求
     */
    public static PresignedUrlReqDTO buildForDownload(String bucketName, String objectKey, Long userId, int expiration) {
        return PresignedUrlReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .userId(userId)
                .method(HttpMethod.GET.getCode())
                .expiration(expiration)
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
        if (method == null || method.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否为上传请求
     */
    public boolean isUploadRequest() {
        return HttpMethod.PUT.getCode().equals(method) ||
                HttpMethod.POST.getCode().equals(method);
    }

    /**
     * 判断是否为下载请求
     */
    public boolean isDownloadRequest() {
        return HttpMethod.GET.getCode().equals(method);
    }
}

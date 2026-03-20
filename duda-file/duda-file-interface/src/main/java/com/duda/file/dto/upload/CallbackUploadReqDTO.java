package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import java.io.Serializable;
import lombok.Builder;
import java.io.Serializable;
import lombok.Data;
import java.io.Serializable;
import lombok.NoArgsConstructor;
import java.io.Serializable;

import java.util.List;
import java.io.Serializable;
import java.util.Map;
import java.io.Serializable;

/**
 * 回调上传请求DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallbackUploadReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 上传方式
     * - POST: 表单上传
     * - PUT: PUT上传
     * - STS: STS临时凭证上传
     */
    private String uploadMethod;

    /**
     * 过期时间(秒)
     */
    private Integer expiration;

    /**
     * 回调URL
     */
    private String callbackUrl;

    /**
     * 回调Body模板
     * 支持变量:${bucket}, ${object}, ${etag}, ${size}, ${mimeType}, ${filename}
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
     * key: 变量名
     * value: 变量值
     */
    private Map<String, String> callbackVar;

    /**
     * 允许的文件类型(MIME类型)
     */
    private List<String> allowedContentTypes;

    /**
     * 文件大小限制
     */
    private PostObjectFormReqDTO.SizeLimit sizeLimit;

    /**
     * 用户自定义元数据
     */
    private Map<String, String> userMetadata;

    /**
     * 是否使用HTTPS
     */
    private Boolean httpsOnly;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 扩展参数
     */
    private Map<String, Object> extra;

    /**
     * 构建表单上传请求
     */
    public static CallbackUploadReqDTO buildFormUpload(String bucketName, String objectKey,
                                                         String callbackUrl, Long userId) {
        return CallbackUploadReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .uploadMethod("POST")
                .expiration(3600)
                .callbackUrl(callbackUrl)
                .callbackBodyType("application/json")
                .httpsOnly(true)
                .userId(userId)
                .build();
    }

    /**
     * 构建PUT上传请求
     */
    public static CallbackUploadReqDTO buildPutUpload(String bucketName, String objectKey,
                                                       String callbackUrl, Long userId) {
        return CallbackUploadReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .uploadMethod("PUT")
                .expiration(3600)
                .callbackUrl(callbackUrl)
                .callbackBodyType("application/json")
                .httpsOnly(true)
                .userId(userId)
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
        if (uploadMethod == null || uploadMethod.isEmpty()) {
            return false;
        }
        if (callbackUrl == null || callbackUrl.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否为表单上传
     */
    public boolean isFormUpload() {
        return "POST".equals(uploadMethod);
    }

    /**
     * 判断是否为PUT上传
     */
    public boolean isPutUpload() {
        return "PUT".equals(uploadMethod);
    }
}

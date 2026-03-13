package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 回调上传DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallbackUploadDTO {

    /**
     * 上传地址
     */
    private String uploadUrl;

    /**
     * 上传表单字段
     * key: 字段名
     * value: 字段值
     */
    private Map<String, String> formData;

    /**
     * 文件字段名
     * 默认: file
     */
    private String fileFieldName;

    /**
     * 上传方式
     * - POST: 表单上传
     * - PUT: PUT上传
     */
    private String uploadMethod;

    /**
     * 过期时间(时间戳)
     */
    private Long expirationTime;

    /**
     * 有效期(秒)
     */
    private Long durationSeconds;

    /**
     * 回调URL
     */
    private String callbackUrl;

    /**
     * 回调Body
     */
    private String callbackBody;

    /**
     * 回调Body类型
     */
    private String callbackBodyType;

    /**
     * 回调签名
     */
    private String callbackSignature;

    /**
     * 扩展信息
     */
    private Map<String, Object> extra;

    /**
     * 判断是否已过期
     */
    public boolean isExpired() {
        if (expirationTime == null) {
            return false;
        }
        return System.currentTimeMillis() > expirationTime;
    }

    /**
     * 获取剩余有效时间(秒)
     */
    public Long getRemainingSeconds() {
        if (expirationTime == null) {
            return null;
        }
        long remaining = (expirationTime - System.currentTimeMillis()) / 1000;
        return remaining > 0 ? remaining : 0;
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

package com.duda.file.dto.upload;

import com.duda.file.dto.object.ObjectMetadataDTO;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import lombok.Builder;
import java.io.Serializable;
import lombok.Data;
import java.io.Serializable;
import lombok.NoArgsConstructor;
import java.io.Serializable;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.io.Serializable;

/**
 * 追加对象请求DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppendObjectReqDTO implements Serializable {

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
     * 追加数据输入流
     */
    private InputStream inputStream;

    /**
     * 追加位置
     * 第一次追加时为0,之后使用上次返回的position
     */
    private Long position;

    /**
     * 追加数据大小(字节)
     */
    private Long contentLength;

    /**
     * MIME类型
     */
    private String contentType;

    /**
     * 对象元数据
     */
    private ObjectMetadataDTO metadata;

    /**
     * 用户自定义元数据
     */
    private Map<String, String> userMetadata;

    /**
     * 服务端加密算法
     */
    private String serverSideEncryption;

    /**
     * KMS密钥ID(使用SSE-KMS时)
     */
    private String kmsKeyId;

    /**
     * Content-MD5
     */
    private String contentMD5;

    /**
     * 进度监听器
     */
    private SimpleUploadReqDTO.ProgressListener progressListener;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 扩展参数
     */
    private Map<String, Object> extra;

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
        if (inputStream == null) {
            return false;
        }
        if (contentLength == null || contentLength <= 0) {
            return false;
        }
        if (position == null || position < 0) {
            return false;
        }
        return true;
    }

    /**
     * 是否为第一次追加
     */
    public boolean isFirstAppend() {
        return position != null && position == 0;
    }
}

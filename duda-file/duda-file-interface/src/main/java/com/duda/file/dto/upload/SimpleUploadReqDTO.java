package com.duda.file.dto.upload;

import com.duda.file.dto.object.ObjectMetadataDTO;
import com.duda.file.enums.StorageClass;
import com.duda.file.enums.AclType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * 简单上传请求DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleUploadReqDTO implements Serializable {

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
     * 文件输入流
     */
    private InputStream inputStream;

    /**
     * 文件大小(字节)
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
     * 存储类型
     */
    private StorageClass storageClass;

    /**
     * 访问权限
     */
    private AclType acl;

    /**
     * 用户自定义元数据
     * key: 元数据键(以x-oss-meta-开头)
     * value: 元数据值
     */
    private Map<String, String> userMetadata;

    /**
     * 服务端加密算法
     * - AES256: SSE-OSS加密
     * - KMS: SSE-KMS加密
     */
    private String serverSideEncryption;

    /**
     * KMS密钥ID(使用SSE-KMS时)
     */
    private String kmsKeyId;

    /**
     * Content-MD5
     * 用于验证数据完整性
     */
    private String contentMD5;

    /**
     * 是否启用CRC64校验
     */
    private Boolean enableCRC64;

    /**
     * 回调配置(上传成功后回调)
     */
    private CallbackConfig callbackConfig;

    /**
     * 进度监听器
     */
    private ProgressListener progressListener;

    /**
     * 用户ID(用于权限验证)
     */
    private Long userId;

    /**
     * 扩展参数
     */
    private Map<String, Object> extra;

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
         * 回调主机
         */
        private String callbackHost;

        /**
         * 回调变量
         */
        private Map<String, String> callbackVar;
    }

    /**
     * 进度监听器接口
     */
    public interface ProgressListener {
        /**
         * 上传进度变更
         *
         * @param bytesAlreadyWritten 已写入字节数
         * @param totalBytes         总字节数
         * @param stillWritingBytes  剩余字节数
         */
        void progressChanged(long bytesAlreadyWritten, long totalBytes, long stillWritingBytes);

        /**
         * 上传成功
         */
        void onSucceed();

        /**
         * 上传失败
         *
         * @param exception 异常信息
         */
        void onFailed(Exception exception);
    }
}

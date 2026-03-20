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
 * 断点续传请求DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeUploadReqDTO implements Serializable {

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
     * 本地文件路径
     */
    private String localFilePath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 分片大小(字节)
     * 不设置则自动计算
     */
    private Long partSize;

    /**
     * MIME类型
     */
    private String contentType;

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
     * 是否启用CRC64校验
     */
    private Boolean enableCRC64;

    /**
     * 并发上传线程数
     * 默认: 3
     */
    private Integer concurrentThreads;

    /**
     * 进度监听器
     */
    private SimpleUploadReqDTO.ProgressListener progressListener;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否断点续传
     * - true: 检查是否有未完成的记录并恢复
     * - false: 创建新的上传
     */
    private Boolean resume;

    /**
     * 扩展参数
     */
    private Map<String, Object> extra;

    /**
     * 构建默认请求
     */
    public static ResumeUploadReqDTO buildDefault(String bucketName, String objectKey,
                                                   String localFilePath, Long userId) {
        return ResumeUploadReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .localFilePath(localFilePath)
                .userId(userId)
                .concurrentThreads(3)
                .resume(true)
                .build();
    }

    /**
     * 构建新上传请求
     */
    public static ResumeUploadReqDTO buildNewUpload(String bucketName, String objectKey,
                                                     String localFilePath, Long fileSize, Long userId) {
        return ResumeUploadReqDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .localFilePath(localFilePath)
                .fileSize(fileSize)
                .userId(userId)
                .concurrentThreads(3)
                .resume(false)
                .build();
    }

    /**
     * 计算建议的分片大小
     */
    public long calculateRecommendedPartSize() {
        if (fileSize == null) {
            return 1024 * 1024; // 默认1MB
        }

        // 小文件(<100MB): 使用100KB分片
        if (fileSize < 100 * 1024 * 1024) {
            return 100 * 1024;
        }
        // 中等文件(100MB-1GB): 使用1MB分片
        else if (fileSize < 1024 * 1024 * 1024) {
            return 1024 * 1024;
        }
        // 大文件(1GB-10GB): 使用5MB分片
        else if (fileSize < 10L * 1024 * 1024 * 1024) {
            return 5 * 1024 * 1024;
        }
        // 超大文件(>10GB): 使用10MB分片
        else {
            return 10 * 1024 * 1024;
        }
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
        if (localFilePath == null || localFilePath.isEmpty()) {
            return false;
        }
        if (userId == null) {
            return false;
        }
        return true;
    }
}

package com.duda.file.dto.upload;

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

/**
 * 上传分片请求DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadPartReqDTO implements Serializable {

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
     * 上传ID
     */
    private String uploadId;

    /**
     * 分片号(从1开始)
     */
    private Integer partNumber;

    /**
     * 分片数据输入流
     */
    private InputStream inputStream;

    /**
     * 分片大小(字节)
     */
    private Long partSize;

    /**
     * 分片MD5
     * 用于验证数据完整性
     */
    private String contentMD5;

    /**
     * 是否启用CRC64校验
     */
    private Boolean enableCRC64;

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
    private java.util.Map<String, Object> extra;

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
        if (uploadId == null || uploadId.isEmpty()) {
            return false;
        }
        if (partNumber == null || partNumber < 1) {
            return false;
        }
        if (inputStream == null) {
            return false;
        }
        if (partSize == null || partSize <= 0) {
            return false;
        }
        return true;
    }
}

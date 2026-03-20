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
 * 完成分片上传请求DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMultipartUploadReqDTO implements Serializable {

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
     * 分片ETag列表
     * key: partNumber (分片号,从1开始)
     * value: eTag (分片ETag)
     */
    private Map<Integer, String> partETags;

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
        if (uploadId == null || uploadId.isEmpty()) {
            return false;
        }
        if (partETags == null || partETags.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 获取分片总数
     */
    public int getPartCount() {
        return partETags != null ? partETags.size() : 0;
    }

    /**
     * 判断是否包含指定分片
     */
    public boolean containsPart(int partNumber) {
        return partETags != null && partETags.containsKey(partNumber);
    }

    /**
     * 获取指定分片的ETag
     */
    public String getPartETag(int partNumber) {
        return partETags != null ? partETags.get(partNumber) : null;
    }
}

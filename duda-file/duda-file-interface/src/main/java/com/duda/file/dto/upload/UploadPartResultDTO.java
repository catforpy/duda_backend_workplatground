package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import java.io.Serializable;
import lombok.Builder;
import java.io.Serializable;
import lombok.Data;
import java.io.Serializable;
import lombok.NoArgsConstructor;
import java.io.Serializable;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * 上传分片结果DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadPartResultDTO implements Serializable {

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
     * 分片号
     */
    private Integer partNumber;

    /**
     * 分片ETag
     * 用于完成分片上传时验证
     */
    private String eTag;

    /**
     * 分片大小(字节)
     */
    private Long partSize;

    /**
     * CRC64校验值
     */
    private Long crc64;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误码(失败时)
     */
    private String errorCode;

    /**
     * 错误信息(失败时)
     */
    private String errorMessage;

    /**
     * 是否启用CRC64
     */
    private Boolean crc64Enabled;

    /**
     * 判断上传是否成功
     */
    public boolean isSuccess() {
        return success != null && success;
    }

    /**
     * 获取分片摘要信息
     */
    public String getSummary() {
        return String.format("Part %d: %d bytes, ETag=%s",
                partNumber, partSize, eTag);
    }
}

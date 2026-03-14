package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设置Bucket传输加速结果DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketTransferAccelerationResultDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 是否启用传输加速
     */
    private Boolean enabled;

    /**
     * 传输加速加速域名（如果有）
     * 如：my-bucket.oss-accelerate.aliyuncs.com
     */
    private String accelerateEndpoint;
}

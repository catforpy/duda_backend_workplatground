package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设置Bucket传输加速请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketTransferAccelerationReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 是否启用传输加速
     */
    private Boolean enabled;
}

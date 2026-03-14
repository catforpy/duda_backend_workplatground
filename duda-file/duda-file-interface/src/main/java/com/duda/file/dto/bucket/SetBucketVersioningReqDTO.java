package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设置Bucket版本控制请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketVersioningReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 版本控制状态
     * - Enabled: 开启版本控制
     * - Suspended: 暂停版本控制
     */
    private String status;
}

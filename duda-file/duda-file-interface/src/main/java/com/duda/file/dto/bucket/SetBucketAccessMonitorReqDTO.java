package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设置Bucket访问跟踪请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketAccessMonitorReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 是否启用访问跟踪
     */
    private Boolean enabled;

    /**
     * 访问跟踪状态
     * - Enabled: 启用
     * - Suspended: 暂停
     */
    private String status;
}

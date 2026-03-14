package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设置Bucket合规保留策略请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketWORMReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 合规保留策略ID
     */
    private String policyId;

    /**
     * 保护天数（1-25500天）
     */
    private Integer retentionDays;

    /**
     * 是否启用
     */
    private Boolean enabled;
}

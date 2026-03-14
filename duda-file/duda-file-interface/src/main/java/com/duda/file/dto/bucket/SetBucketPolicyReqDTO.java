package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设置Bucket策略请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketPolicyReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 策略文档（JSON格式）
     * 示例：
     * {
     *   "Version": "1",
     *   "Statement": [
     *     {
     *       "Effect": "Allow",
     *       "Action": ["oss:GetObject"],
     *       "Resource": ["acs:oss:*:*:my-bucket/*"],
     *       "Condition": {}
     *     }
     *   ]
     * }
     */
    private String policyDocument;

    /**
     * 是否删除现有策略
     * 如果为true，则会删除Bucket的Policy
     */
    private Boolean deleteExisting;
}

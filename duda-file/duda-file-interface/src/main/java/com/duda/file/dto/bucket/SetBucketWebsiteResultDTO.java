package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设置Bucket静态网站托管结果DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketWebsiteResultDTO {

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
     * 网站终端地址（如果是阿里云OSS）
     * 如：http://my-bucket.oss-cn-hangzhou.aliyuncs.com
     */
    private String websiteEndpoint;
}

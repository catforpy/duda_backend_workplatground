package com.duda.file.dto.bucket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设置Bucket防盗链规则请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBucketRefererReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 是否启用防盗链
     */
    private Boolean enabled;

    /**
     * 是否允许空Referer（直接访问）
     */
    private Boolean allowEmptyReferer;

    /**
     * 白名单Referer列表
     * 如：http://example.com, https://*.example.com
     */
    private List<String> refererList;

    /**
     * 是否允许空Referer的配置
     * 如果为true，则允许浏览器直接访问（没有Referer）
     */
    private Boolean allowEmpty;
}

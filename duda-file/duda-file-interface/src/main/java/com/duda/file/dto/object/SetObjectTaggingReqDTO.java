package com.duda.file.dto.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 设置对象标签请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetObjectTaggingReqDTO {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 标签（Map形式）
     * Key-Value形式，如：
     * - key1: value1
     * - key2: value2
     */
    private Map<String, String> tags;

    /**
     * 版本ID（可选，开启版本控制时使用）
     */
    private String versionId;
}

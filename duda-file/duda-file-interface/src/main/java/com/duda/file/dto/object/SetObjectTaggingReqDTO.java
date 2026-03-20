package com.duda.file.dto.object;

import lombok.AllArgsConstructor;
import java.io.Serializable;
import lombok.Builder;
import java.io.Serializable;
import lombok.Data;
import java.io.Serializable;
import lombok.NoArgsConstructor;
import java.io.Serializable;

import java.util.Map;
import java.io.Serializable;

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
public class SetObjectTaggingReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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

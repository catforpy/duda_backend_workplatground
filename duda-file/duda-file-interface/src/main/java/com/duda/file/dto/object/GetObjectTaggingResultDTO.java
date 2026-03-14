package com.duda.file.dto.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 获取对象标签结果DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetObjectTaggingResultDTO {

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
     */
    private Map<String, String> tags;

    /**
     * 版本ID（如果有）
     */
    private String versionId;

    /**
     * 标签数量
     */
    private Integer tagCount;
}

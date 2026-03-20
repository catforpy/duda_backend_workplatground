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
 * 获取对象标签结果DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetObjectTaggingResultDTO implements Serializable {

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

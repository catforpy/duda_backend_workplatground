package com.duda.file.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 上传结果（统一DTO）
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Data
@Builder
public class UploadResult {

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * ETag
     */
    private String etag;

    /**
     * 版本ID（如果支持）
     */
    private String versionId;

    /**
     * 文件大小
     */
    private Long contentLength;

    /**
     * 文件位置
     */
    private String location;

    /**
     * 最后修改时间
     */
    private Long lastModified;
}

package com.duda.file.dto;

import com.duda.file.enums.AclType;
import com.duda.file.enums.StorageClass;
import lombok.Builder;
import lombok.Data;

/**
 * 文件信息（统一DTO）
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Data
@Builder
public class FileInfo {

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * Content-Type
     */
    private String contentType;

    /**
     * ETag
     */
    private String etag;

    /**
     * 最后修改时间
     */
    private Long lastModified;

    /**
     * 文件权限
     */
    private AclType acl;

    /**
     * 存储类型
     */
    private StorageClass storageClass;

    /**
     * 版本ID
     */
    private String versionId;

    /**
     * 删除标记
     */
    private Integer deleteMarker;
}

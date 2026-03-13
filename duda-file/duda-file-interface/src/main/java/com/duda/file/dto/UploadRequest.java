package com.duda.file.dto;

import com.duda.file.enums.AclType;
import com.duda.file.enums.StorageClass;
import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.util.Map;

/**
 * 上传请求（统一DTO）
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Data
@Builder
public class UploadRequest {

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 文件流
     */
    private InputStream inputStream;

    /**
     * 内容长度
     */
    private Long contentLength;

    /**
     * Content-Type
     */
    private String contentType;

    /**
     * 自定义元数据
     */
    private Map<String, String> metadata;

    /**
     * 文件权限
     */
    private AclType acl;

    /**
     * 存储类型
     */
    private StorageClass storageClass;

    /**
     * 用户自定义头
     */
    private Map<String, String> userHeaders;
}

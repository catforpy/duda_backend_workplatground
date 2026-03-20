package com.duda.file.dto.bucket;

import com.duda.file.enums.AclType;
import com.duda.file.enums.DataRedundancyType;
import com.duda.file.enums.StorageClass;
import com.duda.file.enums.StorageType;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Bucket信息DTO
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Data
@Builder
public class BucketDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * Bucket显示名称（用户自定义）
     */
    private String displayName;

    /**
     * 存储类型
     */
    private StorageType storageType;

    /**
     * 地域
     */
    private String region;

    /**
     * 地域名称（中文）
     */
    private String regionName;

    /**
     * 创建时间
     */
    private Date creationTime;

    /**
     * 存储类型
     */
    private StorageClass storageClass;

    /**
     * 数据冗余类型
     */
    private DataRedundancyType dataRedundancyType;

    /**
     * 权限类型
     */
    private AclType acl;

    /**
     * 外网Endpoint
     */
    private String extranetEndpoint;

    /**
     * 内网Endpoint
     */
    private String intranetEndpoint;

    /**
     * 当前文件数量
     */
    private Long fileCount;

    /**
     * 当前存储使用量（字节）
     */
    private Long storageSize;

    /**
     * 存储配额（字节）
     */
    private Long storageQuota;

    /**
     * Bucket标签
     */
    private Map<String, String> tags;

    /**
     * 状态
     */
    private String status;

    /**
     * 是否开启版本控制
     */
    private Boolean versioningEnabled;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 扩展信息（JSON）
     */
    private Map<String, Object> extra;
}

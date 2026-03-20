package com.duda.file.dto.object;

import com.duda.file.enums.AclType;
import java.io.Serializable;
import com.duda.file.enums.StorageClass;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import lombok.Builder;
import java.io.Serializable;
import lombok.Data;
import java.io.Serializable;
import lombok.NoArgsConstructor;
import java.io.Serializable;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Map;
import java.io.Serializable;

/**
 * 对象DTO
 * 表示OSS中的一个对象(文件)
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键(文件名)
     */
    private String objectKey;

    /**
     * 对象ETag
     */
    private String eTag;

    /**
     * 对象类型(普通对象、追加对象、归档对象等)
     */
    private String objectType;

    /**
     * 对象大小(字节)
     */
    private Long size;

    /**
     * 存储类型
     */
    private StorageClass storageClass;

    /**
     * 访问权限
     */
    private AclType acl;

    /**
     * 对象最后修改时间
     */
    private LocalDateTime lastModified;

    /**
     * 对象创建时间
     */
    private LocalDateTime createTime;

    /**
     * 对象所有者ID
     */
    private String ownerId;

    /**
     * 对象所有者名称
     */
    private String ownerDisplayName;

    /**
     * 对象是否为文件夹
     */
    private Boolean isDirectory;

    /**
     * 是否为软链接
     */
    private Boolean isSymlink;

    /**
     * 软链接目标(如果是软链接)
     */
    private String symlinkTarget;

    /**
     * 对象存储类型(STANDARD, IA, ARCHIVE, COLD_ARCHIVE等)
     */
    private String storageType;

    /**
     * 对象是否被删除(删除标记)
     */
    private Boolean deleteMarker;

    /**
     * 对象版本ID(开启版本控制时)
     */
    private String versionId;

    /**
     * 对象标签
     */
    private Map<String, String> tags;

    /**
     * 对象元数据(部分核心字段)
     */
    private String contentType;
    private String contentEncoding;
    private String cacheControl;

    /**
     * 自定义元数据
     */
    private Map<String, String> userMetadata;

    /**
     * 对象完整性检查(MD5或CRC64)
     */
    private String contentMD5;
    private Long crc64;

    /**
     * 对象的完整路径(包含bucket)
     */
    public String getFullPath() {
        return bucketName + "/" + objectKey;
    }

    /**
     * 获取人类可读的文件大小
     */
    public String getHumanReadableSize() {
        if (size == null) {
            return "unknown";
        }
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
